package org.talend.dataprep.transformation.service.dtos;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import org.talend.dataprep.parameters.SelectParameter;

public class Item {

    private String value;

    private String label;

    private List<Parameter> parameters;

    public static Item from(Locale locale, SelectParameter.Item item) {
        Item i = new Item();
        i.setLabel(item.getLabel());
        i.setValue(item.getValue());
        if (item.getInlineParameters() != null) {
            i.setParameters(item.getInlineParameters().stream().map(p -> Parameter.from(locale, p)).collect(Collectors.toList()));
        }
        return i;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Item item = (Item) o;
        return Objects.equals(value, item.value) &&
                Objects.equals(label, item.label) &&
                Objects.equals(parameters, item.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, label, parameters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("value", value).add("label", label).add("parameters", parameters).toString();
    }
}
