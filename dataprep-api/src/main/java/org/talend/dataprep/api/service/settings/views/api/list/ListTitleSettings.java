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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List items title (main property) settings
 */
@JsonInclude(NON_NULL)
public class ListTitleSettings {

    /**
     * The item property that represents the title display mode
     */
    private String displayModeKey;

    /**
     * The item property that represents the icon name
     */
    private String iconKey;

    /**
     * The item property that represents the title
     */
    private String key;

    /**
    * The key to access to the item persistent actions
    */
    private String persistentActionsKey;

    /**
     * The title click action identifier
     */
    private String onClick;

    /**
     * The title edition cancel action identifier
     */
    private String onEditCancel;

    /**
     * The title edition submit action identifier
     */
    private String onEditSubmit;


    public String getDisplayModeKey() {
        return displayModeKey;
    }

    public void setDisplayModeKey(String displayModeKey) {
        this.displayModeKey = displayModeKey;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getPersistentActionsKey() {
        return persistentActionsKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public void setPersistentActionsKey(String persistentActionsKey) {
        this.persistentActionsKey = persistentActionsKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    public String getOnEditCancel() {
        return onEditCancel;
    }

    public void setOnEditCancel(String onEditCancel) {
        this.onEditCancel = onEditCancel;
    }

    public String getOnEditSubmit() {
        return onEditSubmit;
    }

    public void setOnEditSubmit(String onEditSubmit) {
        this.onEditSubmit = onEditSubmit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String displayModeKey;

        private String iconKey;

        private String persistentActionsKey;

        private String key;

        private String onClick;

        private String onEditCancel;

        private String onEditSubmit;

        public Builder displayModeKey(final String displayModeKey) {
            this.displayModeKey = displayModeKey;
            return this;
        }

        public Builder iconKey(final String iconKey) {
            this.iconKey = iconKey;
            return this;
        }

        public Builder persistentActionsKey(final String persistentActionsKey) {
            this.persistentActionsKey = persistentActionsKey;
            return this;
        }

        public Builder key(final String key) {
            this.key = key;
            return this;
        }

        public Builder onClick(final String onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder onEditCancel(final String onEditCancel) {
            this.onEditCancel = onEditCancel;
            return this;
        }

        public Builder onEditSubmit(final String onEditSubmit) {
            this.onEditSubmit = onEditSubmit;
            return this;
        }

        public ListTitleSettings build() {
            final ListTitleSettings settings = new ListTitleSettings();
            settings.setDisplayModeKey(this.displayModeKey);
            settings.setIconKey(this.iconKey);
            settings.setPersistentActionsKey(this.persistentActionsKey);
            settings.setKey(this.key);
            settings.setOnClick(this.onClick);
            settings.setOnEditCancel(this.onEditCancel);
            settings.setOnEditSubmit(this.onEditSubmit);
            return settings;
        }

    }
}
