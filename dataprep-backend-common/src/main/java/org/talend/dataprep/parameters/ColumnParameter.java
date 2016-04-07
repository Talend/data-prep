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

package org.talend.dataprep.parameters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Parameter that should be displayed as a select box with columns in the UI.
 */
public class ColumnParameter extends Parameter {

    /** Filters */
    @JsonIgnore // will be part of the Parameter#configuration
    private List<String> filters;

    /**
     * Full constructor.
     *
     * @param name The parameter name.
     * @param defaultValue The parameter default value.
     * @param implicit True if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     * @param filters List of filtered column types.
     */
    public ColumnParameter(String name, String defaultValue, boolean implicit, boolean canBeBlank, List<String> filters) {
        super(name, ParameterType.COLUMN, defaultValue, implicit, canBeBlank);
        setFilters(filters);
    }

    /**
     * @return the Filters
     */
    public List<String> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set.
     */
    public void setFilters(List<String> filters) {
        this.filters = filters;
        addConfiguration("filters", filters);
    }

}
