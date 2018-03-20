//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.service.analysis.synchronous;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.lock.DistributedLock;

/**
 * A special implementation of {@link SynchronousAnalysisEnd} that is always executed after all other implementations.
 * @see #order()
 */
@Component
public class SynchronousAnalysisEnd implements SynchronousDataSetAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronousAnalysisEnd.class);

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
                metadata.getLifecycle().setImporting(false);
                LOG.info("Finished content import of data set #{}.", dataSetId);
                repository.save(metadata);
            } else {
                LOG.info("Data set #{} no longer exists.", dataSetId); //$NON-NLS-1$
            }
        } finally {
            datasetLock.unlock();
        }

    }
}
