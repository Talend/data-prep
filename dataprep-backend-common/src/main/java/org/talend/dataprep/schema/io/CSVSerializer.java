package org.talend.dataprep.schema.io;

import java.io.*;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.CSVFormatGuess;
import org.talend.dataprep.schema.Serializer;

import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Service("serializer#csv")
public class CSVSerializer implements Serializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(CSVSerializer.class);

    @Resource(name = "serializer#csv#executor")
    TaskExecutor executor;

    @Override
    public InputStream serialize(InputStream rawContent, DataSetMetadata metadata) {
        try {
            PipedInputStream pipe = new PipedInputStream();
            PipedOutputStream jsonOutput = new PipedOutputStream(pipe);
            // Serialize asynchronously for better performance (especially if caller doesn't consume all, see sampling).
            Runnable r = () -> {
                final Map<String, String> parameters = metadata.getContent().getParameters();
                final String separator = parameters.get(CSVFormatGuess.SEPARATOR_PARAMETER);
                try (CSVReader reader = new CSVReader(new InputStreamReader(rawContent), separator.charAt(0))) {
                    JsonGenerator generator = new JsonFactory().createGenerator(jsonOutput);
                    reader.readNext(); // Skip column names
                    generator.writeStartArray();
                    writeLineContent(reader, metadata, generator, separator);
                    generator.writeEndArray();
                    generator.flush();
                } catch (Exception e) {
                    // Consumer may very well interrupt consumption of stream (in case of limit(n) use for sampling).
                    // This is not an issue as consumer is allowed to partially consumes results, it's up to the
                    // consumer to ensure data it consumed is consistent.
                    LOGGER.debug("Unable to continue serialization. Skipping remaining content.", e);
                } finally {
                    try {
                        jsonOutput.close();
                    } catch (IOException e) {
                        LOGGER.error("Unable to close output", e);
                    }
                }
            };
            executor.execute(r);
            return pipe;
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
            if (line.length == 1 && (StringUtils.isEmpty(line[0]) || line[0].charAt(0) == '\u0000')) {
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
                    generator.writeString(cleanCharacters(additionalContent));
                }
                // deal with fewer content (line.length < columns.size)
                else if (i < line.length && line[i] != null) {
                    generator.writeString(cleanCharacters(line[i]));
                }
                // deal with null
                else {
                    generator.writeNull();
                }
            }
            generator.writeEndObject();
        }
    }

    private String cleanCharacters(final String value) {
        return StringUtils.remove(value, '\u0000');
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
