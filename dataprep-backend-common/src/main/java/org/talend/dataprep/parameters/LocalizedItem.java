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

package org.talend.dataprep.parameters;

import java.util.List;
import java.util.Locale;

import org.talend.dataprep.i18n.ActionsBundle;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Models a select item.
 */
public class LocalizedItem implements Item {

    /** the item value. */
    private final String value;

    /** The optional inline parameter. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Parameter> parameters;

    /** the item label. */
    private String label;

    private Object parent;

    /**
     * Create a select Item. The item's label will be by default looked up with key ("choice." + value).
     *
     * @param value the item value.
     * @param parameters the item optional parameters.
     */
    LocalizedItem(String value, String label, List<Parameter> parameters) {
        this.value = value;
        this.label = label;
        this.parameters = parameters;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public Item attach(Object parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public String getLabel() {
        return ActionsBundle.INSTANCE.choice(parent, Locale.ENGLISH, label);
    }

}
