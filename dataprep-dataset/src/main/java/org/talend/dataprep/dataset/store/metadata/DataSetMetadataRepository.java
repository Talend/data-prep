package org.talend.dataprep.dataset.store.metadata;

import javax.annotation.Nullable;

import org.talend.dataprep.DistributedLock;
import org.talend.dataprep.api.dataset.DataSetMetadata;

public interface DataSetMetadataRepository {

    String DATASET_LOCK_PREFIX = "dataset#"; //$NON-NLS-1$

    /**
     * @return A {@link java.lang.Iterable iterable} of {@link DataSetMetadata data set}. Returned data set are expected
     * to be visible by current user.
     */
    Iterable<DataSetMetadata> list();

    /**
     * <p>
     * Creates a new {@link DataSetMetadata data set}. If a previous one already exists, it will be updated by this
     * operation.
     * </p>
     * <p>
     * <b>However</b>, if a previous data set exists but the current user has no write rights on it, an exception should
     * be thrown.
     * </p>
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to create or update.
     */
    void add(DataSetMetadata dataSetMetadata);

    /**
     * <p>
     * Removes all {@link DataSetMetadata data sets} in this repository. Repository does not provide rollback operation
     * for this, use it with care!
     * </p>
     * <p>
     * Please note this methods only removes data set the current user has write access on.
     * </p>
     */
    void clear();

    /**
     * @return The number of {@link DataSetMetadata data sets} the current user can see.
     */
    int size();

    /**
     * Returns the {@link DataSetMetadata data set} with given id.
     * 
     * @param id A data set id.
     * @return The {@link DataSetMetadata} with given <code>id</code> or null if non found.
     */
    @Nullable
    DataSetMetadata get(String id);

    /**
     * Removes the {@link DataSetMetadata data set} with given id.
     * 
     * @param id The id of the {@link DataSetMetadata data set} to delete.
     * @see DataSetMetadata#getId()
     */
    void remove(String id);

    /**
     * create a general cluster lock for the given DatasetMetadata ID This is up to the client to use it or not and of
     * course to release it.
     * 
     * @param id of the metadata to get the lock on.
     * @return the lock instance to be used for locking and unlocking metadata access.
     */
    DistributedLock createDatasetMetadataLock(String id);

}
