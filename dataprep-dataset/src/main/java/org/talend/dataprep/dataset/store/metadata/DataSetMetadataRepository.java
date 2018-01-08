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

import java.util.Collections;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

/**
 * Interface for all DatasetMetadata repository implementations.
 */
public interface DataSetMetadataRepository {

    /**
     * Returns <code>true</code> if at least one {@link DataSetMetadata} matches given filter.
     * @param filter A TQL filter (i.e. storage-agnostic)
     * @return <code>true</code> if at least one {@link DataSetMetadata} matches <code>filter</code>.
     */
    boolean exist(String filter);

    /**
     * @return A {@link java.lang.Iterable iterable} of {@link DataSetMetadata data set}. Returned data set are expected
     * to be visible by current user.
     */
    Stream<DataSetMetadata> list();

    /**
     * Returns an {@link Iterable} of all {@link DataSetMetadata} that match given filter.
     *
     * @param filter A TQL filter (i.e. storage-agnostic)
     * @param sortField An optional field to be used to sort results (may be <code>null</code> to indicate no sort).
     * @param sortDirection An optional field to be used how to sort results (may be <code>null</code> to indicate no sort). Valid
     * values are "ASC" or "DESC".
     * @return A {@link Iterable} of {@link DataSetMetadata} that matches <code>filter</code>.
     */
    Stream<DataSetMetadata> list(String filter, Sort sortField, Order sortDirection);

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
    void save(DataSetMetadata dataSetMetadata);

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

    /**
     * Returns data set that are in this repository which are compatible with the data set of the specified id.
     * @param id the id of a data set
     * @return A {@link java.lang.Iterable iterable} of {@link DataSetMetadata data set}.
     */
    default Iterable<DataSetMetadata> listCompatible(String id) {
        final DataSetMetadata metadata = get(id);
        if (metadata == null) {
            return Collections.emptyList();
        }
        final Stream<DataSetMetadata> stream = list().filter(m -> m != null && !metadata.equals(m) && metadata.compatible(m));
        return stream::iterator;
    }

    /**
     * Counts and returns the total size of all data sets.
     *
     * @return the total size of all data sets
     */
    long countAllDataSetsSize();
}
