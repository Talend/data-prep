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

package org.talend.dataprep.api.service.settings.views.api.breadcrumb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class BreadcrumbSettings implements ViewSettings {
    public static final String VIEW_TYPE = "Breadcrumb";

    @JsonIgnore
    private String id;
    private int maxItems;
    private String onItemClick;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public String getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(String onItemClick) {
        this.onItemClick = onItemClick;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private int maxItems;
        private String onItemClick;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder maxItems(final int maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public Builder onItemClick(final String onItemClick) {
            this.onItemClick = onItemClick;
            return this;
        }

        public BreadcrumbSettings build() {
            final BreadcrumbSettings settings = new BreadcrumbSettings();
            settings.setId(this.id);
            settings.setMaxItems(this.maxItems);
            settings.setOnItemClick(this.onItemClick);
            return settings;
        }

    }
}
