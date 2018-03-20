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

package org.talend.dataprep.async.repository;

import java.util.stream.Stream;

import org.talend.dataprep.async.AsyncExecution;

/**
 * Defines the managed task repository interface. This interface only stores information relative to executions (i.e. it does not
 * manage scheduling, cancel...).
 *
 */
public interface ManagedTaskRepository {

    /**
     * Return the managed task from the given id.
     *
     * @param id the wanted managed task id.
     * @return the managed task from the given id.
     */
    AsyncExecution get(String id);

    /**
     * Return the managed tasks that belongs to the groupId
     *
     * @param groupId the group id.
     * @return all the managed tasks.
     */
    Stream<AsyncExecution> list(final String groupId);

    /**
     * @return all the managed tasks.
     */
    Stream<AsyncExecution> list();

    /**
     * Remove the managed task from the given id.
     *
     * @param id the managed task id to remove.
     */
    void remove(String id);

    /**
     * Save a managed task in the repository.
     *
     * @param asyncExecution the taks to save.
     */
    void save(AsyncExecution asyncExecution);

    /**
     * Clear the whole repository.
     */
    // TODO replace this test method by a utility test class.
    void clear();
}
