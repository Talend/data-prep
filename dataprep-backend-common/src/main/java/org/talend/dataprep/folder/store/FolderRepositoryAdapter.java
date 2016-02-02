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

package org.talend.dataprep.folder.store;

import javax.inject.Inject;

import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.lock.LockFactory;

public abstract class FolderRepositoryAdapter implements FolderRepository {

    /** Prefix for the shared lock when working on a Folder. */
    private static final String FOLDER_LOCK_PREFIX = "dataset#"; //$NON-NLS-1$

    /** The lock factory. */
    @Inject
    private LockFactory lockFactory;

    /**
     * @see FolderRepository#createFolderLock(String)
     */
    @Override
    public DistributedLock createFolderLock(String id) {
        return lockFactory.getLock(FOLDER_LOCK_PREFIX + id);
    }

}
