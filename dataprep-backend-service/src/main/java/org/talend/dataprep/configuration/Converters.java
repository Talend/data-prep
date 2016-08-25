package org.talend.dataprep.configuration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class Converters {

    private static final Logger LOGGER = LoggerFactory.getLogger(Converters.class);

    @Bean
    public Converter<String, JsonNode> jsonNodeConverter() {
        // Don't convert to lambda -> cause issue for Spring to infer source and target types.
        return new Converter<String, JsonNode>() {
            @Override
            public JsonNode convert(String source) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readTree(source);
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            }
        };
    }

    @Bean
    public Converter<String, ErrorCode> errorCodeConverter() {
        // Don't convert to lambda -> cause issue for Spring to infer source and target types.
        return new Converter<String, ErrorCode>() {
            @Override
            public ErrorCode convert(String source) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readerFor(JsonErrorCode.class).readValue(source);
                } catch (Exception e) {
                    LOGGER.debug("Unable to read error code from '{}'", source, e);
                    return CommonErrorCodes.UNEXPECTED_EXCEPTION;
                }
            }
        };
    }

}
