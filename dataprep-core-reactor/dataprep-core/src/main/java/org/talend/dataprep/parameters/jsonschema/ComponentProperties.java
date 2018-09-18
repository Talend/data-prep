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

package org.talend.dataprep.parameters.jsonschema;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Representation of dynamic form structure.
 */
public class ComponentProperties {

    /**
     * Schema that describe the structure of the properties object.
     */
    private ObjectNode jsonSchema;

    /**
     * Data container describe by the schema. Can contains any structure of data but will be used by TComp to recreate a
     * {@link org.talend.daikon.properties.Properties} object.
     */
    private ObjectNode properties;

    /**
     * Front-end behavior description.
     */
    private ObjectNode uiSchema;

    public ObjectNode getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(ObjectNode jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public ObjectNode getProperties() {
        return properties;
    }

    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    public ObjectNode getUiSchema() {
        return uiSchema;
    }

    public void setUiSchema(ObjectNode uiSchema) {
        this.uiSchema = uiSchema;
    }

}
