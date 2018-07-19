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

package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON;
import static org.talend.dataprep.schema.csv.CSVFormatFamily.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for CSV format handling.
 */
@Component
public class CSVFormatUtils {

    /**
     * Dataprep ready jackson builder.
     */
    @Autowired
    private ObjectMapper mapper;

    /** The default enclosure character. */
    @Value("${default.import.text.enclosure:\"}")
    private String defaultTextEnclosure;

    /** The default escape character. */
    @Value("${default.import.text.escape:\u0000}")
    private String defaultEscapeChar;

    /**
     * @param parameters the dataset format parameters.
     * @return the list of the dataset header or an empty list of an error occurs while computing the headers.
     */
    List<String> retrieveHeader(Map<String, String> parameters) {
        List<String> header;
        try {
            String jsonMap = parameters.get(HEADER_COLUMNS_PARAMETER);
            header = mapper.readValue(jsonMap, new TypeReference<List<String>>() {
            });
        } catch (Exception e) { // NOSONAR no need to log or throw the exception here
            return Collections.emptyList();
        }
        return header;
    }

    /**
     * Retrieve properties associated with a dataset content and put them in a
     * map with their corresponding key.
     *
     * @param separator the specified separator
     * @param updatedParameters the updated header
     *
     */
    Map<String, String> compileParameterProperties(Separator separator, Map<String, String> updatedParameters) {

        List<String> header = separator.getHeaders().stream().map(p -> p.getKey()).collect(Collectors.toList());
        // header
        String jsonHeader;
        try {
            jsonHeader = mapper.writeValueAsString(header);
        } catch (Exception e) {
            throw new TDPException(UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
        updatedParameters.put(HEADER_COLUMNS_PARAMETER, jsonHeader);

        // separator
        updatedParameters.put(SEPARATOR_PARAMETER, String.valueOf(separator.getSeparator()));

        // if no parameter set set take the default one
        updatedParameters.putIfAbsent(TEXT_ENCLOSURE_CHAR, defaultTextEnclosure);
        updatedParameters.putIfAbsent(ESCAPE_CHAR, defaultEscapeChar);

        return updatedParameters;
    }
}
