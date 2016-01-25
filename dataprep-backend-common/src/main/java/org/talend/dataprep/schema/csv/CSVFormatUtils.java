package org.talend.dataprep.schema.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class CSVFormatUtils {

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
        Map<String, String> parameters = new HashMap<>();
        // separator
        parameters.put(CSVFormatGuess.SEPARATOR_PARAMETER, String.valueOf(separator.getSeparator()));
        // header
        List<String> columns = separator.getHeaders().keySet().stream().collect(Collectors.toList());
        String header = null;
        try {
            header = builder.build().writeValueAsString(columns);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
        parameters.put(CSVFormatGuess.HEADER_COLUMNS_PARAMETER, header);
        // nb lines of header
        parameters.put(CSVFormatGuess.HEADER_NB_LINES_PARAMETER, separator.isFirstLineAHeader() ? "1" : "0");
        return parameters;
    }

    /**
     * Retrieve properties (separator, header and the headerNbLines) associated with a dataset content and put them in a
     * map with their corresponding key.
     * 
     * @param separator the specified separator
     * @param header the specified header
     * @param headerNbLines the specified number of lines spanned by the header
     */
    public Map<String, String> compileSeparatorProperties(String separator, List<String> header, int headerNbLines) {
        Map<String, String> parameters = new HashMap<>();
        // separator
        parameters.put(CSVFormatGuess.SEPARATOR_PARAMETER, separator);
        String jsonHeader = null;
        try {
            jsonHeader = builder.build().writeValueAsString(header);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
        parameters.put(CSVFormatGuess.HEADER_COLUMNS_PARAMETER, jsonHeader);
        // nb lines of header
        parameters.put(CSVFormatGuess.HEADER_NB_LINES_PARAMETER, headerNbLines + "");
        return parameters;
    }

    /**
     *
     * @param parameters
     * @return
     */
    public List<String> retrieveHeader(Map<String, String> parameters) {
        List<String> header = null;
        try {
            String jsonMap = parameters.get(CSVFormatGuess.HEADER_COLUMNS_PARAMETER);
            header = builder.build().readValue(jsonMap, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
        return header;
    }

    /**
     * Uses the <tt>separator</tt>, <tt>header</tt>, and <tt>headerNbLines</tt> to reset the parameters of a given
     * metadata.
     * 
     * @param dataSetMetadata the specified dataset metadata
     * @param separator the specified separator
     * @param header the specified header
     * @param headerNbLines the specified number of lines spanned by the header
     */
    public void resetParameters(DataSetMetadata dataSetMetadata, String separator, List<String> header, int headerNbLines) {
        dataSetMetadata.getContent().setNbLinesInHeader(headerNbLines);
        dataSetMetadata.getContent().setParameters(compileSeparatorProperties(separator, header, headerNbLines));
    }
}
