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

package org.talend.dataprep.api.service.settings.context.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class ContextSettings {
    /**
     * The context property id
     */
    private String id;

    /**
     * The context property value
     */
    private String value;

    /**
     * Getters & Setters
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    /**
     * Builder Pattern for immutable purpose
     */
    public static ContextSettings.Builder builder() {
        return new ContextSettings.Builder();
    }

    public static ContextSettings.Builder from(final ContextSettings contextSettings) {
        return builder() //
                .id(contextSettings.getId()) //
                .value(contextSettings.getValue());
    }

    public static class Builder {

        private String id;

        private String value;

        public ContextSettings.Builder id(String id) {
            this.id = id;
            return this;
        }

        public ContextSettings.Builder value(final String value) {
            this.value = value;
            return this;
        }

        public ContextSettings build() {
            final ContextSettings contextSettings = new ContextSettings();
            contextSettings.setId(this.id);
            contextSettings.setValue(this.value);
            return contextSettings;
        }
    }
}
