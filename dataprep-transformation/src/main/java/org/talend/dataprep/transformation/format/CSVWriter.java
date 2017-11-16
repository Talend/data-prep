// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriter;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.util.FilesHelper;

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

    private File bufferFile;

    private CSVWriterCustom recordsWriter;

    /** The default separator. */
    @Value("${default.text.separator:;}")
    private String defaultSeparator;

    /** The default enclosure character. */
    @Value("${default.text.enclosure=:\"}")
    private String defaultTextEnclosure;

    /** The default escape character. */
    @Value("${default.text.escape:\"}")
    private String defaultEscapeChar;

    /** The default encoding. */
    @Value("${default.text.encoding:UTF-8}")
    private String defaultEncoding;

    private Map<String, String> parameters;

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
        try {
            this.separator = getParameterCharValue(parameters, CSVFormat.ParametersCSV.FIELDS_DELIMITER, defaultSeparator);
            this.escapeCharacter = getParameterCharValue(parameters, CSVFormat.ParametersCSV.ESCAPE_CHAR, defaultEscapeChar);
            this.enclosureCharacter = getParameterCharValue(parameters, CSVFormat.ParametersCSV.ENCLOSURE_CHAR,
                    defaultTextEnclosure);
            this.enclosureMode = getParameterStringValue(parameters, CSVFormat.ParametersCSV.ENCLOSURE_MODE,
                    DEFAULT_ENCLOSURE_MODE);

            Charset encoding = extractEncodingWithFallback(parameters.get(CSVFormat.ParametersCSV.ENCODING));

            bufferFile = File.createTempFile("csvWriter", ".csv");

            OutputStreamWriter charOutput = new OutputStreamWriter(new FileOutputStream(bufferFile), encoding);

            recordsWriter = new CSVWriterCustom(charOutput, separator, enclosureCharacter, escapeCharacter);

        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_USE_EXPORT, e);
        }
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

    private static char getParameterCharValue(Map<String, String> parameters, String parameterName, String defaultValue) {
        String parameter = parameters.get(parameterName);
        if (parameter == null || StringUtils.isEmpty(parameter) || parameter.length() > 1) {
            return defaultValue.charAt(0);
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
        return recordsWriter;
    }

    @Override
    public void write(DataSetRow row) throws IOException {
        if (!row.values().isEmpty() && row.getRowMetadata().getColumns().isEmpty()) {
            throw new IllegalStateException(
                    " If a dataset row has some values it should at least have columns just before writing the result of a non json transformation.");
        }

        // values need to be written in the same order as the columns
        if (DEFAULT_ENCLOSURE_MODE.equals(enclosureMode)) {
            recordsWriter.writeNext(row.order().toArray(DataSetRow.SKIP_TDP_ID), row.getRowMetadata());
        } else {
            recordsWriter.writeNext(row.order().toArray(DataSetRow.SKIP_TDP_ID));
        }

    }

    /**
     * @see TransformerWriter#write(RowMetadata)
     */
    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        // write the columns names
        String[] columnsName = rowMetadata.getColumns().stream().map(ColumnMetadata::getName).toArray(String[]::new);

        CSVWriterCustom csvWriter = //
                new CSVWriterCustom(new OutputStreamWriter(output), separator, enclosureCharacter, escapeCharacter);
        // values need to be written in the same order as the columns
        if (DEFAULT_ENCLOSURE_MODE.equals(enclosureMode)) {
            csvWriter.writeNext(columnsName, rowMetadata);
        } else {
            csvWriter.writeNext(columnsName);
        }

        csvWriter.flush();
        // Write buffered records
        recordsWriter.flush();
        try (InputStream input = new FileInputStream(bufferFile)) {
            IOUtils.copy(input, output);
        } finally {
            recordsWriter.close();
        }
    }

    /**
     * @see TransformerWriter#flush()
     */
    @Override
    public void flush() throws IOException {
        output.flush();
        try {
            FilesHelper.delete(bufferFile);
        } catch (IOException e) {
            LOGGER.warn("Unable to delete temporary file '{}'", bufferFile, e);
        }
    }
}
