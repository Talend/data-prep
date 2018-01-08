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

package org.talend.dataprep.api.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LightweightExportableDataSetUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(LightweightExportableDataSetUtils.class);

    /**
     * Reads token of the specified JsonParser and returns a list of column metadata.
     *
     * @param jsonParser the jsonParser whose next tokens are supposed to represent a list of column metadata
     * @return The column metadata parsed from JSON parser.
     * @throws IOException In case of JSON exception related error.
     */
    private static List<ColumnMetadata> parseAnArrayOfColumnMetadata(JsonParser jsonParser) throws IOException {
        try {
            List<ColumnMetadata> columns = new ArrayList<>();
            // skip the array beginning [
            jsonParser.nextToken();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY && !jsonParser.isClosed()) {
                ColumnMetadata columnMetadata = jsonParser.readValueAs(ColumnMetadata.class);
                columns.add(columnMetadata);
            }
            if (columns.isEmpty()) {
                throw new IllegalArgumentException(
                        "No column metadata has been retrieved when trying to parse the retrieved data set.");
            }
            return columns;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the list of column metadata", e);
        }
    }

    private static RowMetadata parseDataSetMetadataAndReturnRowMetadata(JsonParser jsonParser) throws IOException {
        try {
            RowMetadata rowMetadata = null;
            while (jsonParser.nextToken() != JsonToken.END_OBJECT && !jsonParser.isClosed()) {
                String currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("columns", currentField)) {
                    rowMetadata = new RowMetadata(parseAnArrayOfColumnMetadata(jsonParser));
                }
            }
            LOGGER.debug("Skipping data to go back to the outer json object");
            while (jsonParser.getParsingContext().getParent().getCurrentName() != null && !jsonParser.isClosed()) {
                jsonParser.nextToken();
            }
            return rowMetadata;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the row metadata", e);
        }
    }

    private static LightweightExportableDataSet parseRecords(JsonParser jsonParser, RowMetadata rowMetadata, String joinOnColumn)
            throws IOException {
        try {
            LightweightExportableDataSet lookupDataset = new LightweightExportableDataSet();
            lookupDataset.setMetadata(rowMetadata);
            jsonParser.nextToken();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY && !jsonParser.isClosed()) {
                Map<String, String> values = jsonParser.readValueAs(Map.class);
                lookupDataset.addRecord(values.get(joinOnColumn), values);
            }
            if (lookupDataset.isEmpty()) {
                throw new IllegalArgumentException(
                        "No lookup record has been retrieved when trying to parse the retrieved data set.");
            }
            return lookupDataset;
        } catch (IOException e) {
            throw new IOExceptionWithCause("Unable to parse and retrieve the records of the data set", e);
        }
    }

    /**
     * Reads and Maps the data set from the specified input stream.
     *
     * @param inputStream the input stream containing the data set
     * @param joinOnColumn the column used to join the lookup data set
     * @return a map which associates to each value of the joint column its corresponding data set row
     * @throws IOException In case of JSON exception related error.
     */
    public static LightweightExportableDataSet parseAndMapLookupDataSet(InputStream inputStream, String joinOnColumn)
            throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("The provided input stream must not be null");
        }

        try (JsonParser jsonParser = mapper.getFactory().createParser(inputStream)) {
            LightweightExportableDataSet lookupDataset = new LightweightExportableDataSet();
            RowMetadata rowMetadata = new RowMetadata();

            while (jsonParser.nextToken() != JsonToken.END_OBJECT && !jsonParser.isClosed()) {
                String currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("metadata", currentField)) {
                    rowMetadata = parseDataSetMetadataAndReturnRowMetadata(jsonParser);
                }

                currentField = jsonParser.getCurrentName();
                if (StringUtils.equalsIgnoreCase("records", currentField)) {
                    lookupDataset = parseRecords(jsonParser, rowMetadata, joinOnColumn);
                }
            }
            if (lookupDataset.isEmpty()) {
                throw new IllegalArgumentException(
                        "No lookup data has been retrieved when trying to parse the specified data set .");
            }
            return lookupDataset;
        }
    }



}
