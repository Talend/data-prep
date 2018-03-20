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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.schema.csv.CSVFormatFamily;

/**
 * This analyzer means to index the content for search.
 */
@Component
public class ContentAnalysis implements SynchronousDataSetAnalyzer {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ContentAnalysis.class);

    /** Size limit for datasets. */
    @Value("${dataset.records.limit:10000}")
    private Long sizeLimit;

    /** DataSet metadata repository. */
    @Autowired
    private DataSetMetadataRepository repository;

    /** Dataset content store. */
    @Autowired
    private ContentStoreRouter contentStore;

    /**
     * @see DataSetAnalyzer#analyze(String)
     */
    @Override
    public void analyze(String dataSetId) {

        // defensive programming
        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }

        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata != null) {
                LOG.info("Indexing content of data set #{}...", metadata.getId());

                updateHeaderAndFooter(metadata);
                updateLimit(metadata);

                metadata.getLifecycle().contentIndexed(true);
                repository.save(metadata);
                LOG.info("Indexed content of data set #{}.", dataSetId);
            } else {
                LOG.info("Data set #{} no longer exists.", dataSetId); //$NON-NLS-1$
            }
        } finally {
            datasetLock.unlock();
        }
    }

    /**
     * Update the header and footer information in the dataset metadata.
     *
     * @param metadata the dataset metadata to update.
     */
    private void updateHeaderAndFooter(DataSetMetadata metadata) {

        DataSetContent datasetContent = metadata.getContent();
        // parameters
        final Map<String, String> parameters = metadata.getContent().getParameters();
        int headerNBLines = 1;
        try {
            headerNBLines = Integer.parseInt(parameters.get(CSVFormatFamily.HEADER_NB_LINES_PARAMETER));
        } catch (NumberFormatException e) {
            LOG.info("No header information for {}, let's use the first line as header.", metadata.getId());
        }

        datasetContent.setNbLinesInHeader(headerNBLines);
        datasetContent.setNbLinesInFooter(0);
    }

    /**
     * Update the dataset limit if it's too large for the system settings.
     *
     * @param metadata the dataset metadata to update.
     */
    private void updateLimit(DataSetMetadata metadata) {
        // auto closable block is really important to make sure the stream is closed after the limit is set
        try (final Stream<DataSetRow> stream = contentStore.stream(metadata)) {
            final Optional<DataSetRow> firstAfterLimit = stream.skip(sizeLimit).findAny();
            if (firstAfterLimit.isPresent()) {
                metadata.getContent().setLimit(sizeLimit);
            }
        }
    }

    /**
     * @return this analysis order.
     */
    @Override
    public int order() {
        return 2;
    }
}
