//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON;
import static org.talend.dataprep.schema.csv.CSVFormatGuess.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Utility class for CSV format handling.
 */
@Component
public class CSVFormatUtils {

    /**
     * Dataprep ready jackson builder.
     */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    /**
     * Retrieve properties (separator, header and the headerNbLines) associated with a dataset content and put them in a
     * map with their corresponding key.
     *
     * @param separator the separator object detected by the CSVFormatGuesser
     * @return a map associating to header and separator parameters their corresponding value
     */
    public Map<String, String> compileSeparatorProperties(Separator separator) {
        return compileSeparatorProperties( //
                String.valueOf(separator.getSeparator()), //
                separator.getHeaders().keySet().stream().collect(Collectors.toList()), //
                separator.isFirstLineAHeader() ? 1 : 0 //
        );
    }

    /**
     * @param parameters the dataset format parameters.
     * @return the list of the dataset header or an empty list of an error occurs while computing the headers.
     */
    public List<String> retrieveHeader(Map<String, String> parameters) {
        List<String> header;
        try {
            String jsonMap = parameters.get(HEADER_COLUMNS_PARAMETER);
            header = builder.build().readValue(jsonMap, new TypeReference<List<String>>() {});
        } catch (Exception e) { // NOSONAR no need to log or throw the exception here
            return Collections.emptyList();
        }
        return header;
    }

    /**
     * Retrieve properties (separator, header and the headerNbLines) associated with a dataset content and put them in a
     * map with their corresponding key.
     *  @param separator     the specified separator
     * @param header        the specified header
     * @param headerNbLines the specified number of lines spanned by the header
     */
    private Map<String, String> compileSeparatorProperties(String separator, List<String> header, int headerNbLines) {
        Map<String, String> parameters = new HashMap<>();
        // separator
        parameters.put(SEPARATOR_PARAMETER, separator);
        String jsonHeader;
        try {
            jsonHeader = builder.build().writeValueAsString(header);
        } catch (Exception e) {
            throw new TDPException(UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
        parameters.put(HEADER_COLUMNS_PARAMETER, jsonHeader);
        // nb lines of header
        parameters.put(HEADER_NB_LINES_PARAMETER, Integer.toString(headerNbLines));
        return parameters;
    }

    /**
     * Use the separator from the metadata
     *
     * @param updated  the dataset metadata to use.
     */
    public void useNewSeparator(DataSetMetadata updated) {

        final Map<String, String> newParameters = updated.getContent().getParameters();
        final String newSeparator = newParameters.get(SEPARATOR_PARAMETER);
        final String newHeaderLines = newParameters.get(HEADER_NB_LINES_PARAMETER);
        List<String> newHeaders = Collections.emptyList();

        // update the metadata with the new parameters
        final Map<String, String> parameters = compileSeparatorProperties( //
                newSeparator, //
                newHeaders, //
                Integer.valueOf(newHeaderLines) //
        );
        updated.getContent().setParameters(parameters);
    }
}
