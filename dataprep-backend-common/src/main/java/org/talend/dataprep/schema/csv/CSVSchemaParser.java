package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

import au.com.bytecode.opencsv.CSVReader;

@Service("parser#csv")
public class CSVSchemaParser implements SchemaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVSchemaParser.class);

    private static final String META_KEY = "key";

    @Autowired
    protected CSVFormatUtils csvFormatUtils;

    /**
     *
     * @param request container with information needed to parse the raw data.
     * @return
     */
    @Override
    public SchemaParserResult parse(Request request) {
        List<SchemaParserResult.SheetContent> sheetContents = new ArrayList<>();
        sheetContents.add(new SchemaParserResult.SheetContent(META_KEY, new ArrayList<>()));
        try {
            final DataSetMetadata metadata = request.getMetadata();
            final Map<String, String> parameters = metadata.getContent().getParameters();
            final char separator = parameters.get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            // Map<String, String> header = retrieveHeader(parameters);
            List<String> header = csvFormatUtils.retrieveHeader(parameters);

            if (header == null || header.isEmpty()) {
                try (CSVReader reader = new CSVReader(new InputStreamReader(request.getContent(), metadata.getEncoding()), separator)) {
                    String[] columns = reader.readNext();
                    if (columns == null) { // Empty content?
                        return SchemaParserResult.Builder.parserResult() //
                                .sheetContents(sheetContents).build();
                    } else {
                        throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT);
                    }
                }
            }
            LOGGER.debug("Columns found: {}", header);
            // By default, consider all columns as Strings (to be refined by deeper analysis).
            LOGGER.debug("Setting default type for columns...");
            int i = 0;
            for (String column : header) {
                sheetContents.stream().filter(sheetContent -> META_KEY.equals(sheetContent.getName())).findFirst() //
                        .get().getColumnMetadatas() //
                        .add(column().id(i++).name(column).type(Type.STRING).build());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        }
        return SchemaParserResult.Builder.parserResult() //
                .sheetContents(sheetContents) //
                .draft(false).build();
    }

    /**
     * Parses the ten first lines and naively guess the data type of columns. TODO: it guesses according to the last
     * line read instead of using all ten lines.
     *
     * @param reader the csv reader.
     * @param sheetContents the sheet content.
     * @throws IOException if an error occurs.
     */
    private void detectHeader(CSVReader reader, List<SchemaParserResult.SheetContent> sheetContents) throws IOException {

        // Best guess (and naive) on data types
        String[] line;
        int lineNumber = 0;
        // Performs naive check on the 10 first lines (quite time consuming to parse all for a naive guess).
        while ((line = reader.readNext()) != null && lineNumber < 10) {
            for (int i = 0; i < line.length; i++) {
                String columnValue = line[i];
                Optional<SchemaParserResult.SheetContent> content = sheetContents.stream() //
                        .filter(sheetContent -> META_KEY.equals(sheetContent.getName())) //
                        .findFirst();

                List<ColumnMetadata> columns = content.get().getColumnMetadatas();
                try {
                    Integer.parseInt(columnValue);

                    // in case there are more columns that in the header
                    if (content.isPresent() && columns.size() > i) {
                        columns.get(i).setType(Type.INTEGER.getName());
                    }
                } catch (NumberFormatException e) {
                    // Not a number
                }
                if (("true".equalsIgnoreCase(columnValue.trim()) || "false".equalsIgnoreCase(columnValue.trim()))
                        && columns.size() > i) {
                    columns.get(i).setType(Type.BOOLEAN.getName());
                }
            }
            lineNumber++;
        }
    }
}