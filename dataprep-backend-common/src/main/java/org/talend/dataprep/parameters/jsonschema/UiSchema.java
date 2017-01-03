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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Representation of a ui schema. For now it is only an empty extension of ObjectNode but it has the advantages of: typing and
 * avoid a too direct dependency on jackson.
 */
public class UiSchema extends ObjectNode {

    public UiSchema(JsonNodeFactory nc) {
        super(nc);
    }

    public UiSchema(JsonNodeFactory nc, Map<String, JsonNode> kids) {
        super(nc, kids);
    }
}
