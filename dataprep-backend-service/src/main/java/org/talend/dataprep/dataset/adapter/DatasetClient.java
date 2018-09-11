package org.talend.dataprep.dataset.adapter;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.talend.dataprep.command.GenericCommand.DATASET_GROUP;

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

import javax.annotation.PostConstruct;

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
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.DatasetDetailsDTO;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.conversions.inject.OwnerInjection;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadataLegacy;
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

/**
 * Adapter for legacy data model over the {@link DataCatalogClient}.
 */
@Service
public class DatasetClient {

    private static final Statistics EMPTY_STATS = new Statistics();

    @Autowired
    private DataCatalogClient dataCatalogClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private BeanConversionService conversionService;

    @Autowired
    private DataSetContentLimit dataSetContentLimit;

    @Value("${dataset.records.limit:10000}")
    private long sampleSize;

    @Autowired
    private FilterService filterService;

    @Autowired
    private OwnerInjection ownerInjection;

    private final Cache<String, AnalysisResult> computedMetadataCache = CacheBuilder
            .newBuilder() //
            .maximumSize(50) //
            .softValues() //
            .build();

    private Function<String, AnalysisResult> datasetAnalysisSupplier;

    @Autowired
    private ApplicationContext context;

    @Value("${dataset.service.provider:legacy}")
    private String catalogMode;

    @PostConstruct
    private void initializeAnalysisSupplier() {
        if ("legacy".equals(catalogMode)) {
            datasetAnalysisSupplier = this::getAnalyseDatasetFromLegacy;
        } else {
            datasetAnalysisSupplier = this::analyseDataset;
        }
    }

    // ------- Composite adapters -------

    /**
     * List all DataSetMetadata
     * <p>
     * {@link DataSetMetadata#getRowMetadata()} will returns null
     * </p>
     *
     * @param certification filter with a specific certification state
     * @param favorite filter with favorite only
     * @return DataSetMetadata without rowMetadata
     */
    public Stream<DatasetDTO> listDataSetMetadata(Dataset.CertificationState certification, Boolean favorite) {

        return dataCatalogClient.listDataset(certification, favorite).map(
                dataset -> conversionService.convert(dataset, DatasetDTO.class, ownerInjection.injectIntoDataset()));
    }

    public DataSetMetadata getDataSetMetadata(String id) {
        return toDataSetMetadata(dataCatalogClient.getMetadata(id));
    }

    public RowMetadata getDataSetRowMetadata(String id) {
        Schema dataSetSchema = dataCatalogClient.getDataSetSchema(id);
        return dataSetSchema == null ? null : AvroUtils.toRowMetadata(dataSetSchema);
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, RowMetadata rowMetadata, boolean fullContent) {
        return toDataSetRows(dataCatalogClient.getDataSetContent(id, limit(fullContent)), rowMetadata);
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, boolean fullContent) {
        DataSetMetadata metadata = getDataSetMetadata(id);
        return toDataSetRows(dataCatalogClient.getDataSetContent(id, limit(fullContent)), metadata.getRowMetadata());
    }

    /**
     * Get a dataSet by id.
     *
     * @param id the dataset to fetch
     */
    public DataSet getDataSet(String id) {
        return getDataSet(id, false, false, null);
    }

    /**
     * Get a dataSet by id.
     *
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     */
    public DataSet getDataSet(String id, boolean fullContent) {
        return getDataSet(id, fullContent, false, null);
    }

    /**
     * Get a dataSet by id.
     *
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     * @param withRowValidityMarker perform a quality analysis on the dataset records
     */
    public DataSet getDataSet(String id, boolean fullContent, boolean withRowValidityMarker) {
        return getDataSet(id, fullContent, withRowValidityMarker, null);
    }

