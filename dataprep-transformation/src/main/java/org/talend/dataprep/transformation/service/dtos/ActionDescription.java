package org.talend.dataprep.transformation.service.dtos;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public class ActionDescription {

    private String name;

    private String category;

    private String label;

    private String description;

    private String docUrl;

    private List<String> actionScope;

    private boolean dynamic;

    private boolean implicitFilter;

    private List<Parameter> parameters;

    public static ActionDescription from(Locale locale,
                                         org.talend.dataprep.transformation.actions.common.ActionMetadata actionMetadata) {
        ActionDescription lam = new ActionDescription();
        lam.setName(actionMetadata.getName());
        lam.setActionScope(actionMetadata.getActionScope());
        lam.setCategory(actionMetadata.getCategory());
        lam.setDescription(actionMetadata.getDescription());
        lam.setDocUrl(actionMetadata.getDocUrl());
        lam.setDynamic(actionMetadata.isDynamic());
        lam.setImplicitFilter(actionMetadata.implicitFilter());
        lam.setLabel(actionMetadata.getLabel());
        lam.setParameters(
                actionMetadata.getParameters().stream().map(p -> Parameter.from(locale, p)).collect(Collectors.toList()));
        return lam;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public List<String> getActionScope() {
        return actionScope;
    }

    public void setActionScope(List<String> actionScope) {
        this.actionScope = actionScope;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isImplicitFilter() {
        return implicitFilter;
    }

    public void setImplicitFilter(boolean implicitFilter) {
        this.implicitFilter = implicitFilter;
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
        ActionDescription that = (ActionDescription) o;
        return dynamic == that.dynamic &&
                implicitFilter == that.implicitFilter &&
                Objects.equals(name, that.name) &&
                Objects.equals(category, that.category) &&
                Objects.equals(label, that.label) &&
                Objects.equals(description, that.description) &&
                Objects.equals(docUrl, that.docUrl) &&
                Objects.equals(actionScope, that.actionScope) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, label, description, docUrl, actionScope, dynamic, implicitFilter, parameters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("category", category)
                .add("label", label)
                .add("description", description)
                .add("docUrl", docUrl)
                .add("actionScope", actionScope)
                .add("dynamic", dynamic)
                .add("implicitFilter", implicitFilter)
                .add("parameters", parameters)
                .toString();
    }
}
