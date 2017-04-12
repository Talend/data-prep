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

package org.talend.dataprep.api.service.settings.uris.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Uris settings
 *
 * */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class UriSettings {

    /**
     * The uri display name
     */
    private String name;

    /**
     * The action uri that will appears next to the name
     */
    private String uri;

    /**
     * Getters & Setters
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Builder Pattern for immutable purpose
     */

    public static Builder from(final UriSettings actionSettings) {
        return builder()
                .name(actionSettings.getName())
                .uri(actionSettings.getUri());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String uri;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder uri(final String uri) {
            this.uri = uri;
            return this;
        }

        public UriSettings build() {
            final UriSettings action = new UriSettings();
            action.setName(this.name);
            action.setUri(this.uri);
            return action;
        }
    }
}
