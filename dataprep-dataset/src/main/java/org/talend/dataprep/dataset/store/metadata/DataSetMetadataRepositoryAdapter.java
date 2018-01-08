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

package org.talend.dataprep.dataset.store.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.lock.LockFactory;

/**
 * Base class for all DataSetMetadataRepository implementation.
 */
public abstract class DataSetMetadataRepositoryAdapter implements DataSetMetadataRepository {

    /** The lock factory. */
    @Autowired
    private LockFactory lockFactory;

    /**
     * @see DataSetMetadataRepository#createDatasetMetadataLock(String)
     */
    @Override
    public DistributedLock createDatasetMetadataLock(String id) {
        return lockFactory.getLock(LockFactory.DATASET_LOCK_PREFIX + id);
    }

}
