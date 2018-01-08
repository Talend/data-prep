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

package org.talend.dataprep.transformation.format;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.dataprep.transformation.format.CSVFormat.CSV;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriter;

/**
 * Write datasets in CSV.
 *
 * <strong>Warning</strong>: implementation does not support sending records after metadata. Metadata MUST be sent last or will
 * not be written.
 */
@Scope("prototype")
@Component("writer#" + CSV)
public class CSVWriter extends AbstractTransformerWriter {

    /** The default enclosure character. */
    private static final String DEFAULT_ENCLOSURE_MODE = CSVFormat.ParametersCSV.ENCLOSURE_TEXT_ONLY;

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);

    private final OutputStream output;

    private char separator;

    private char enclosureCharacter;

    private String enclosureMode;

    private char escapeCharacter;

    private ObjectBuffer<BufferedDatasetRow> objectBuffer;

    /** The default separator. */
    @Value("${default.text.separator:;}")
    private String defaultSeparator;

    /** The default enclosure character. */
    @Value("${default.text.enclosure:\"}")
    private String defaultTextEnclosure;

    /** The default escape character. */
    @Value("${default.text.escape:\"}")
    private String defaultEscapeChar;

    /** The default encoding. */
    @Value("${default.text.encoding:UTF-8}")
    private String defaultEncoding;

    private Map<String, String> parameters;

    private Charset encoding;

    private CSVWriterCustom csvWriter;

    /**
     * Simple constructor with default separator value.
     *
     * @param output where this writer should... write !
     */
    public CSVWriter(final OutputStream output) {
        this(output, Collections.emptyMap());
    }

    /**
     * Constructor.
     *
     * @param output where to write the dataset.
     * @param parameters parameters to get the separator and the escape character from.
     */
    public CSVWriter(final OutputStream output, Map<String, String> parameters) {
        this.parameters = ExportFormat.cleanParameters(parameters);
        this.output = output;
    }

    @PostConstruct
    private void initWriter() {
        separator = getParameterCharValue(parameters, CSVFormat.ParametersCSV.FIELDS_DELIMITER, defaultSeparator);
        escapeCharacter = getParameterCharValueWithEmpty(parameters, CSVFormat.ParametersCSV.ESCAPE_CHAR, defaultEscapeChar);
        enclosureCharacter = getParameterCharValueWithEmpty(parameters, CSVFormat.ParametersCSV.ENCLOSURE_CHAR,
                defaultTextEnclosure);
        enclosureMode = getParameterStringValue(parameters, CSVFormat.ParametersCSV.ENCLOSURE_MODE, DEFAULT_ENCLOSURE_MODE);

        encoding = extractEncodingWithFallback(parameters.get(CSVFormat.ParametersCSV.ENCODING));
    }

    private Charset extractEncodingWithFallback(String encodingParameter) {
        Charset charset;
        if (!StringUtils.isEmpty(encodingParameter) && Charset.isSupported(encodingParameter)) {
            charset = Charset.forName(encodingParameter);
        } else {
            charset = Charset.isSupported(defaultEncoding) ? Charset.forName(defaultEncoding) : UTF_8;
        }
        return charset;
    }

    /**
     * separator value can't be empty
     */
    private static char getParameterCharValue(Map<String, String> parameters, String parameterName, String defaultValue) {
        String parameter = parameters.get(parameterName);
        if (parameter == null || StringUtils.isEmpty(parameter) || parameter.length() > 1) {
            return defaultValue.charAt(0);
        } else {
            return parameter.charAt(0);
        }
    }

    /**
     * escape and enclosure character can be empty
     */
    private static char getParameterCharValueWithEmpty(Map<String, String> parameters, String parameterName,
            String defaultValue) {
        String parameter = parameters.get(parameterName);
        if (parameter == null || parameter.length() > 1) {
            return defaultValue.charAt(0);
        } else if (StringUtils.isEmpty(parameter)) {
            return Character.MIN_VALUE;
        } else {
            return parameter.charAt(0);
        }
    }

    private static String getParameterStringValue(Map<String, String> parameters, String parameterName, String defaultValue) {
        String parameter = parameters.get(parameterName);
        if (parameter == null || StringUtils.isEmpty(parameter)) {
            return String.valueOf(defaultValue);
        } else {
            return parameter;
        }
    }

    @Override
    protected au.com.bytecode.opencsv.CSVWriter getRecordsWriter() {
        return csvWriter;
    }

    /**
     * Write the content of the current row
     *
     * @param row the column metadata
     */
    @Override
    public void write(DataSetRow row) throws IOException {
        if (!row.values().isEmpty() && row.getRowMetadata().getColumns().isEmpty()) {
            throw new IllegalStateException(
                    " If a dataset row has some values it should at least have columns just before writing the result of a non json transformation.");
        }

        if (csvWriter == null) {
            if (objectBuffer == null) {
                objectBuffer = new ObjectBuffer<>(BufferedDatasetRow.class);
            }
            objectBuffer.appendRow(new BufferedDatasetRow(row));
        } else {
            internalWrite(new BufferedDatasetRow(row));
        }
    }

    /**
     * Write the rowMetadata
     *
     * @param rowMetadata
     */
    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        csvWriter = new CSVWriterCustom(new OutputStreamWriter(output, encoding), separator, enclosureCharacter, escapeCharacter);

        // write the columns names, i.e. the header of the file
        csvWriter.writeNext(new BufferedDatasetRow(rowMetadata).nextLine);

        // Write buffered records
        if (objectBuffer != null) {
            // Warning: if an exception occurs in the stream it will terminate the stream thus stopping the writing.
            try {
                objectBuffer.readAll().forEach(this::internalWrite);
                LOGGER.debug("Finished writing temporary values into TComp.");
            } finally {
                objectBuffer.close();
                objectBuffer = null;
            }
        }
        csvWriter.flush();
    }

    /**
     * Choose the type of writer
     *
     * @param row the current row to write
     */
    private void internalWrite(BufferedDatasetRow row) {
        // values need to be written in the same order as the columns
        if (DEFAULT_ENCLOSURE_MODE.equals(enclosureMode)) {
            csvWriter.writeNext(row.nextLine, row.isEnclosedTypeValues);
        } else {
            csvWriter.writeNext(row.nextLine);
        }
    }

    @Override
    public void flush() throws IOException {
        if (csvWriter != null) {
            csvWriter.flush();
        }
    }

    private static final class BufferedDatasetRow {

        public String[] nextLine;

        public Boolean[] isEnclosedTypeValues;

        // for jackson serialization
        public BufferedDatasetRow() {
        }

        public BufferedDatasetRow(RowMetadata rowMetadata) {
            nextLine = rowMetadata.getColumns().stream().map(ColumnMetadata::getName).toArray(String[]::new);
            isEnclosedTypeValues = rowMetadata.getColumns().stream().map(ColumnMetadata::getType)
                    .map(v -> v.equals(Type.STRING.getName())).toArray(Boolean[]::new);
        }

        public BufferedDatasetRow(DataSetRow row) {
            nextLine = row.order().toArray(DataSetRow.SKIP_TDP_ID);
            isEnclosedTypeValues = row.getRowMetadata().getColumns().stream().map(ColumnMetadata::getType)
                    .map(v -> v.equals(Type.STRING.getName())).toArray(Boolean[]::new);
        }
    }
}
