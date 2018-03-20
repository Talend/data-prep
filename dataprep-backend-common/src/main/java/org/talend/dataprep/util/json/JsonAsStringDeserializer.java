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

package org.talend.dataprep.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Keeps json value as json, does not try to deserialize it
 *
 */
public class JsonAsStringDeserializer extends JsonDeserializer<String> {

    public  static final JsonAsStringDeserializer INSTANCE= new JsonAsStringDeserializer();

    @Override
    public String deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}