    /**
     * Get a dataSet by id.
     * Convert metadata and records from {@link Dataset} to {@link DataSet}
     *
     * @param id the dataset to fetch
     * @param fullContent we need the full dataset or a sample (see sample limit in datset: 10k rows)
     * @param withRowValidityMarker perform a quality analysis on the dataset records
     * @param filter TQL filter for content
     */
    public DataSet getDataSet(String id, boolean fullContent, boolean withRowValidityMarker, String filter) {
        DataSet dataset = new DataSet();
        // convert metadata
        Dataset metadata = dataCatalogClient.getMetadata(id);
        if (metadata == null) {
            return null;
        }
        DataSetMetadata dataSetMetadata = toDataSetMetadata(metadata, fullContent);
        dataset.setMetadata(dataSetMetadata);

        // convert records
        Stream<GenericRecord> dataSetContent = dataCatalogClient.getDataSetContent(id, limit(fullContent));
        Stream<DataSetRow> records = toDataSetRows(dataSetContent, dataSetMetadata.getRowMetadata());
        if (withRowValidityMarker) {
            records = records.peek(addValidity(dataSetMetadata.getRowMetadata().getColumns()));
        }
        if (filter != null) {
            records = records.filter(filterService.build(filter, dataSetMetadata.getRowMetadata()));
        }
        dataset.setRecords(records);

        // DataSet specifics
        if (!fullContent) {
            dataSetMetadata.getContent().getLimit().ifPresent(
                    limit -> dataset.setRecords(dataset.getRecords().limit(limit)));
        }
        return dataset;
    }

    public Stream<DataSetMetadata> searchDataset(String name, boolean strict) {
        Stream<Dataset> datasetStream = dataCatalogClient.listDataset(null, null);
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
        if (dataSetContentLimit.limitContentSize() && dataSetContentLimit.getLimit() != null) {
            recordsLimitApply = this.dataSetContentLimit.getLimit();
        }
        if (!fullContent) {
            recordsLimitApply = sampleSize;
        }
        return recordsLimitApply;
    }

    /**
     * @deprecated : Still present because Chained commands still need this one.
     */
    @Deprecated
    public HystrixCommand<InputStream> getDataSetGetCommand(final String dataSetId, final boolean fullContent,
            final boolean includeInternalContent) {
        return new HystrixCommand<InputStream>(DATASET_GROUP) {

            @Override
            protected InputStream run() throws IOException {
                DataSet dataSet = getDataSet(dataSetId, fullContent, includeInternalContent);
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
        return AvroUtils.buildToDataSetRowConverter(rowMetadata);
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

        if (rowMetadata != null && rowMetadata.getColumns().stream().map(ColumnMetadata::getStatistics).anyMatch(
                this::isComputedStatistics)) {
            AnalysisResult analysisResult = datasetAnalysisSupplier.apply(dataset.getId());
            metadata.setRowMetadata(new RowMetadata(analysisResult.rowMetadata));
            metadata.getContent().setNbRecords(analysisResult.rowcount);
        }

        return metadata;
    }

    private boolean isComputedStatistics(Statistics statistics) {
        return statistics == null || EMPTY_STATS.equals(statistics);
    }

    // No cache as metadata may be updated without notice (see BackgroundAnalysis that update metadata twice)
    private AnalysisResult getAnalyseDatasetFromLegacy(String id) {
        DataSetMetadata metadata = context.getBean(DataSetGetMetadataLegacy.class, id).execute();
        return new AnalysisResult(metadata.getRowMetadata(), metadata.getContent().getNbRecords());
    }

    private AnalysisResult analyseDataset(String id) {
        try {
            return computedMetadataCache.get(id, () -> {
                AtomicLong count = new AtomicLong(0);
                RowMetadata rowMetadata = getDataSetRowMetadata(id);
                try (Stream<DataSetRow> records =
                        dataCatalogClient.getDataSetContent(id, sampleSize).map(toDatasetRow(rowMetadata))) {
                    analyzerService.analyzeFull(records, rowMetadata.getColumns());
                }
                return new AnalysisResult(rowMetadata, count.get());
            });
        } catch (ExecutionException e) {
            // source method do not throw checked exception
            throw (RuntimeException) e.getCause();
        }
    }

    public DatasetDetailsDTO getDataSetDetails(String id) {
        Dataset dataset = dataCatalogClient.getMetadata(id);
        return conversionService.convert(dataset, DatasetDetailsDTO.class, ownerInjection.injectIntoDatasetDetails());

    }

    private class AnalysisResult {

        private final RowMetadata rowMetadata;

        private final long rowcount;

        AnalysisResult(RowMetadata rowMetadata, long rowcount) {
            this.rowMetadata = rowMetadata;
            this.rowcount = rowcount;
        }
    }

    @EventListener
    public void cleanCacheEntryOnDatasetModification(DatasetUpdatedEvent event) {
        computedMetadataCache.invalidate(event.getSource().getId());
    }

}
