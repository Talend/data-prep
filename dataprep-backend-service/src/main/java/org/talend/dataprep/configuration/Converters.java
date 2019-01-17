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

package org.talend.dataprep.configuration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
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
                if (source.isEmpty()) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                            new IllegalArgumentException("Source should not be empty"));
                }
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readTree(source);
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            }
        };
    }

}
