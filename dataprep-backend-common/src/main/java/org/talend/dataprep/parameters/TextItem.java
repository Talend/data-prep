package org.talend.dataprep.parameters;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TextItem implements Item {

    private final String value;

    private final String text;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Parameter> parameters;

    TextItem(String value, List<Parameter> parameters) {
        this(value, null, parameters);
    }

    public TextItem(String value, String text, List<Parameter> parameters) {
        this.value = value;
        this.text = text;
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
    public Item attach(Object parent) {
        return this;
    }

    @Override
    public String getLabel() {
        // Returns constant text if specified, value otherwise.
        return text == null ? value : text;
    }
}
