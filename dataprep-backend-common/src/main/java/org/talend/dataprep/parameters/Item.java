// ============================================================================
//
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

package org.talend.dataprep.parameters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Models a select item.
 */
public class Item {

    /** the item value. */
    private final String value;

    /** The optional inline parameter. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Parameter> parameters;

    /** the item label. */
    private final String label;

    /**
     * Create a select Item.
     *
     * @param value the item value.
     * @param label the item label.
     * @param parameters the item optional parameters.
     */
    public Item(String value, String label, List<Parameter> parameters) {
        this.value = value;
        this.label = label;
        this.parameters = parameters;
    }

    public String getValue() {
        return value;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getLabel() {
        return label;
    }

}
