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

package org.talend.dataprep.api.service.settings.views.api.list;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List sort menu settings, placed on the toolbar
 */
@JsonInclude(NON_NULL)
public class ListSortSettings {

    /**
     * The list of selectable option. Each option should contains an id (the field key to base the sort on) and a label that will
     * be displayed
     */
    private List<Map<String, String>> options;

    /**
     * The option select action identifier
     */
    private String onChange;

    public List<Map<String, String>> getOptions() {
        return options;
    }

    public void setOptions(final List<Map<String, String>> options) {
        this.options = options;
    }

    public String getOnChange() {
        return onChange;
    }

    public void setOnChange(final String onChange) {
        this.onChange = onChange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final String ID = "id";

        private static final String NAME = "name";

        private List<Map<String, String>> options = new ArrayList<>();

        private String onChange;

        public Builder options(final String id, final String name) {
            final Map<String, String> keyValue = new HashMap<>(2);
            keyValue.put(ID, id);
            keyValue.put(NAME, name);
            this.options.add(keyValue);
            return this;
        }

        public Builder onChange(final String onChange) {
            this.onChange = onChange;
            return this;
        }

        public ListSortSettings build() {
            final ListSortSettings settings = new ListSortSettings();
            settings.setOptions(this.options);
            settings.setOnChange(this.onChange);
            return settings;
        }
    }
}
