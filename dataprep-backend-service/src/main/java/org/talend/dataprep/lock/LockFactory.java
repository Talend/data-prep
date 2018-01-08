// ============================================================================
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

package org.talend.dataprep.lock;

/**
 * Factory used to generate locks. This class is used to generate prototype DistributedLock.
 */
public interface LockFactory {

    /** Prefix for the shared lock when working on a dataset. */
    String DATASET_LOCK_PREFIX = "dataset#";

    /**
     * @param id An id for the distributed lock. It is up to the caller to decide any naming rules or for uniqueness of
     * id.
     * @return A {@link DistributedLock lock} to perform platform wide lock operations.
     */
    DistributedLock getLock(String id);

}
