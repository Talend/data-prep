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

import org.apache.commons.lang3.StringUtils;
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
        if (metadata == null) {
            LOGGER.info("Unable to analyze quality of data set #{}: seems to be removed.", dataSetId);
            return;
        }
        final List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();
        if (columns.isEmpty()) {
            LOGGER.debug("Skip statistics of {} (no column information).", metadata.getId());
            return;
        }
        if (!metadata.getLifecycle().schemaAnalyzed()) {
            LOGGER.debug(
                    "Dataset {}, schema information must be computed before quality analysis can be performed, ignoring message",
                    metadata.getId());
            return; // no acknowledge to allow re-poll.
        }
        // base analysis
        try (final Stream<DataSetRow> stream = store.stream(metadata)) {
            try (Analyzer<Analyzers.Result> analyzer = analyzerService.schemaAnalysis(columns)) {
                computeStatistics(analyzer, columns, stream);
                LOGGER.debug("Base statistics analysis done for {}", dataSetId);
                // Save base analysis
                saveAnalyzerResults(analyzer, metadata);
            }
        } catch (Exception e) {
            LOGGER.warn("Base statistics analysis, dataset {} generates an error", dataSetId, e);
            throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
        }
        // advanced analysis
        try (final Stream<DataSetRow> stream = store.stream(metadata);
                Analyzer<Analyzers.Result> analyzerAdvanced = analyzerService.advancedAnalysis(columns)) {
            computeStatistics(analyzerAdvanced, columns, stream);
            updateNbRecords(metadata, analyzerAdvanced.getResult());
            LOGGER.debug("Advanced statistics analysis done for {}", dataSetId);
            // Save advanced analysis
            saveAnalyzerResults(analyzerAdvanced, metadata);
        } catch (Exception e) {
            LOGGER.warn("Advanced statistics analysis, dataset {} generates an error", dataSetId, e);
            throw new TDPException(UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
        }
        LOGGER.info("Statistics analysis done for {}", dataSetId);
    }

    private void saveAnalyzerResults(Analyzer<Analyzers.Result> analyzer, DataSetMetadata metadata) {
        DistributedLock datasetLock = repository.createDatasetMetadataLock(metadata.getId());
        try {
            datasetLock.lock();
            final DataSetMetadata savedDataSetMetadata = repository.get(metadata.getId());
            if (savedDataSetMetadata != null) {
                adapter.adapt(metadata.getRowMetadata().getColumns(), analyzer.getResult());
                repository.save(metadata);
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
        if (metadata.getContent().getNbRecords() == 0 && result.exist(ValueQualityStatistics.class)) {
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
