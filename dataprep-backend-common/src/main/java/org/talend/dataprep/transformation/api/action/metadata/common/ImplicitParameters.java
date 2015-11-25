package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

/**
 * Common implicit parameters used by nearly all actions.
 */
public enum ImplicitParameters {

                                COLUMN_ID(STRING, EMPTY),
                                ROW_ID(STRING, EMPTY),
                                SCOPE(STRING, EMPTY),
                                FILTER(ParameterType.FILTER, EMPTY);

    /** The paramter. */
    private final Parameter parameter;

    /**
     * Constructor.
     *
     * @param type type of parameter.
     * @param defaultValue the parameter default value.
     */
    ImplicitParameters(final ParameterType type, final String defaultValue) {
        this.parameter = new Parameter(this.name().toLowerCase(), type, defaultValue, true);
    }

    /**
     * @return the parameter key.
     */
    public String getKey() {
        return parameter.getName().toLowerCase();
    }

    /**
     * @return the actual parameter.
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * @return the full list of implicit parameters.
     */
    public static List<Parameter> getParameters() {
        return Arrays.stream(values()) //
                .map(ImplicitParameters::getParameter) //
                .collect(Collectors.toList());
    }
}
