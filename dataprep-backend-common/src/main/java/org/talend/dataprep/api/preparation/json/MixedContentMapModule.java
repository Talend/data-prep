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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module that deals with MixedContentMap.
 * 
 * @see MixedContentMap
 */
@Component
public class MixedContentMapModule extends SimpleModule {

    /**
     * Default empty constructor.
     */
    public MixedContentMapModule() {
        super(MixedContentMapModule.class.getName(), new Version(1, 0, 0, null, null, null));
        addSerializer(MixedContentMap.class, new Serializer());
        addDeserializer(MixedContentMap.class, new Deserializer());
    }

    /**
     * Serialize MixedContentMap to json.
     */
    private class Serializer extends JsonSerializer<MixedContentMap> {

        /**
         * @see JsonSerializer#serialize(Object, JsonGenerator, SerializerProvider)
         */
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
                } else if (value.charAt(0) == '{' || value.charAt(0) == '[') {
                    // check that it's a real json array or object
                    try {
                        JsonNode node = new ObjectMapper().reader().readTree(value);

                        // Must add endsWith tests, because jackson parser stops parsing at first ']' when json starts
                        // with a '[' (means values such as '[]anything' are consider as valid array by jackson parser)
                        boolean realJson = false;
                        if (value.charAt(0) == '{') {
                            // case json object:
                            realJson = node.isContainerNode() && value.endsWith("}");
                        } else {
                            // case json array:
                            realJson = node.isArray() && value.endsWith("]");
                        }

                        if (realJson) {
                            jsonGenerator.writeRawValue(value);
                        } else {
                            // otherwise, it is written as a string (may be a regular expression, e.g. [A-Za-z0-9]*)
                            jsonGenerator.writeString(value);
                        }
                    }
                    // otherwise, it is written as a string (may be a regular expression, e.g. [A-Za-z0-9]*)
                    catch (IOException ioe) {
                        jsonGenerator.writeString(value);
                    }
                } else {
                    jsonGenerator.writeString(value);
                }
            }
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Deserialize MixedContentMap to MixedContentMap.
     */
    private class Deserializer extends JsonDeserializer<MixedContentMap> {

        /**
         * @see JsonDeserializer#deserialize(JsonParser, DeserializationContext)
         */
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
                    } else if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
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
