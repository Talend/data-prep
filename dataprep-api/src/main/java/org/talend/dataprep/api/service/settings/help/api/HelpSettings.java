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

package org.talend.dataprep.api.service.settings.help.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 *
 */
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public class HelpSettings {
    /**
     * The help property id
     */
    private String id;

    /**
     * The help property value
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
    public static HelpSettings.Builder builder() {
        return new HelpSettings.Builder();
    }

    public static HelpSettings.Builder from(final HelpSettings helpSettings) {
        return builder() //
                .id(helpSettings.getId()) //
                .value(helpSettings.getValue());
    }

    public static class Builder {

        private String id;

        private String value;

        public HelpSettings.Builder id(String id) {
            this.id = id;
            return this;
        }

        public HelpSettings.Builder value(final String value) {
            this.value = value;
            return this;
        }

        public HelpSettings build() {
            final HelpSettings helpSettings = new HelpSettings();
            helpSettings.setId(this.id);
            helpSettings.setValue(this.value);
            return helpSettings;
        }
    }
}
