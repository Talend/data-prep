package org.talend.dataprep.dataset.service.analysis;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.DistributedLock;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;

@Component
public class SynchronousAnalysisEnd implements SynchronousDataSetAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(ContentAnalysis.class);

    @Autowired
    DataSetMetadataRepository repository;

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void analyze(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }
        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata != null) {
                metadata.getLifecycle().importing(false);
                LOG.info("Finished content import of data set #{}.", dataSetId);
                repository.add(metadata);
            } else {
                LOG.info("Data set #{} no longer exists.", dataSetId); //$NON-NLS-1$
            }
        } finally {
            datasetLock.unlock();
        }

    }
}
