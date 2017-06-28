// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.preparation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.transformation.actions.Providers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * A custom deserializer to get all action definition based on JSON content (and class field value).
 */
public class ActionDefinitionDeserializer extends JsonDeserializer<List<ActionDefinition>> {

    @Override
    public List<ActionDefinition> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        List<ActionDefinition> actions = new ArrayList<>();
        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
            final TreeNode tree = jsonParser.readValueAsTree();
            for (int i = 0; i < tree.size(); i++) {
                try {
                    final String className = ((TextNode) tree.get(i).get("class")).asText();
                    final Class<?> clazz = Class.forName(className);
                    final ActionDefinition actionInstance = Providers.get((Class<? extends ActionDefinition>) clazz);
                    actions.add(actionInstance);
                } catch (Exception e) {
                    throw new TalendRuntimeException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
                }
            }
        }
        return actions;
    }
}
