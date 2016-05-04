// ============================================================================
//
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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;

/**
 * Base interface for preparation repositories (mongodb & in memory).
 *
 * This repository manage both Preparation and Step, hence the use of Idenfiable.
 *
 * @see Preparation
 * @see Step
 */
public interface PreparationRepository {

    /**
     * Save or update an identifiable object.
     * @param object the identifiable to save.
     */
    void add(Identifiable object);

    /**
     * Returns the Identifiable that matches the id and the class or null if none match.
     * @param id the wanted Identifiable id.
     * @param clazz the wanted Identifiable class.
     * @param <T> the type of Identifiable.
     * @return the Identifiable that matches the id and the class or null if none match.
     */
    <T extends Identifiable> T get(String id, Class<T> clazz);

    /**
     * @param dataSetId the wanted dataset id.
     * @return all preparations used by a dataset or an empty list if there's none.
     */
    Collection<Preparation> getByDataSet(String dataSetId);

    /**
     * Returns all the preparations that match the specified name on an exact match whether <i>exactMatch</i> is
     * <tt>true</tt>. Otherwise it returns all the preparations having their names containing the specified name.
     *
     * If <i>name</i> is null or empty it returns the list of all preparations.
     * 
     * @param name the specified name
     * @param exactMatch the specified boolean
     * @return all the preparations having their names either matching <i>name</i> or containing <i>name</i>according to
     * specified <i>exactMatch</i>
     */
    /**
     * @see PreparationRepository#getByMatchingName(String, boolean)
     */
    default Collection<Preparation> getByMatchingName(String name, boolean exactMatch) {
        Collection<Preparation> result;
        if (StringUtils.isEmpty(name)) {
            result = listAll(Preparation.class);
        } else {
            if (exactMatch) {
                result = listAll(Preparation.class) //
                        .stream() //
                        .filter(preparation -> StringUtils.equalsIgnoreCase(name, preparation.getName())) //
                        .collect(Collectors.toList()); //
            } else {
                result = listAll(Preparation.class) //
                        .stream() //
                        .filter(preparation -> StringUtils.containsIgnoreCase(preparation.getName(),name)) //
                        .collect(Collectors.toList()); //

            }
        }
        return result;
    }

    <T extends Identifiable> Collection<T> listAll(Class<T> clazz);

    void clear();

    void remove(Identifiable object);
}
