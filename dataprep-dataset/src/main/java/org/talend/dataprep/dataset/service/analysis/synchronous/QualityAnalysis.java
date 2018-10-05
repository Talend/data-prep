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

package org.talend.dataprep.dataset.service.analysis.synchronous;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

@Component
public class QualityAnalysis implements SynchronousDataSetAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QualityAnalysis.class);

    @Autowired
    DataSetMetadataRepository repository;

    @Autowired
    ContentStoreRouter store;

    @Autowired
    StatisticsAdapter adapter;

    @Autowired
    AnalyzerService analyzerService;

    @Value("#{'${max_records:2000}'}")
    private final int maxRecord = 2000;

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
            // e.g. excel multi sheet dataset when user has not choose the sheet yet
            if (!metadata.getLifecycle().isInProgress()) {
                LOGGER.debug("No need to recompute quality of data set #{} (statistics are completed).", dataSetId);
                return;
            }
            if (!metadata.getLifecycle().schemaAnalyzed()) {
                LOGGER.debug(
                        "Schema information must be computed before quality analysis can be performed, ignoring message");
                return; // no acknowledge to allow re-poll.
            }

            try (Stream<DataSetRow> stream = store.stream(metadata)) {

                LOGGER.debug("Analyzing quality of dataset #{}...", metadata.getId());
                // New data set, or reached the max limit of records for synchronous analysis, trigger a full scan (but
                // async).
                final long dataSetSize = metadata.getContent().getNbRecords();
                final boolean isNewDataSet = dataSetSize == 0;
                if (isNewDataSet || dataSetSize == maxRecord) {
                    // If data set size is maxRecord, performs a full scan, otherwise only take first maxRecord
                    // records.
                    computeQuality(metadata, stream, dataSetSize == maxRecord ? -1 : maxRecord);
                }
                // Turn on / off "in progress" flag
                if (isNewDataSet && metadata.getContent().getNbRecords() >= maxRecord) {
                    metadata.getLifecycle().setInProgress(true);
                } else {
                    metadata.getLifecycle().setInProgress(false);
                }
                // ... all quality is now analyzed, mark it so.
                metadata.getLifecycle().qualityAnalyzed(true);
                final DataSetMetadata savedDataSetMetadata = repository.get(metadata.getId());
                // in order to check that the dataset was not deleted during analysis
                if (savedDataSetMetadata != null) {
                    repository.save(metadata);
                    LOGGER.debug("Analyzed quality of dataset #{}.", dataSetId);
                }

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
        // Compute sample / valid / invalid / empty / count, need data types for analyzer first
        final List<ColumnMetadata> columns = dataset.getRowMetadata().getColumns();
        if (columns.isEmpty()) {
            LOGGER.debug("Skip analysis of {} (no column information).", dataset.getId());
            return;
        }
        try (Analyzer<Analyzers.Result> analyzer = analyzerService.qualityAnalysis(columns)) {
            AtomicLong sampleRecordsCount = new AtomicLong(0);
            records.peek(r -> sampleRecordsCount.incrementAndGet()).map(row -> {
                if (sampleRecordsCount.get() < limit) {
                    return row.toArray(DataSetRow.SKIP_TDP_ID);
                }
                return null;
            }).filter(Objects::nonNull).forEach(analyzer::analyze);
            final List<Analyzers.Result> result = analyzer.getResult();
            adapter.adapt(columns, result);
            // Remember the number of records of the current sample
            dataset.getContent().setNbRecords((int) sampleRecordsCount.get());
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
