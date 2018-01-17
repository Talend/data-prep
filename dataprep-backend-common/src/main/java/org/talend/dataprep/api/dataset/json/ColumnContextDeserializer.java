// ============================================================================
//
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

package org.talend.dataprep.api.dataset.json;

import java.io.IOException;
import java.util.List;

import org.talend.dataprep.api.dataset.ColumnMetadata;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ColumnContextDeserializer extends JsonDeserializer<List<ColumnMetadata>> {

    @Override
    public List<ColumnMetadata> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        final List<ColumnMetadata> columnMetadata = oc.readValue(jsonParser, new TypeReference<List<ColumnMetadata>>() {
        });
        deserializationContext.setAttribute(ColumnContextDeserializer.class.getName(), columnMetadata);
        return columnMetadata;
    }
}
