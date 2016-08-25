package org.talend.dataprep.parameters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

class TextItem implements Item {

    private final String value;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Parameter> parameters;

    TextItem(String value, List<Parameter> parameters) {
        this.value = value;
        this.parameters = parameters;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String getLabel() {
        return value;
    }
}
