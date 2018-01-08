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

package org.talend.dataprep.transformation.actions.common;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.i18n.ActionsBundle.parameterDescription;
import static org.talend.dataprep.i18n.ActionsBundle.parameterLabel;
import static org.talend.dataprep.parameters.ParameterType.STRING;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;

/**
 * Common implicit parameters used by nearly all actions.
 */
public enum ImplicitParameters {

    COLUMN_ID(STRING, EMPTY),
    ROW_ID(STRING, EMPTY),
    SCOPE(STRING, EMPTY),
    FILTER(ParameterType.FILTER, EMPTY);

    private final ParameterType type;

    private final String defaultValue;

    /**
     * Constructor.
     *
     * @param type type of parameter.
     * @param defaultValue the parameter default value.
     */
    ImplicitParameters(final ParameterType type, final String defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * @return the full list of implicit parameters.
     * @param locale
     */
    public static List<Parameter> getParameters(Locale locale) {
        return Arrays.stream(values()).map(ip -> ip.getParameter(locale)).collect(Collectors.toList());
    }

    /**
     * @return the parameter key.
     */
    public String getKey() {
        return name().toLowerCase();
    }

    /**
     * @return the actual parameter.
     * @param locale
     */
    public Parameter getParameter(Locale locale) {
        return Parameter
                .parameter(locale)
                .setName(name().toLowerCase())
                .setType(type)
                .setDefaultValue(defaultValue)
                .setImplicit(true)
                .setLabel(parameterLabel(null, locale, name().toLowerCase()))
                .setDescription(parameterDescription(null, locale, name().toLowerCase()))
                .build(this);
    }
}
