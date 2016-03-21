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

package org.talend.dataprep.transformation.api.action.metadata.common;

import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

/**
 * Post action context used for post action executions (pipeline etc...)
 */
public enum PostProcessParameters
{

    // TODO ideally we'd like to be able to pass a collection of ids
    OTHER_COLUMN_ID(ParameterType.STRING, "columns.modified");

    /** The parameter. */
    private final Parameter parameter;

    /**
     * Constructor.
     *
     * @param type type of parameter.
     * @param defaultValue the parameter default value.
     */
    PostProcessParameters(final ParameterType type, final String defaultValue) {
        this.parameter = new Parameter(this.name().toLowerCase(), type, defaultValue, true);
    }

    /**
     * @return the parameter key.
     */
    public String getKey() {
        return parameter.getName().toLowerCase();
    }

    /**
     * @return the actual parameter.
     */
    public Parameter getParameter() {
        return parameter;
    }

}
