package org.talend.dataprep.dataset.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.hystrix.HystrixCommand;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetContent;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadata;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetSchema;
import org.talend.dataprep.dataset.adapter.commands.DatasetList;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.util.avro.AvroUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.GenericCommand.DATASET_GROUP;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

/**
 * Client based on Hystrix commands to call a dataset API. Exposes native avro calls and conversions.
 */
// It also allows to avoid using context.getBean everywhere
@Service
public class ApiDatasetClient {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSetMetadataBuilder dataSetMetadataBuilder;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private BeanConversionService conversionService;

    @Autowired
    private DataSetContentLimit limit;

    @Value("${dataset.records.limit:10000}")
    private long sampleSize;

    @Autowired
    private FilterService filterService;

    private final Cache<String, AnalysisResult> metadataCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .softValues()
            .build();


    // ------- Pure API -------

    public Stream<Dataset> listDataset() {
        return context.getBean(DatasetList.class).execute();
    }

    public Dataset getMetadata(String id) {
        return context.getBean(DataSetGetMetadata.class, id).execute();
    }

    public Schema getDataSetSchema(String id) {
        return context.getBean(DataSetGetSchema.class, id).execute();
    }

    public Stream<GenericRecord> getDataSetContent(String id) {
        Schema schema = getDataSetSchema(id);
        return context.getBean(DataSetGetContent.class, id, schema).execute();
    }

    // ------- Composite adapters -------

    public DataSetMetadata getDataSetMetadata(String id) {
        return toDataSetMetadata(getMetadata(id));
    }

    public RowMetadata getDataSetRowMetadata(String id) {
        Schema dataSetSchema = getDataSetSchema(id);
        return AvroUtils.toRowMetadata(dataSetSchema);
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, RowMetadata rowMetadata) {
        return getDataSetContent(id).map(toDatasetRow(rowMetadata));
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id) {
        DataSetMetadata metadata = getDataSetMetadata(id);
        return toDataSetRows(getDataSetContent(id), metadata);
    }

    public DataSet getDataSet(String id) {
        return getDataSet(id, false);
    }

    public DataSet getDataSet(String id, boolean fullContent) {
        DataSet dataset = new DataSet();
        // convert metadata
        DataSetMetadata metadata = toDataSetMetadata(getMetadata(id), fullContent);
        dataset.setMetadata(metadata);
        // convert records
        dataset.setRecords(toDataSetRows(getDataSetContent(id), metadata));

        // DataSet specifics
        metadata.getContent().getLimit().ifPresent(theLimit -> dataset.setRecords(dataset.getRecords().limit(theLimit)));
        return dataset;
    }

    /**
     *
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     * @param filter TQL filter for content
     */
    public DataSet getDataSet(String id, boolean fullContent, String filter) {
        DataSet dataSet = getDataSet(id, fullContent);
        dataSet.setRecords(filter(dataSet.getRecords(), filter, dataSet.getMetadata().getRowMetadata()));
        return dataSet;
    }

    public Stream<DataSetMetadata> searchDataset(String name, boolean strict) {
        return listDataset()
                .filter(ds -> {
                    boolean valid;
                    String label = ds.getLabel();
                    if (label != null) {
                        if (strict) {
                            valid = label.equals(name);
                        } else {
                            valid = label.contains(name);
                        }
                    } else {
                        valid = name == null;
                    }
                    return valid;
                })
                .map(this::toDataSetMetadata);
    }

    private Stream<DataSetRow> filter(Stream<DataSetRow> stream, String filter, RowMetadata metadata) {
        return stream.filter(filterService.build(filter, metadata));
    }

    private Long limit(boolean fullContent) {
        Long recordsLimitApply = null;
        if (limit.limitContentSize() && limit.getLimit() != null) {
            recordsLimitApply = this.limit.getLimit();
        }
        if (!fullContent) {
            recordsLimitApply = sampleSize;
        }
        return recordsLimitApply;
    }

    /**
     * Still present because Chained commands still need this one.
     *
     * @param dataSetId
     * @param fullContent
     * @param includeInternalContent
     * @return
     */
    @Deprecated
    public HystrixCommand<InputStream> getDataSetGetCommand(final String dataSetId, final boolean fullContent, final boolean includeInternalContent) {
        DataSet dataSet = getDataSet(dataSetId, fullContent);
        return new HystrixCommand<InputStream>(DATASET_GROUP) {

            @Override
            protected InputStream run() throws IOException {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                mapper.writerFor(DataSet.class).writeValue(out, dataSet);
                return new ByteArrayInputStream(out.toByteArray());
            }
        };
    }

    // ------- Utilities -------


    private Stream<DataSetRow> toDataSetRows(Stream<GenericRecord> dataSetContent, DataSetMetadata metadata) {
        return dataSetContent.map(toDatasetRow(metadata.getRowMetadata()));
    }
    // GenericRecord -> DataSetRow
    private Function<GenericRecord, DataSetRow> toDatasetRow(RowMetadata rowMetadata) {
        return AvroUtils.toDataSetRowConverter(rowMetadata);
    }

    // Dataset -> DataSetMetadata
    private DataSetMetadata toDataSetMetadata(Dataset dataset) {
        return toDataSetMetadata(dataset, false);
    }

    private DataSetMetadata toDataSetMetadata(Dataset dataset, boolean fullContent) {
        RowMetadata rowMetadata = getDataSetRowMetadata(dataset.getId());
        DataSetMetadata metadata = conversionService.convert(dataset, DataSetMetadata.class);
        metadata.setRowMetadata(rowMetadata);
        metadata.getContent().setLimit(limit(fullContent));

        if (rowMetadata.getColumns().stream().anyMatch(c -> c.getStatistics() != null)) {
            try {
                AnalysisResult analysisResult = metadataCache.get(dataset.getId(), () -> analyseDataset(dataset.getId(), rowMetadata));
                metadata.setRowMetadata(new RowMetadata(analysisResult.rowMetadata)); // because sadly, my cache is not immutable
                metadata.setDataSetSize(analysisResult.rowcount);
                metadata.getContent().setNbRecords(analysisResult.rowcount);
            } catch (ExecutionException e) {
                // source method do not throw checked exception
                throw (RuntimeException) e.getCause();
            }
        }

        return metadata;
    }

    private AnalysisResult analyseDataset(String id, RowMetadata rowMetadata) {
        AtomicLong count = new AtomicLong(0);
        try (Stream<DataSetRow> records = getDataSetContentAsRows(id, rowMetadata).peek(e -> count.incrementAndGet())) {
            analyzerService.analyzeFull(records, rowMetadata.getColumns());
        }
        return new AnalysisResult(rowMetadata, count.get());
    }

    private class AnalysisResult {
        private final RowMetadata rowMetadata;
        private final long rowcount;

        public AnalysisResult(RowMetadata rowMetadata, long rowcount) {
            this.rowMetadata = rowMetadata;
            this.rowcount = rowcount;
        }
    }

    private RowMetadata getPreparationMetadata(String preparationId) {
        final PreparationDetailsGet preparationDetailsGet = context.getBean(PreparationDetailsGet.class, preparationId, Step.ROOT_STEP.id());
        try (InputStream details = preparationDetailsGet.execute()) {
            PreparationMessage preparationMessage = mapper.readerFor(PreparationMessage.class).readValue(details);
            return preparationMessage.getRowMetadata();
        } catch (Exception e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }
    }

}
