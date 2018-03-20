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

package org.talend.dataprep.parameters;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.i18n.ActionsBundle.parameterDescription;
import static org.talend.dataprep.i18n.ActionsBundle.parameterLabel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bean that models action parameter.
 */
public class Parameter implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The parameter name. */
    private String name;

    /** The parameter type. */
    private String type;

    /** The parameter default value. */
    private String defaultValue;

    /** True if the parameter is not displayed to the user. */
    private boolean implicit;

    /** True if the parameter can be blank. */
    private boolean canBeBlank;

    /** Provides a hint to user on how to fill parameter (e.g "http://" for a url, "mm/dd/yy" for a date). */
    private String placeHolder;

    /** The configuration. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> configuration = new HashMap<>();

    private String label;

    private String description;

    /** If the field is editable or not. */
    private Boolean readonly;

    public Parameter() {
    }

    protected Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit, final boolean canBeBlank, String placeHolder, String label, String description,
                        Boolean readonly) {
        this.name = name;
        this.placeHolder = placeHolder;
        this.type = type == null ? null : type.asString();
        this.defaultValue = defaultValue;
        this.implicit = implicit;
        this.canBeBlank = canBeBlank;
        this.label = label;
        this.description = description;
        this.readonly = readonly;
    }

    public static ParameterBuilder parameter(Locale locale) {
        return new ParameterBuilder(locale);
    }

    public static Parameter generateParameter(Locale locale, String name, ParameterType type, String defaultValue, Boolean implicit, Boolean canBeBlank, Action action) {
        return Parameter
                .parameter(locale) //
                .setName(name) //
                .setType(type) //
                .setDefaultValue(defaultValue) //
                .setImplicit(implicit) //
                .setCanBeBlank(canBeBlank) //
                .build(action);
    }

    void addConfiguration(String name, Object configuration) {
        this.configuration.put(name, configuration);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefault() {
        return defaultValue;
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public boolean isCanBeBlank() {
        return canBeBlank;
    }

    public void setCanBeBlank(boolean canBeBlank) {
        this.canBeBlank = canBeBlank;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return implicit == parameter.implicit && canBeBlank == parameter.canBeBlank && Objects.equals(name, parameter.name)
                && Objects.equals(type, parameter.type) && Objects.equals(defaultValue, parameter.defaultValue) && Objects.equals(
                placeHolder, parameter.placeHolder) && Objects.equals(configuration, parameter.configuration) && Objects.equals(
                label, parameter.label) && Objects.equals(description, parameter.description) && Objects.equals(readonly, parameter.readonly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, implicit, canBeBlank, placeHolder, configuration, label, description, readonly);
    }

    public static class ParameterBuilder {

        private static final Logger LOGGER = getLogger(ParameterBuilder.class);

        private String name;

        private ParameterType type;

        private String defaultValue = null;

        private boolean implicit = false;

        private boolean canBeBlank = true;

        private String placeHolder = EMPTY;

        private String label;

        private String description;

        private Locale locale;

        /** If the field is readonly or not (null means 'can be edited'). */
        private Boolean readonly = null;

        private ParameterBuilder(Locale locale) {
            this.locale = locale;
        }

        public ParameterBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ParameterBuilder setType(ParameterType type) {
            this.type = type;
            return this;
        }

        public ParameterBuilder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ParameterBuilder setDefaultValue(Boolean defaultValue) {
            this.defaultValue = defaultValue.toString();
            return this;
        }

        public ParameterBuilder setImplicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        public ParameterBuilder setCanBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        public ParameterBuilder setPlaceHolder(String placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public ParameterBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public ParameterBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ParameterBuilder setReadonly(boolean readonly) {
            this.readonly = readonly;
            return this;
        }

        // for now we still are forced to auto detect label and description parameters but if it is possible to do this
        // without binding parameter builder with the I18n mechanism it would be really great, hence the warnings
        public Parameter build(Object action) {
            if (label == null) {
                LOGGER.debug("Warning: implicit label in [{}] parameter creation.", name);
                try {
                    label = parameterLabel(action, locale, name);
                } catch (Exception e) {
                    // If no label can be auto-found, we really do not care
                    LOGGER.trace("Error while auto-finding label parameter for [{}].", name);
                }
            }
            if (description == null) {
                LOGGER.debug("Warning: implicit description in [{}] parameter creation.", name);
                try {
                    description = parameterDescription(action, locale, name);
                } catch (Exception e) {
                    // If no description can be auto-found, we really do not care
                    LOGGER.trace("Error while auto-finding description parameter for [{}].", name);
                }
            }
            return new Parameter(name, type, defaultValue, implicit, canBeBlank, placeHolder, label, description, readonly);
        }
    }
}
