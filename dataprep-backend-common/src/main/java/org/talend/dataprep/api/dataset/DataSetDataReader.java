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

package org.talend.dataprep.api.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;

/**
 * Utility class to read data set data as returned by data-prep.
 * It expect something of the form:
 * <pre>
 * {
 *     "metadata": {
 *          "columns": [
 *              {{@link ColumnMetadata}},
 *              ...
 *          ]
 *     },
 *     "records":[
 *          {
 *              "first column id":"first value",
 *              "second column id":"second value",
 *              ...
 *          },
 *          ...
 *      ]
 * }
 * </pre>
 * It will be read as stream by first reading metadata to extract columns metadata in the form of {@link RowMetadata} then,
 * records will be read and transformed into {@link  DataSetRow DataSetRows}.
 * <p>
 * <strong>Warning</strong> This class is strongly relying on exception to work: {@link IOException} and {@link IllegalArgumentException}
 * are used when malformed data or errors arise during parsing.
 * </p>
 */
public final class DataSetDataReader {

    private static final String INCORRECT_OBJECT_STRUCTURE_ERROR_MESSAGE = "Malformed lookup data from DataPrep server.";

    private ObjectMapper mapper;

    public DataSetDataReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Reads and Maps the data set from the specified input stream.
     * <p><strong>Does NOT close the supplied {@link InputStream}</strong></p>
     *
     * @param inputStream  the input stream containing the data set
     * @param joinOnColumn the column used to join the lookup data set
     * @return a map which associates to each value of the joint column its corresponding data set row
     * @throws IOException              In case of JSON exception related error.
     * @throws IllegalArgumentException If the input stream is not of the expected JSON structure.
     */
    public LightweightExportableDataSet parseAndMapLookupDataSet(InputStream inputStream, String joinOnColumn) throws IOException {
        Validate.isTrue(inputStream != null, "The provided input stream must not be null");

        try (JsonParser jsonParser = mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .getFactory()
                .createParser(inputStream)) {
            LightweightExportableDataSet lookupDataset = new LightweightExportableDataSet();
            RowMetadata rowMetadata = new RowMetadata();

            JsonToken currentToken = jsonParser.nextToken();
            Validate.isTrue(currentToken == JsonToken.START_OBJECT, INCORRECT_OBJECT_STRUCTURE_ERROR_MESSAGE);

            while (currentToken != JsonToken.END_OBJECT && !jsonParser.isClosed()) {
                currentToken = jsonParser.nextToken();
                String currentField = jsonParser.getCurrentName();
                if ("metadata".equalsIgnoreCase(currentField)) {
                    JsonToken metadataStartToken = jsonParser.nextToken(); // advance to start object
                    Validate.isTrue(metadataStartToken == JsonToken.START_OBJECT, INCORRECT_OBJECT_STRUCTURE_ERROR_MESSAGE);
                    rowMetadata = parseDataSetMetadataAndReturnRowMetadata(jsonParser);
                    lookupDataset.setMetadata(rowMetadata);
                } else if ("records".equalsIgnoreCase(currentField)) {
                    JsonToken recordsStartToken = jsonParser.nextToken(); // advance to start object
                    Validate.isTrue(recordsStartToken == JsonToken.START_ARRAY, INCORRECT_OBJECT_STRUCTURE_ERROR_MESSAGE);
                    lookupDataset.setRecords(parseRecords(jsonParser, rowMetadata, joinOnColumn));
                }
            }
            if (lookupDataset.isEmpty()) {
                throw new IOException("No lookup data has been retrieved when trying to parse the specified data set.");
            }
            return lookupDataset;

        }
    }

    private RowMetadata parseDataSetMetadataAndReturnRowMetadata(JsonParser jsonParser) throws IOException {
        try {
            JsonNode treeNode = jsonParser.readValueAsTree();
            JsonNode columns = treeNode.get("columns");
            List<ColumnMetadata> columnsParsed = new ArrayList<>();
            if (columns.isArray()) {
                for (int columnId = 0; columnId < columns.size(); columnId++) {
                    JsonNode column = columns.get(columnId);
                    columnsParsed.add(mapper.reader(ColumnMetadata.class).readValue(column));
                }
            }
            return new RowMetadata(columnsParsed);
        } catch (IOException e) {
            throw new IOException("Unable to parse and retrieve the row metadata", e);
        }
    }

    private  Map<String, Map<String, String>> parseRecords(JsonParser jsonParser, RowMetadata rowMetadata, String joinOnColumn)
            throws IOException {

        try {
            JsonToken firstToken = jsonParser.nextToken(); // Read to array first element
            Validate.isTrue(firstToken == JsonToken.START_OBJECT, INCORRECT_OBJECT_STRUCTURE_ERROR_MESSAGE);

            Map<String, Map<String, String>> lookupDataset = new HashMap<>();
            Iterator<Map<String, String>> mapIterator = jsonParser.readValuesAs(new TypeReference<Map<String, String>>() {});

            while (mapIterator.hasNext()) {
                Map<String, String> recordMap = mapIterator.next();
                lookupDataset.put(recordMap.get(joinOnColumn), recordMap);
            }

            Validate.isTrue(!lookupDataset.isEmpty(),
                    "No lookup record has been retrieved when trying to parse the retrieved data set.");

            return lookupDataset;
        } catch (IOException e) {
            throw new IOException("Unable to parse and retrieve the records of the data set", e);
        }
    }

}
