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

package org.talend.dataprep.parameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.ActionsBundle;

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
    private  String placeHolder;

    /** The configuration. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> configuration;

    private Object parent;

    public Parameter() {
    }

    /**
     * Minimal default constructor.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     */
    public Parameter(String name, ParameterType type) {
        this(name, type, null, false);
    }

    /**
     * Constructor with a default value.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     * @param defaultValue the parameter default value.
     */
    public Parameter(String name, ParameterType type, String defaultValue) {
        this(name, type, defaultValue, false);
    }

    /**
     * Constructor with a default value and the implicit flag.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     * @param defaultValue the parameter default value.
     * @param implicit true if the parameter is implicit.
     */
    public Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit) {
        this(name, type, defaultValue, implicit, true);
    }

    /**
     * Full constructor.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     * @param defaultValue the parameter default value.
     * @param implicit true if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     */
    public Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit,
            final boolean canBeBlank) {
        this(name, type, defaultValue, implicit, canBeBlank, StringUtils.EMPTY);
    }

    public Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit,
            final boolean canBeBlank, String placeHolder) {
        this.name = name;
        this.placeHolder = placeHolder;
        this.type = type.asString();
        this.defaultValue = defaultValue;
        this.implicit = implicit;
        this.canBeBlank = canBeBlank;
        this.configuration = new HashMap<>();
    }

    void addConfiguration(String name, Object configuration) {
        this.configuration.put(name, configuration);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return ActionsBundle.INSTANCE.parameterLabel(parent, Locale.ENGLISH, getName());
    }

    public String getDescription() {
        return ActionsBundle.INSTANCE.parameterDescription(parent, Locale.ENGLISH, getName());
    }

    public String getType() {
        return type;
    }

    public String getDefault() {
        return defaultValue;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public boolean isCanBeBlank() {
        return canBeBlank;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public Parameter attach(Object parent) {
        this.parent = parent;
        return this;
    }
}
