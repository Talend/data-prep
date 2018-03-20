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

package org.talend.dataprep.api.service.settings.views.api.breadcrumb;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Breadcrumb settings
 * see
 * https://talend.github.io/react-talend-components/?selectedKind=Breadcrumbs&selectedStory=default&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
public class BreadcrumbSettings implements ViewSettings {

    public static final String VIEW_TYPE = TYPE_BREADCRUMB;

    /**
     * The key in view dictionary
     */
    @JsonIgnore
    private String id;

    /**
     * Max number of items before ellipsis
     */
    private int maxItems;

    /**
     * Item click action identifier
     */
    private String onItemClick;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ViewSettings translate() {
        return BreadcrumbSettings
                .from(this) //
                .translate() //
                .build();
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

    public static BreadcrumbSettings.Builder from(final BreadcrumbSettings viewSettings) {
        return builder() //
                .id(viewSettings.getId()) //
                .maxItems(viewSettings.getMaxItems()) //
                .onItemClick(viewSettings.getOnItemClick());
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

        public Builder translate() {
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
