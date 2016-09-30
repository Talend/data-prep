package org.talend.dataprep.transformation.service.dtos;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

public class Parameter {

    private static final Logger log = LoggerFactory.getLogger(Parameter.class);

    private String name;

    private ParameterType type;

    private String defaultValue;

    private boolean implicit;

    private boolean canBeBlank;

    private String placeHolder;

    private String description;

    private String label;

    private Configuration configuration;

    public static Parameter from(Locale locale, org.talend.dataprep.parameters.Parameter parameter) {
        Parameter p = new Parameter();
        p.setName(parameter.getName());
        p.setLabel(parameter.getLabel());
        p.setCanBeBlank(parameter.isCanBeBlank());
        p.setDefault(parameter.getDefault());
        p.setDescription(parameter.getDescription());
        p.setConfiguration(buildConfiguration(locale, parameter.getConfiguration()));
        p.setImplicit(parameter.isImplicit());
        p.setPlaceHolder(parameter.getPlaceHolder());
        p.setType(parameter.getType());
        return p;
    }

    private static Configuration buildConfiguration(Locale locale, Map<String, Object> configuration) {
        Configuration c = new Configuration();
        c.setFilters((List<String>) configuration.get("filter"));
        Boolean multiple = (Boolean) configuration.get("multiple");
        c.setMultiple(multiple == null ? false : multiple);
        List<SelectParameter.Item> values = (List<SelectParameter.Item>) configuration.get("values");
        if (values != null) {
            c.setValues(values.stream().map(i -> Item.from(locale, i)).collect(Collectors.toList()));
        }
        return c;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type.name().toLowerCase();
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public void setType(String type) {
        try{
            this.type = ParameterType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Trying to use a nonexistent parameter type " + type, e);
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Parameter parameter = (Parameter) o;
        return implicit == parameter.implicit &&
                canBeBlank == parameter.canBeBlank &&
                Objects.equals(name, parameter.name) &&
                type == parameter.type &&
                Objects.equals(defaultValue, parameter.defaultValue) &&
                Objects.equals(placeHolder, parameter.placeHolder) &&
                Objects.equals(description, parameter.description) &&
                Objects.equals(label, parameter.label) &&
                Objects.equals(configuration, parameter.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, implicit, canBeBlank, placeHolder, description, label, configuration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("type", type)
                .add("defaultValue", defaultValue)
                .add("implicit", implicit)
                .add("canBeBlank", canBeBlank)
                .add("placeHolder", placeHolder)
                .add("description", description)
                .add("label", label)
                .add("configuration", configuration)
                .toString();
    }
}
