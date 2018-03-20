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

import org.talend.dataprep.api.service.settings.views.api.actionsbar.ActionsBarSettings;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List toolbar settings
 */
@JsonInclude(NON_NULL)
public class ToolbarDetailsSettings {

    /**
     * Sort menu settings
     */
    private ListSortSettings sort;

    /**
     * Display menu settings
     */
    private ListDisplaySettings display;

    /**
     * List of simple actions settings
     */
    private ActionsBarSettings actionBar;

    public ListSortSettings getSort() {
        return sort;
    }

    public ListDisplaySettings getDisplay() {
        return display;
    }

    public void setSort(final ListSortSettings sort) {
        this.sort = sort;
    }

    public void setDisplay(final ListDisplaySettings display) {
        this.display = display;
    }

    public ActionsBarSettings getActionBar() {
        return actionBar;
    }

    public void setActionBar(final ActionsBarSettings actionBar) {
        this.actionBar = actionBar;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(final ToolbarDetailsSettings viewSettings) {
        return builder().actionBar(viewSettings.getActionBar());
    }

    public static class Builder {

        private ListSortSettings sort;

        private ListDisplaySettings display;

        private ActionsBarSettings actionBar;

        public Builder sort(final ListSortSettings sort) {
            this.sort = sort;
            return this;
        }

        public Builder display(final ListDisplaySettings display) {
            this.display = display;
            return this;
        }

        public Builder actionBar(final ActionsBarSettings actionBar) {
            this.actionBar = actionBar;
            return this;
        }

        public ToolbarDetailsSettings build() {
            final ToolbarDetailsSettings settings = new ToolbarDetailsSettings();
            settings.setSort(this.sort);
            settings.setDisplay(this.display);
            settings.setActionBar(this.actionBar);
            return settings;
        }
    }
}
