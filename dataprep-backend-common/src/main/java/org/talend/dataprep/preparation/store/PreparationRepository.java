// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.preparation.store;

import java.util.stream.Stream;

import org.talend.dataprep.api.preparation.Identifiable;

/**
 * Base interface for preparation repositories (mongodb & in memory).
 *
 * This repository manages both Preparation and Step, hence the use of {@link Identifiable}.
 *
 * @see org.talend.dataprep.api.preparation.Preparation
 * @see org.talend.dataprep.api.preparation.Step
 */
public interface PreparationRepository {

    /**
     * Returns <code>true</code> if at least one <code>clazz</code> matches given filter.
     *
     * @param clazz The class used for checking.
     * @param filter A TQL filter (i.e. storage-agnostic)
     * @return <code>true</code> if at least one <code>clazz</code> matches <code>filter</code>.
     */
    <T extends Identifiable> boolean exist(Class<T> clazz, String filter);

    /**
     * @return A {@link java.lang.Iterable iterable} of <code>clazz</code>.
     */
    <T extends Identifiable> Stream<T> list(Class<T> clazz);

    /**
     * @return A {@link java.lang.Iterable iterable} of <code>clazz</code> that match given <code>filter</code>.
     */
    <T extends Identifiable> Stream<T> list(Class<T> clazz, String filter);

    /**
     * Save or update an identifiable object.
     *
     * @param object the identifiable to save.
     */
    void add(Identifiable object);

    /**
     * Returns the Identifiable that matches the id and the class or null if none match.
     *
     * @param id the wanted Identifiable id.
     * @param clazz the wanted Identifiable class.
     * @param <T> the type of Identifiable.
     * @return the Identifiable that matches the id and the class or null if none match.
     */
    <T extends Identifiable> T get(String id, Class<T> clazz);

    /**
     * Removes all {@link Identifiable} stored in this repository.
     */
    void clear();

    /**
     * Removes the {@link Identifiable identifiable} from repository.
     *
     * @param object The {@link Identifiable identifiable} to be deleted (only {@link Identifiable#getId()} will be used for
     * delete).
     */
    void remove(Identifiable object);

}
