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
 * List items extra properties settings
 */
@JsonInclude(NON_NULL)
public class ListItemsSettings {

    /**
     * The item property key to get the classname
     */
    private String classNameKey;

    public String getClassNameKey() {
        return classNameKey;
    }

    public void setClassNameKey(String classNameKey) {
        this.classNameKey = classNameKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String classNameKey;

        public Builder classNameKey(final String classNameKey) {
            this.classNameKey = classNameKey;
            return this;
        }

        public ListItemsSettings build() {
            final ListItemsSettings settings = new ListItemsSettings();
            settings.setClassNameKey(this.classNameKey);
            return settings;
        }

    }
}
