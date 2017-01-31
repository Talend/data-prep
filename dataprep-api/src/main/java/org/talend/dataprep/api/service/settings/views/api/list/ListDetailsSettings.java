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

package org.talend.dataprep.api.service.settings.views.api.list;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List details settings.
 * This configures the list items display information.
 */
@JsonInclude(NON_NULL)
public class ListDetailsSettings {

    /**
     * The columns to display. Each column configuration should have a key (the item property key) and a label (the property
     * display label).
     */
    private List<Map<String, String>> columns;

    /**
     * Items extra properties
     */
    private ListItemsSettings itemProps;

    /**
     * Sort configuration
     */
    private ListSortSettings sort;

    /**
     * Items title (main property) configuration
     */
    private ListTitleSettings titleProps;

    public List<Map<String, String>> getColumns() {
        return columns;
    }

    public void setColumns(final List<Map<String, String>> columns) {
        this.columns = columns;
    }

    public ListItemsSettings getItemProps() {
        return itemProps;
    }

    public void setItemProps(final ListItemsSettings itemProps) {
        this.itemProps = itemProps;
    }

    public ListSortSettings getSort() {
        return sort;
    }

    public void setSort(ListSortSettings sort) {
        this.sort = sort;
    }

    public ListTitleSettings getTitleProps() {
        return titleProps;
    }

    public void setTitleProps(final ListTitleSettings titleProps) {
        this.titleProps = titleProps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final String KEY = "key";

        private static final String LABEL = "label";

        private static final String TYPE = "type";

        private final List<Map<String, String>> columns = new ArrayList<>();

        private ListItemsSettings itemProps;

        private ListSortSettings sort;

        private ListTitleSettings titleProps;

        public Builder column(final String key, final String label) {
           this.column(key, label, null);
            return this;
        }

        public Builder column(final String key, final String label, final String type) {
            final Map<String, String> keyValue = new HashMap<>(2);
            keyValue.put(KEY, key);
            keyValue.put(LABEL, label);
            if(type != null) {
                keyValue.put(TYPE, type);
            }
            this.columns.add(keyValue);
            return this;
        }

        public Builder itemProps(final ListItemsSettings itemProps) {
            this.itemProps = itemProps;
            return this;
        }

        public Builder sort(final ListSortSettings sort) {
            this.sort = sort;
            return this;
        }

        public Builder titleProps(final ListTitleSettings titleProps) {
            this.titleProps = titleProps;
            return this;
        }

        public ListDetailsSettings build() {
            final ListDetailsSettings settings = new ListDetailsSettings();
            settings.setColumns(this.columns);
            settings.setItemProps(this.itemProps);
            settings.setSort(this.sort);
            settings.setTitleProps(this.titleProps);
            return settings;
        }

    }
}
