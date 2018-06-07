package org.talend.dataprep.dataset.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetContent;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadata;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetSchema;
import org.talend.dataprep.dataset.adapter.commands.DatasetList;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.util.avro.AvroUtils;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.hystrix.HystrixCommand;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.talend.dataprep.command.GenericCommand.DATASET_GROUP;

/**
 * Client based on Hystrix commands to call a dataset API. Exposes native avro calls and conversions.
 */
// It also allows to avoid using context.getBean everywhere
@Service
public class ApiDatasetClient {

    @Autowired
    private ApplicationContext context;

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

    public Stream<Dataset> listDataset(Dataset.CertificationState certification, Boolean favorite) {
        return context.getBean(DatasetList.class, certification, favorite).execute();
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
        return dataSetSchema == null ? null : AvroUtils.toRowMetadata(dataSetSchema);
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, RowMetadata rowMetadata) {
        return toDataSetRows(getDataSetContent(id), rowMetadata);
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id) {
        DataSetMetadata metadata = getDataSetMetadata(id);
        return toDataSetRows(getDataSetContent(id), metadata.getRowMetadata());
    }

    /**
     * Get a dataSet by id.
     * @param id the dataset to fetch
     */
    public DataSet getDataSet(String id) {
        return getDataSet(id, false, false, null);
    }

    /**
     * Get a dataSet by id.
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     */
    public DataSet getDataSet(String id, boolean fullContent) {
        return getDataSet(id, fullContent, false, null);
    }

    /**
     * Get a dataSet by id.
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     * @param filter TQL filter for content
     */
    public DataSet getDataSet(String id, boolean fullContent, String filter) {
        return getDataSet(id, fullContent, false, filter);
    }

    /**
     * Get a dataSet by id.
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     * @param withRowValidityMarker perform a quality analysis on the dataset records
     * @param filter TQL filter for content
     */
    public DataSet getDataSet(String id, boolean fullContent, boolean withRowValidityMarker, String filter) {
        DataSet dataset = new DataSet();
        // convert metadata
        DataSetMetadata metadata = toDataSetMetadata(getMetadata(id), fullContent);
        dataset.setMetadata(metadata);
        // convert records
        Stream<DataSetRow> records = toDataSetRows(getDataSetContent(id), metadata.getRowMetadata());
        if (withRowValidityMarker) {
            records = records.peek(addValidity(metadata.getRowMetadata().getColumns()));
        }
        if (filter != null) {
            records = records.filter(filterService.build(filter, metadata.getRowMetadata()));
        }
        dataset.setRecords(records);

        // DataSet specifics
        metadata.getContent().getLimit().ifPresent(theLimit -> dataset.setRecords(dataset.getRecords().limit(theLimit)));
        return dataset;
    }

    public Stream<DataSetMetadata> searchDataset(String name, boolean strict) {
        Stream<Dataset> datasetStream = listDataset(null, null);
        if (isNotBlank(name)) {
            if (strict) {
                datasetStream = datasetStream.filter(dataset -> name.equalsIgnoreCase(dataset.getLabel()));
            } else {
                datasetStream = datasetStream.filter(dataset -> containsIgnoreCase(dataset.getLabel(), name));
            }
        }
        return datasetStream.filter(Objects::nonNull).map(this::toDataSetMetadata);
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
        DataSet dataSet = getDataSet(dataSetId, fullContent, false, null);
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

    private Stream<DataSetRow> toDataSetRows(Stream<GenericRecord> dataSetContent, RowMetadata rowMetadata) {
        return dataSetContent.map(toDatasetRow(rowMetadata));
    }

    private Consumer<DataSetRow> addValidity(List<ColumnMetadata> columns) {
        final Analyzer<Analyzers.Result> analyzer = analyzerService.build(columns, AnalyzerService.Analysis.QUALITY);
        InvalidMarker invalidMarker = new InvalidMarker(columns, analyzer);
        return invalidMarker::apply;
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

        if (rowMetadata != null && rowMetadata.getColumns().stream().map(ColumnMetadata::getStatistics).anyMatch(this::isComputedStatistics)) {
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

    private static final Statistics EMPTY_STATS = new Statistics();

    private boolean isComputedStatistics(Statistics statistics) {
        return statistics == null || EMPTY_STATS.equals(statistics);
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

    // TODO: WIP
    @EventListener
    public void onUpdate(DatasetUpdatedEvent event) {
        metadataCache.invalidate(event.getSource().getId());
    }

}
