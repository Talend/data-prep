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

package org.talend.dataprep.exception.json;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.exception.ErrorCodeDto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module that deals with ErrorCode.
 * <bold>WARN:</bold> all anum ErrorCodes are serialized as a String field as their name.
 *
 * @see ErrorCode
 */
@Component
public class ErrorCodeModule extends SimpleModule {

    public ErrorCodeModule() {
        super(ErrorCodeModule.class.getName(), new Version(1, 0, 0, null, null, null));
        addDeserializer(ErrorCode.class, new Deserializer());
    }

    private static class Deserializer extends StdDeserializer<ErrorCode> {

        Deserializer() {
            super(ErrorCode.class);
        }

        @Override
        public ErrorCode deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            return jsonParser.readValueAs(ErrorCodeDto.class);
        }
    }
}
