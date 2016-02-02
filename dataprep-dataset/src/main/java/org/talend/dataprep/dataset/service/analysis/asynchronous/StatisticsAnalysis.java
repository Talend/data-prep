//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_TO_ANALYZE_DATASET_QUALITY;

import java.util.List;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.dataset.service.Destinations;
import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;
import org.talend.datascience.common.inference.ValueQualityStatistics;

/**
 * Compute statistics analysis on the full dataset.
 */
@Component
public class StatisticsAnalysis implements AsynchronousDataSetAnalyzer {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsAnalysis.class);

    /** Dataset metadata repository. */
    @Autowired
    DataSetMetadataRepository repository;

    /** DataSet content store. */
    @Autowired
    ContentStoreRouter store;

    /** Analyzer service */
    @Autowired
    AnalyzerService analyzerService;

    /** Statistics adapter. */
    @Autowired
    StatisticsAdapter adapter;

    /**
     * Receives jms message to start a quality analysis.
     * 
     * @param message the jms message that holds the dataset id.
     */
    @JmsListener(destination = Destinations.STATISTICS_ANALYSIS)
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
     * @see DataSetAnalyzer#analyze
     */
    @Override
    public void analyze(String dataSetId) {

        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }

        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            LOGGER.debug("Statistics analysis starts for {}", dataSetId);

            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata != null) {
                if (!metadata.getLifecycle().schemaAnalyzed()) {
                    LOGGER.debug(
                            "Dataset {}, schema information must be computed before quality analysis can be performed, ignoring message",
                            metadata.getId());
                    return; // no acknowledge to allow re-poll.
                }

                final List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();
                if (columns.isEmpty()) {
                    LOGGER.debug("Skip statistics of {} (no column information).", metadata.getId());
                } else {
                    // base analysis
                    try (final Stream<DataSetRow> stream = store.stream(metadata)) {
                        final Analyzer<Analyzers.Result> analyzer = analyzerService.baselineAnalysis(columns);
                        computeStatistics(analyzer, columns, stream);
                        updateNbRecords(metadata, analyzer.getResult());
                        LOGGER.debug("Base statistics analysis done for{}", dataSetId);
                    } catch (Exception e) {
                        LOGGER.warn("Base statistics analysis, dataset {} generates an error", dataSetId, e);
                        throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
                    }

                    // advanced analysis
                    try (final Stream<DataSetRow> stream = store.stream(metadata)) {
                        final Analyzer<Analyzers.Result> analyzer = analyzerService.advancedAnalysis(columns);
                        computeStatistics(analyzer, columns, stream);
                        LOGGER.debug("Advanced statistics analysis done for{}", dataSetId);
                    } catch (Exception e) {
                        LOGGER.warn("Advances statistics analysis, dataset {} generates an error", dataSetId, e);
                        throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
                    }

                    // Tag data set quality: now analyzed
                    metadata.getLifecycle().qualityAnalyzed(true);
                    repository.add(metadata);
                    LOGGER.info("Statistics analysis done for {}", dataSetId);
                }

            } else {
                LOGGER.info("Unable to analyze quality of data set #{}: seems to be removed.", dataSetId);
            }
        } finally {
            datasetLock.unlock();
        }

    }

    /**
     * Update the number of records for the dataset.
     * 
     * @param metadata the dataset metadata to update.
     * @param results the
     */
    private void updateNbRecords(DataSetMetadata metadata, List<Analyzers.Result> results) {
        // defensive programming
        if (results.isEmpty()) {
            return;
        }
        // get the analyzer of the first column
        final Analyzers.Result result = results.get(0);
        if (result.exist(ValueQualityStatistics.class)) {
            final ValueQualityStatistics valueQualityStatistics = result.get(ValueQualityStatistics.class);
            metadata.getContent().setNbRecords(valueQualityStatistics.getCount());
        }
        LOGGER.debug("nb records for {} is updated to {}", metadata.getId(), metadata.getContent().getNbRecords());
    }

    /**
     * Compute the statistics for the given dataset metadata and content.
     *
     * @param analyzer the analyzer to perform.
     * @param columns the columns metadata.
     * @param stream the content to compute the statistics from.
     */
    private void computeStatistics(final Analyzer<Analyzers.Result> analyzer, final List<ColumnMetadata> columns,
            final Stream<DataSetRow> stream) {
        // Create a content with the expected format for the StatisticsClientJson class
        stream.map(row -> row.toArray(DataSetRow.SKIP_TDP_ID)).forEach(analyzer::analyze);
        analyzer.end();

        // Store results back in data set
        adapter.adapt(columns, analyzer.getResult());
    }

    /**
     * Compute the statistics for the given dataset metadata and content.
     *
     * @param metadata the metadata to compute the statistics for.
     * @param stream the content to compute the statistics from.
     */
    public void computeFullStatistics(final DataSetMetadata metadata, final Stream<DataSetRow> stream) {
        final List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();
        if (columns.isEmpty()) {
            LOGGER.debug("Skip statistics of {} (no column information).", metadata.getId());
            return;
        }
        final Analyzer<Analyzers.Result> analyzer = analyzerService.full(columns);
        computeStatistics(analyzer, columns, stream);
    }

    /**
     * @see AsynchronousDataSetAnalyzer#destination()
     */
    @Override
    public String destination() {
        return Destinations.STATISTICS_ANALYSIS;
    }
}
