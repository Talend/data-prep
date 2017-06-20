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

package org.talend.dataprep.api.service.settings.documentation.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 *
 */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class DocumentationSettings {
    /**
     * The documentation property id
     */
    private String id;

    /**
     * The documentation property value
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
    public static DocumentationSettings.Builder builder() {
        return new DocumentationSettings.Builder();
    }

    public static DocumentationSettings.Builder from(final DocumentationSettings documentationSettings) {
        return builder() //
                .id(documentationSettings.getId()) //
                .value(documentationSettings.getValue());
    }

    public static class Builder {

        private String id;

        private String value;

        public DocumentationSettings.Builder id(String id) {
            this.id = id;
            return this;
        }

        public DocumentationSettings.Builder value(final String value) {
            this.value = value;
            return this;
        }

        public DocumentationSettings build() {
            final DocumentationSettings documentationSettings = new DocumentationSettings();
            documentationSettings.setId(this.id);
            documentationSettings.setValue(this.value);
            return documentationSettings;
        }
    }
}