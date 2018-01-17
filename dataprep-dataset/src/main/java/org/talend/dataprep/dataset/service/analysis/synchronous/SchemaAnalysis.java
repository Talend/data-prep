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

import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_TO_ANALYZE_COLUMN_TYPES;

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
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

/**
 * <p>
 * Schema analysis use the first 20 rows of the dataset content to find out columns' :
 * <ul>
 * <li>data type</li>
 * <li>semantic</li>
 * <li>quality (valid, invalid and empty)</li>
 * </ul>
 * </p>
 */
@Component
public class SchemaAnalysis implements SynchronousDataSetAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaAnalysis.class);

    @Autowired
    DataSetMetadataRepository repository;

    @Autowired
    ContentStoreRouter store;

    @Autowired
    StatisticsAdapter adapter;

    @Autowired
    AnalyzerService analyzerService;

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
                LOGGER.info("Unable to analyze schema of data set #{}: seems to be removed.", dataSetId);
                return;
            }
            // Schema analysis
            try (Stream<DataSetRow> stream = store.stream(metadata, 100)) {
                LOGGER.info("Analyzing schema in dataset #{}...", dataSetId);
                // Configure analyzers
                final List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();
                try (Analyzer<Analyzers.Result> analyzer = analyzerService.schemaAnalysis(columns)) {
                    // Determine schema for the content.
                    stream.limit(100).map(row -> row.toArray(DataSetRow.SKIP_TDP_ID)).forEach(analyzer::analyze);

                    // Find the best suitable type
                    adapter.adapt(columns, analyzer.getResult());
                    LOGGER.info("Analyzed schema in dataset #{}.", dataSetId);
                    metadata.getLifecycle().schemaAnalyzed(true);
                    repository.save(metadata);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to analyse schema for dataset " + dataSetId + ".", e);
                TDPException.rethrowOrWrap(e, UNABLE_TO_ANALYZE_COLUMN_TYPES);
            }
        } finally {
            datasetLock.unlock();
        }
    }

    @Override
    public int order() {
        return 1;
    }
}
