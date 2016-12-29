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

package org.talend.dataprep.api.service.settings.views.api.list;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.talend.dataprep.api.service.settings.views.api.actionsbar.ActionsBarSettings;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ToolbarDetailsSettings {
    private ListSortSettings sort;
    private ActionsBarSettings actionBar;
    private String searchLabel;

    public ListSortSettings getSort() {
        return sort;
    }

    public void setSort(final ListSortSettings sort) {
        this.sort = sort;
    }

    public ActionsBarSettings getActionBar() {
        return actionBar;
    }

    public void setActionBar(final ActionsBarSettings actionBar) {
        this.actionBar = actionBar;
    }

    public String getSearchLabel() {
        return searchLabel;
    }

    public void setSearchLabel(final String searchLabel) {
        this.searchLabel = searchLabel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ListSortSettings sort;
        private ActionsBarSettings actionBar;
        private String searchLabel;

        public Builder sort(final ListSortSettings sort) {
            this.sort = sort;
            return this;
        }

        public Builder actionBar(final ActionsBarSettings actionBar) {
            this.actionBar = actionBar;
            return this;
        }

        public Builder searchLabel(final String searchLabel) {
            this.searchLabel = searchLabel;
            return this;
        }

        public ToolbarDetailsSettings build() {
            final ToolbarDetailsSettings settings = new ToolbarDetailsSettings();
            settings.setSort(this.sort);
            settings.setActionBar(this.actionBar);
            settings.setSearchLabel(this.searchLabel);
            return settings;
        }

    }
}
