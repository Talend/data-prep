/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.api.action;

import java.io.Serializable;
import java.util.List;

import org.talend.dataprep.parameters.Parameter;

/**
 * User-oriented representation of an action.
 */
public class ActionForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Action unique id. Non i18n. */
    private String name;

    // TODO: should be an enum value
    /** Action category unique id. Non i18n. */
    private String category;

    /** Action parameters to build the form. */
    private List<Parameter> parameters;

    /** List of the scope in which the action deal */
    private List<String> actionScope;

    private boolean isDynamic;

    /** Short description of what the action does. */
    private String description;

    private String alternateDescription;

    /** Action title. */
    private String label;

    private String alternateLabel;

    // TODO: should be an URL object
    /** Action documentation URL. */
    private String docUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlternateDescription() {
        return alternateDescription;
    }

    public void setAlternateDescription(String alternateDescription) {
        this.alternateDescription = alternateDescription;
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

    public String getAlternateLabel() {
        return alternateLabel;
    }

    public void setAlternateLabel(String alternateLabel) {
        this.alternateLabel = alternateLabel;
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
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }
}
