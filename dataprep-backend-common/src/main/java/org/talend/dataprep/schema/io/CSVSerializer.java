package org.talend.dataprep.schema.io;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.schema.CSVFormatGuess;
import org.talend.dataprep.schema.Serializer;

import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Service("serializer#csv")
public class CSVSerializer implements Serializer {

    @Override
    public InputStream serialize(InputStream rawContent, DataSetMetadata metadata) {
        try {
            final Map<String, String> parameters = metadata.getContent().getParameters();
            final String separator = parameters.get(CSVFormatGuess.SEPARATOR_PARAMETER);
            CSVReader reader = new CSVReader(new InputStreamReader(rawContent), separator.charAt(0));
            StringWriter writer = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(writer);
            reader.readNext(); // Skip column names

            generator.writeStartArray();
            writeLineContent(reader, metadata, generator, separator);
            generator.writeEndArray();

            generator.flush();
            return new ByteArrayInputStream(writer.toString().getBytes("UTF-8")); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
    }

    /**
     * Write the line content.
     *
     * @param reader the csv reader to use as data source.
     * @param metadata the dataset metadata to use to get the columns.
     * @param generator the json generator used to actually write the line content.
     * @param separator the csv separator to use.
     * @throws IOException if an error occurs.
     */
    private void writeLineContent(CSVReader reader, DataSetMetadata metadata, JsonGenerator generator, String separator)
            throws IOException {
        String[] line;

        while ((line = reader.readNext()) != null) {

            // skip empty lines
            if (line.length == 1 && StringUtils.isEmpty(line[0])) {
                continue;
            }

            List<ColumnMetadata> columns = metadata.getRow().getColumns();
            generator.writeStartObject();
            int columnsSize = columns.size();
            for (int i = 0; i < columnsSize; i++) {
                ColumnMetadata columnMetadata = columns.get(i);

                generator.writeFieldName(columnMetadata.getId());

                // deal with additional content (line.length > columns.size)
                if (i == columnsSize - 1 && line.length > columnsSize) {
                    String additionalContent = getRemainingColumns(line, i, separator);
                    generator.writeString(additionalContent);
                }
                // deal with fewer content (line.length < columns.size)
                else if (i < line.length && line[i] != null) {
                    generator.writeString(line[i]);
                }
                // deal with null
                else {
                    generator.writeNull();
                }
            }
            generator.writeEndObject();
        }
    }

    /**
     * Return the remaining raw (with separators) content of the column.
     *
     * @param line the line to parse.
     * @param start where to start in the line.
     * @param separator the separator to append.
     * @return the remaining raw (with separators) content of the column.
     */
    private String getRemainingColumns(String[] line, int start, String separator) {
        StringBuilder buffer = new StringBuilder();
        for (int j = start; j < line.length; j++) {
            buffer.append(line[j]);
            if (j < line.length - 1) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }
}
