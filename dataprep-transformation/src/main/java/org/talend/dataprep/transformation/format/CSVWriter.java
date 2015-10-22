package org.talend.dataprep.transformation.format;

import static org.talend.dataprep.transformation.format.CSVFormat.CSV;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
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
    public static final String SEPARATOR_PARAM_NAME = "exportParameters.csvSeparator";

    /** The CSV writer. */
    private final au.com.bytecode.opencsv.CSVWriter writer;

    /** the columns ids. */
    private String[] columnIds;


    /**
     * Simple constructor with default separator value.
     * 
     * @param output where this writer should... write !
     */
    public CSVWriter(final OutputStream output) {
        writer = new au.com.bytecode.opencsv.CSVWriter(new OutputStreamWriter(output), DEFAULT_SEPARATOR);
    }

    /**
     * Constructor.
     * 
     * @param output where to write the dataset.
     * @param parameters parameters to get the separator from.
     */
    public CSVWriter(final OutputStream output, Map<String, Object> parameters) {
        String actualSeparator = (String) parameters.get(SEPARATOR_PARAM_NAME);
        if (actualSeparator == null || StringUtils.isEmpty(actualSeparator) || actualSeparator.length() > 1) {
            actualSeparator = String.valueOf(DEFAULT_SEPARATOR);
        }
        writer = new au.com.bytecode.opencsv.CSVWriter(new OutputStreamWriter(output), actualSeparator.charAt(0));
    }

    /**
     * @see TransformerWriter#write(RowMetadata)
     */
    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        // write the columns names
        String[] columnsName = rowMetadata.getColumns().stream().map(ColumnMetadata::getName).toArray(String[]::new);
        writer.writeNext(columnsName);

        // and store the columns ids for the rows.
        columnIds = rowMetadata.getColumns().stream().map(ColumnMetadata::getId).toArray(String[]::new);
    }

    /**
     * @see TransformerWriter#write(DataSetRow)
     * @throws UnsupportedOperationException if CsvWriter#write(RowMetadata) was not called before.
     */
    @Override
    public void write(final DataSetRow row) throws IOException {
        if (columnIds == null) {
            throw new UnsupportedOperationException("Write columns should be called before to init column list");
        }
        final String[] csvRow = Arrays.stream(columnIds).map(row::get).toArray(String[]::new);
        writer.writeNext(csvRow);
    }

    /**
     * @see TransformerWriter#flush()
     */
    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public boolean requireMetadataForHeader() {
        return true;
    }
}
