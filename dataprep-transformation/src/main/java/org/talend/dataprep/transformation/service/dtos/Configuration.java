package org.talend.dataprep.transformation.service.dtos;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class Configuration {

    private List<String> filters;

    private Boolean multiple;

    private List<Item> values;

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<Item> getValues() {
        return values;
    }

    public void setValues(List<Item> values) {
        this.values = values;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Configuration that = (Configuration) o;
        return Objects.equals(filters, that.filters) &&
                Objects.equals(multiple, that.multiple) &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, multiple, values);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("filters", filters)
                .add("multiple", multiple)
                .add("values", values)
                .toString();
    }
}
