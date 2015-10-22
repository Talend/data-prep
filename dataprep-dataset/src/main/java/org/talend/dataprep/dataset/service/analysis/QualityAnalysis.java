package org.talend.dataprep.dataset.service.analysis;

import java.util.List;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.configuration.AnalyzerService;
import org.talend.dataprep.dataset.service.Destinations;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataquality.statistics.quality.ValueQualityStatistics;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;

@Component
public class QualityAnalysis implements SynchronousDataSetAnalyzer, AsynchronousDataSetAnalyzer {

    @Value("max_records")
    public static final int MAX_RECORD = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(QualityAnalysis.class);

    @Autowired
    DataSetMetadataRepository repository;

    @Autowired
    ContentStoreRouter store;

    @Autowired
    StatisticsAdapter adapter;

    @Autowired
    AnalyzerService analyzerService;

    @JmsListener(destination = Destinations.QUALITY_ANALYSIS)
    public void analyzeQuality(Message message) {
        try {
            String dataSetId = message.getStringProperty("dataset.id"); //$NON-NLS-1$
            try {
                analyze(dataSetId);
            } finally {
                message.acknowledge();
            }
        } catch (JMSException e) {
            throw new TDPException(DataSetErrorCodes.UNEXPECTED_JMS_EXCEPTION, e);
        }
    }

    /**
     * Analyse the dataset metadata quality.
     *
     * @param dataSetId the dataset id.
     */
    @Override
    public void analyze(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }
        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata == null) {
                LOGGER.info("Unable to analyze quality of data set #{}: seems to be removed.", dataSetId);
                return;
            }
            if (!metadata.getLifecycle().inProgress()) {
                LOGGER.debug("No need to recompute quality of data set #{} (statistics are completed).", dataSetId);
                return;
            }
            try (Stream<DataSetRow> stream = store.stream(metadata)) {
                if (!metadata.getLifecycle().schemaAnalyzed()) {
                    LOGGER.debug("Schema information must be computed before quality analysis can be performed, ignoring message");
                    return; // no acknowledge to allow re-poll.
                }
                LOGGER.info("Analyzing quality of dataset #{}...", metadata.getId());
                // New data set, or reached the max limit of records for synchronous analysis, trigger a full scan (but
                // async).
                final int dataSetSize = metadata.getContent().getNbRecords();
                final boolean isNewDataSet = dataSetSize == 0;
                if (isNewDataSet || dataSetSize == MAX_RECORD) {
                    // If data set size is MAX_RECORD, performs a full scan, otherwise only take first MAX_RECORD
                    // records.
                    computeQuality(metadata, stream, dataSetSize == MAX_RECORD ? -1 : MAX_RECORD);
                }
                // Turn on / off "in progress" flag
                if (isNewDataSet && metadata.getContent().getNbRecords() >= MAX_RECORD) {
                    metadata.getLifecycle().inProgress(true);
                } else {
                    metadata.getLifecycle().inProgress(false);
                }
                // ... all quality is now analyzed, mark it so.
                metadata.getLifecycle().qualityAnalyzed(true);
                repository.add(metadata);
                LOGGER.info("Analyzed quality of dataset #{}.", dataSetId);
            } catch (Exception e) {
                LOGGER.warn("dataset '{}' generate an error, message: {} ", dataSetId, e.getMessage());
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
            }
        } finally {
            datasetLock.unlock();
        }
    }

    /**
     * @see SynchronousDataSetAnalyzer#order()
     */
    @Override
    public int order() {
        return 3;
    }

    /**
     * Compute the quality (count, valid, invalid and empty) of the given dataset.
     * 
     * @param dataset the dataset metadata.
     * @param records the dataset records
     * @param limit indicates how many records will be read from stream. Use a number < 0 to perform a full scan of
     */
    public void computeQuality(DataSetMetadata dataset, Stream<DataSetRow> records, long limit) {
        // Compute valid / invalid / empty count, need data types for analyzer first
        final List<ColumnMetadata> columns = dataset.getRow().getColumns();
        if (columns.isEmpty()) {
            LOGGER.debug("Skip analysis of {} (no column information).", dataset.getId());
            return;
        }
        final Analyzer<Analyzers.Result> analyzer = analyzerService.qualityAnalysis(columns);
        if (limit > 0) { // Only limit number of rows if limit > 0 (use limit to speed up sync analysis.
            LOGGER.debug("Limit analysis to the first {}.", limit);
            records = records.limit(limit);
        } else {
            LOGGER.debug("Performing full analysis.");
        }
        records.map(row -> row.toArray(DataSetRow.SKIP_TDP_ID)).forEach(analyzer::analyze);
        // Determine content size
        final List<Analyzers.Result> result = analyzer.getResult();
        adapter.adapt(columns, result);
        // Remember the number of records
        if (!result.isEmpty()) {
            final long recordCount = result.get(0).get(ValueQualityStatistics.class).getCount();
            dataset.getContent().setNbRecords((int) recordCount);
        }
    }

    @Override
    public String destination() {
        return Destinations.QUALITY_ANALYSIS;
    }
}