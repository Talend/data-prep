package org.talend.dataprep.api.preparation.json;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.MixedContentMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class MixedContentMapModule extends SimpleModule {

    public MixedContentMapModule() {
        super(MixedContentMapModule.class.getName(), new Version(1, 0, 0, null, null, null));
        addSerializer(MixedContentMap.class, new Serializer());
        addDeserializer(MixedContentMap.class, new Deserializer());
    }

    private static class Serializer extends JsonSerializer<MixedContentMap> {

        @Override
        public void serialize(MixedContentMap map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonGenerator.writeFieldName(entry.getKey());
                final String value = entry.getValue();
                if (value == null) {
                    jsonGenerator.writeNull();
                } else if (value.isEmpty()) {
                    jsonGenerator.writeString(StringUtils.EMPTY);
                } else if (value.charAt(0) == '{') {
                    jsonGenerator.writeRaw(':' + value);
                } else {
                    jsonGenerator.writeString(value);
                }
            }
            jsonGenerator.writeEndObject();
        }
    }

    private static class Deserializer extends JsonDeserializer<MixedContentMap> {

        @Override
        public MixedContentMap deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            final MixedContentMap map = new MixedContentMap();
            JsonToken token;
            String currentKey = null;
            while ((token = jsonParser.nextToken()) != null && token != JsonToken.END_OBJECT) {
                if (currentKey != null) {
                    if (token == JsonToken.VALUE_STRING) {
                        // Value is a string value, get value from underlying parser
                        map.put(currentKey, jsonParser.getValueAsString());
                    } else if (token == JsonToken.VALUE_NUMBER_INT) {
                        map.put(currentKey, String.valueOf(jsonParser.getValueAsInt()));
                    } else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                        map.put(currentKey, String.valueOf(jsonParser.getValueAsDouble()));
                    } else if (token == JsonToken.VALUE_TRUE || token == JsonToken.VALUE_FALSE) {
                        map.put(currentKey, String.valueOf(jsonParser.getValueAsBoolean()));
                    } else if (token == JsonToken.START_OBJECT) {
                        // Value is a JSON object, get content as is
                        map.put(currentKey, jsonParser.readValueAsTree().toString());
                        jsonParser.skipChildren();
                    } else if (token == JsonToken.VALUE_NULL) {
                        map.put(currentKey, null);
                    }
                    currentKey = null;
                } else if (token == JsonToken.FIELD_NAME) {
                    // New entry, keep it for put(...) calls
                    currentKey = jsonParser.getCurrentName();
                }
            }
            return map;
        }
    }
}
