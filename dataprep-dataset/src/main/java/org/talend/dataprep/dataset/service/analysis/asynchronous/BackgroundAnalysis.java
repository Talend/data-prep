// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_TO_ANALYZE_DATASET_QUALITY;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;

/**
 * Compute statistics analysis on the full dataset.
 */
@Component
public class BackgroundAnalysis {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundAnalysis.class);

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
     * @see DataSetAnalyzer#analyze
     */
    public void analyze(String dataSetId) {

        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }

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
                    try (Analyzer<Analyzers.Result> analyzer = analyzerService.schemaAnalysis(columns)) {
                        computeStatistics(analyzer, columns, stream);
                        LOGGER.debug("Base statistics analysis done for{}", dataSetId);
                        // Save base analysis
                        saveAnalyzerResults(dataSetId, analyzer);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Base statistics analysis, dataset {} generates an error", dataSetId, e);
                    throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
                }
                // advanced analysis
                try (final Stream<DataSetRow> stream = store.stream(metadata)) {
                    try (Analyzer<Analyzers.Result> analyzer = analyzerService.full(columns)) {
                        computeStatistics(analyzer, columns, stream);
                        updateNbRecords(metadata, analyzer.getResult());
                        LOGGER.debug("Advanced statistics analysis done for{}", dataSetId);
                        // Save advanced analysis
                        saveAnalyzerResults(dataSetId, analyzer);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Advanced statistics analysis, dataset {} generates an error", dataSetId, e);
                    throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
                }
                // Tag data set quality: now analyzed
                DistributedLock datasetLock = repository.createDatasetMetadataLock(metadata.getId());
                try {
                    datasetLock.lock();
                    final DataSetMetadata dataSetMetadata = repository.get(dataSetId);
                    if (dataSetMetadata != null) {
                        dataSetMetadata.getLifecycle().qualityAnalyzed(true);
                        repository.save(metadata);
                    }
                } finally {
                    datasetLock.unlock();
                }
                LOGGER.info("Statistics analysis done for {}", dataSetId);
            }
        } else {
            LOGGER.info("Unable to analyze quality of data set #{}: seems to be removed.", dataSetId);
        }
    }

    private void saveAnalyzerResults(String id, Analyzer<Analyzers.Result> analyzer) {
        DistributedLock datasetLock = repository.createDatasetMetadataLock(id);
        try {
            datasetLock.lock();
            final DataSetMetadata dataSetMetadata = repository.get(id);
            if (dataSetMetadata != null) {
                adapter.adapt(dataSetMetadata.getRowMetadata().getColumns(), analyzer.getResult());
                repository.save(dataSetMetadata);
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

}
