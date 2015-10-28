package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

public enum ImplicitParameters {

    COLUMN_ID("column_id", ParameterType.STRING, EMPTY),

    ROW_ID("row_id", ParameterType.STRING, EMPTY),

    SCOPE("scope", ParameterType.STRING, EMPTY),

    FILTER("filter", ParameterType.FILTER, EMPTY);

    private final Parameter parameter;

    ImplicitParameters(final String key, final ParameterType type, final String defaultValue) {
        this.parameter = new Parameter(key, type, defaultValue, true);
    }

    public String getKey() {
        return parameter.getName();
    }

    public Parameter getParameter() {
        return parameter;
    }

    public static List<Parameter> getParameters() {
        return Arrays.stream(values()) //
                .map(ImplicitParameters::getParameter) //
                .collect(Collectors.toList());
    }
}
