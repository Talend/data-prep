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

import static org.talend.dataprep.transformation.format.XlsFormat.XLSX;

import java.io.*;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.util.FilesHelper;

import au.com.bytecode.opencsv.CSVReader;

@Scope("prototype")
@Component("writer#" + XLSX)
public class XlsWriter implements TransformerWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsWriter.class);

    // The separator to be used in temporary record buffer
    private static final char BUFFER_CSV_SEPARATOR = ',';

    private final OutputStream outputStream;

    private final SXSSFWorkbook workbook;

    private final Sheet sheet;

    // Holds a temporary buffer on disk (as CSV) of records to be written
    private final File bufferFile;

    // The CSV Writer to write to buffer
    private final au.com.bytecode.opencsv.CSVWriter recordsWriter;

    private int rowIdx = 0;

    public XlsWriter(final OutputStream output) {
        this(output, Collections.emptyMap());
    }

    public XlsWriter(final OutputStream output, Map<String, String> parameters) {
        try {
            this.outputStream = output;
            // we limit to only 50 rows in memory
            this.workbook = new SXSSFWorkbook(50);
            // TODO sheet name as an option?
            this.sheet = this.workbook.createSheet("sheet1");
            bufferFile = File.createTempFile("xlsWriter", ".csv");
            recordsWriter = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(bufferFile), BUFFER_CSV_SEPARATOR);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_USE_EXPORT, e);
        }
    }

    @Override
    public void write(RowMetadata columns) throws IOException {
        LOGGER.debug("write RowMetadata: {}", columns);
        if (columns.getColumns().isEmpty()) {
            return;
        }
        CreationHelper createHelper = this.workbook.getCreationHelper();
        // writing headers so first row
        Row headerRow = this.sheet.createRow(rowIdx++);
        int cellIdx = 0;
        for (ColumnMetadata columnMetadata : columns.getColumns()) {
            // TODO apply some formatting as it's an header cell?
            headerRow.createCell(cellIdx++).setCellValue(createHelper.createRichTextString(columnMetadata.getName()));
        }
        // Empty buffer
        recordsWriter.flush();
        recordsWriter.close();
        try (Reader reader = new InputStreamReader(new FileInputStream(bufferFile))) {
            try (CSVReader bufferReader = new CSVReader(reader, BUFFER_CSV_SEPARATOR, '\"', '\0')) {
                String[] nextRow;
                while ((nextRow = bufferReader.readNext()) != null) {
                    // writing data
                    Row row = this.sheet.createRow(rowIdx++);
                    cellIdx = 0;
                    for (ColumnMetadata columnMetadata : columns.getColumns()) {
                        Cell cell = row.createCell(cellIdx);
                        String val = nextRow[cellIdx];
                        switch (Type.get(columnMetadata.getType())) {
                            case NUMERIC:
                            case INTEGER:
                            case DOUBLE:
                            case FLOAT:
                                try {
                                    if (!StringUtils.isEmpty(val)) {
                                        cell.setCellValue(Double.valueOf(val));
                                    }
                                } catch (NumberFormatException e) {
                                    LOGGER.warn("skip NumberFormatException and use string for value {} row {} cell {}", //
                                            val, rowIdx - 1, cellIdx - 1);
                                    cell.setCellValue(val);
                                }
                                break;
                            case BOOLEAN:
                                cell.setCellValue(Boolean.valueOf(val));
                                break;
                            // FIXME ATM we don't have any idea about the date format so this can generate exceptions
                            // case "date":
                            // cell.setCellValue( );
                            default:
                                cell.setCellValue(val);
                        }
                        cellIdx++;
                    }
                }
            }
        }
    }

    @Override
    public void write(DataSetRow row) throws IOException {
        LOGGER.trace("Buffering DataSetRow (metadata not ready): {}", row);
        // values need to be written in the same order as the columns
        recordsWriter.writeNext(row.order().toArray(DataSetRow.SKIP_TDP_ID));
    }

    @Override
    public void flush() throws IOException {
        this.workbook.write(outputStream);
        try {
            FilesHelper.delete(bufferFile);
        } catch (IOException e) {
            LOGGER.warn("Unable to delete temporary file '{}'", bufferFile, e);
        }
    }

}
