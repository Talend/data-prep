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

package org.talend.dataprep.transformation.format;

import static org.talend.dataprep.transformation.format.CSVFormat.CSV;

import java.io.*;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriter;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.util.FilesHelper;

/**
 * Write datasets in CSV.
 */
@Scope("prototype")
@Component("writer#" + CSV)
public class CSVWriter extends AbstractTransformerWriter {

    /** The default separator. */
    private static final Character DEFAULT_SEPARATOR = ',';

    /** The default escape character. */
    private static final Character DEFAULT_ESCAPE_CHARACTER = '"';

    /** Separator argument name. */
    public static final String SEPARATOR_PARAM_NAME = ExportFormat.PREFIX + "csv_fields_delimiter";

    /** Escape character argument name. */
    public static final String ESCAPE_CHARACTER_PARAM_NAME = ExportFormat.PREFIX + "csv_escape_character";

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);

    private final OutputStream output;

    private final char separator;

    private final char escapeCharacter;

    private final File bufferFile;

    private final au.com.bytecode.opencsv.CSVWriter recordsWriter;

    private char getParameterValue(Map<String, String> parameters, String parameterName, char defaultValue) {
        String parameter = parameters.get(parameterName);
        if (parameter == null || StringUtils.isEmpty(parameter) || parameter.length() > 1) {
            return String.valueOf(defaultValue).charAt(0);
        } else {
            return parameter.charAt(0);
        }
    }

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
        try {
            this.output = output;

            this.separator = this.getParameterValue(parameters, SEPARATOR_PARAM_NAME, DEFAULT_SEPARATOR);
            this.escapeCharacter = this.getParameterValue(parameters, ESCAPE_CHARACTER_PARAM_NAME, DEFAULT_ESCAPE_CHARACTER);

            bufferFile = File.createTempFile("csvWriter", ".csv");
            recordsWriter = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(bufferFile), separator, '"', escapeCharacter);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_USE_EXPORT, e);
        }
    }

    @Override
    protected au.com.bytecode.opencsv.CSVWriter getRecordsWriter() {
        return recordsWriter;
    }

    /**
     * @see TransformerWriter#write(RowMetadata)
     */
    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        // write the columns names
        String[] columnsName = rowMetadata.getColumns().stream().map(ColumnMetadata::getName).toArray(String[]::new);
        au.com.bytecode.opencsv.CSVWriter csvWriter = //
        new au.com.bytecode.opencsv.CSVWriter(new OutputStreamWriter(output), separator, '"', escapeCharacter);
        csvWriter.writeNext(columnsName);
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
