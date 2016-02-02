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

package org.talend.dataprep.preparation.store;

import java.util.Collection;

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

    void add(Identifiable object);

    <T extends Identifiable> T get(String id, Class<T> clazz);

    /**
     * @param dataSetId the wanted dataset id.
     * @return all preparations used by a dataset or an empty list if there's none.
     */
    Collection<Preparation> getByDataSet(String dataSetId);

    <T extends Identifiable> Collection<T> listAll(Class<T> clazz);

    void clear();

    void remove(Identifiable object);
}
