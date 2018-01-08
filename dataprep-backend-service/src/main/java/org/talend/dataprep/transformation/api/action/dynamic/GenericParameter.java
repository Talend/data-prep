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

package org.talend.dataprep.transformation.api.action.dynamic;

/**
 * Representation of a "Generic" Parameter.
 */
public class GenericParameter {

    /** Parameter type (item, parameter, cluster, ...). */
    final String type;

    /** Parameter details. this can be an array, a map, an object. */
    final Object details;

    /**
     * Default constructor.
     *
     * @param type the parameter type.
     * @param details the parameter details.
     */
    public GenericParameter(final String type, final Object details) {
        this.type = type;
        this.details = details;
    }

    /**
     * @return the parameter type.
     */
    public String getType() {
        return type;
    }

    /**
     * @return the parameter details.
     */
    public Object getDetails() {
        return details;
    }
}
