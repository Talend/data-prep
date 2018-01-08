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
 * List details settings.
 * This configures the list items display information.
 */
@JsonInclude(NON_NULL)
public class ListDetailsSettings {

    /**
     * The columns to display. Each column configuration should have a key (the item property key) and a label (the property
     * display label).
     */
    private List<Map> columns;

    /**
     * Items extra properties
     */
    private ListItemsSettings itemProps;

    /**
     * Sort configuration
     */
    private ListSortSettings sort;

    /**
     * Display configuration
     */
    private ListDisplaySettings display;

    /**
     * Items title (main property) configuration
     */
    private ListTitleSettings titleProps;

    public List<Map> getColumns() {
        return columns;
    }

    public void setColumns(final List<Map> columns) {
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

    public ListDisplaySettings getDisplay() {
        return display;
    }

    public void setDisplay(ListDisplaySettings display) {
        this.display = display;
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

        private static final String HIDE_HEADER = "hideHeader";

        private final List<Map> columns = new ArrayList<>();

        private ListItemsSettings itemProps;

        private ListSortSettings sort;

        private ListDisplaySettings display;

        private ListTitleSettings titleProps;

        public Builder column(final String key, final String label) {
            this.column(key, label, null, null);
            return this;
        }

        public Builder column(final String key, final String label, final Boolean hideHeader, final String type) {
            final Map keyValue = new HashMap<>(2);
            keyValue.put(KEY, key);
            keyValue.put(LABEL, label);
            if(hideHeader != null) {
                keyValue.put(HIDE_HEADER, hideHeader);
            }
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

        public Builder display(final ListDisplaySettings display) {
            this.display = display;
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
            settings.setDisplay(this.display);
            settings.setTitleProps(this.titleProps);
            return settings;
        }
    }
}
