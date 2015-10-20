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
