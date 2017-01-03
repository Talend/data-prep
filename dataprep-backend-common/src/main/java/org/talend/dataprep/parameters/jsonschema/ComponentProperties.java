/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.parameters.jsonschema;

/**
 * Representation of dynamic form structure.
 */
public class ComponentProperties {

    /**
     * Schema that describe the structure of the properties object.
     */
    private JsonSchema jsonSchema;

    /**
     * Data container describe by the schema. Can contains any structure of data but will be used by TComp to recreate a
     * {@link org.talend.daikon.properties.Properties} object.
     */
    private PropertiesData properties;

    /**
     * Front-end behavior description.
     */
    private UiSchema uiSchema;

    public JsonSchema getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public PropertiesData getProperties() {
        return properties;
    }

    public void setProperties(PropertiesData properties) {
        this.properties = properties;
    }

    public UiSchema getUiSchema() {
        return uiSchema;
    }

    public void setUiSchema(UiSchema uiSchema) {
        this.uiSchema = uiSchema;
    }

}
