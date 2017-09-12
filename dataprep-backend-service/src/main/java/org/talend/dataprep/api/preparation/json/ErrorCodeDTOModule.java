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

package org.talend.dataprep.api.preparation.json;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.ErrorCodeDto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module that deals with ErrorCodeDto.
 *
 * @see ErrorCodeDto
 */
@Component
public class ErrorCodeDTOModule extends SimpleModule {

    public ErrorCodeDTOModule() {
        super(ErrorCodeDTOModule.class.getName(), new Version(1, 0, 0, null, null, null));
        addSerializer(ErrorCodeDto.class, new Serializer());
    }

    /**
     * Serialize ErrorCodeDto to json.
     */
    private class Serializer extends JsonSerializer<ErrorCodeDto> {

        @Override
        public void serializeWithType(ErrorCodeDto value, JsonGenerator gen, SerializerProvider serializers,
                TypeSerializer typeSer) throws IOException {
            gen.writeStartArray();
            gen.writeString(value.getCode());
            gen.writeEndArray();
        }

        @Override
        public void serialize(ErrorCodeDto errorCode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeStartArray();
            jsonGenerator.writeString(errorCode.getCode());
            jsonGenerator.writeEndArray();
        }
    }

}
