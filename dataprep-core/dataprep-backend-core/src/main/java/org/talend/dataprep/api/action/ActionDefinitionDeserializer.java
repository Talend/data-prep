// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import java.io.IOException;

public class ActionDefinitionDeserializer extends JsonDeserializer<ActionDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionDefinitionDeserializer.class);

    @Override
    public ActionDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        final TreeNode treeNode = jp.readValueAsTree();

        final TreeNode nodeName = treeNode.get("name");
        if (nodeName instanceof TextNode) {
            final String name = ((TextNode) nodeName).asText();
            return Providers.get(ActionRegistry.class).get(name);
        } else {
            LOGGER.error("No action available for: {}.", treeNode);
            return null;
        }
    }
}
