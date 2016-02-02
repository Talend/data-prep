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
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

/**
 * Write datasets in CSV.
 */
@Scope("prototype")
@Component("writer#" + CSV)
public class CSVWriter implements TransformerWriter {

    /** The default separator. */
    private static final Character DEFAULT_SEPARATOR = ',';

    /** Separator argument name. */
    public static final String SEPARATOR_PARAM_NAME = ExportFormat.PREFIX + "csvSeparator";

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWriter.class);

    private final OutputStream output;

    private final char separator;

    private final File bufferFile;

    private final au.com.bytecode.opencsv.CSVWriter recordsWriter;

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
     * @param parameters parameters to get the separator from.
     */
    public CSVWriter(final OutputStream output, Map<String, String> parameters) {
        try {
            this.output = output;
            String separatorParameter = parameters.get(SEPARATOR_PARAM_NAME);
            if (separatorParameter == null || StringUtils.isEmpty(separatorParameter) || separatorParameter.length() > 1) {
                this.separator = String.valueOf(DEFAULT_SEPARATOR).charAt(0);
            } else {
                this.separator = separatorParameter.charAt(0);
            }
            bufferFile = File.createTempFile("csvWriter", ".csv");
            recordsWriter = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(bufferFile), separator);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_USE_EXPORT, e);
        }
    }

    /**
     * @see TransformerWriter#write(RowMetadata)
     */
    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        // write the columns names
        String[] columnsName = rowMetadata.getColumns().stream().map(ColumnMetadata::getName).toArray(String[]::new);
        au.com.bytecode.opencsv.CSVWriter csvWriter = //
        new au.com.bytecode.opencsv.CSVWriter(new OutputStreamWriter(output), separator);
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
     * @see TransformerWriter#write(DataSetRow)
     * @throws UnsupportedOperationException if CsvWriter#write(RowMetadata) was not called before.
     */
    @Override
    public void write(final DataSetRow row) throws IOException {
        // values need to be written in the same order as the columns
        recordsWriter.writeNext(row.order().toArray(DataSetRow.SKIP_TDP_ID));
    }

    /**
     * @see TransformerWriter#flush()
     */
    @Override
    public void flush() throws IOException {
        output.flush();
        if (bufferFile.exists()) {
            if (!bufferFile.delete()) {
                LOGGER.warn("Unable to delete temporary file '{}'", bufferFile);
            }
        }
    }

}
