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

package org.talend.dataprep.transformation.format;

import static org.talend.dataprep.exception.error.TransformationErrorCodes.UNABLE_TO_PERFORM_EXPORT;
import static org.talend.dataprep.transformation.format.XlsFormat.XLSX;

import java.io.*;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriter;
import org.talend.dataprep.util.FilesHelper;
import org.talend.dataprep.util.NumericHelper;

import au.com.bytecode.opencsv.CSVReader;

@Scope("prototype")
@Component("writer#" + XLSX)
public class XlsWriter extends AbstractTransformerWriter {

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
    protected au.com.bytecode.opencsv.CSVWriter getRecordsWriter() {
        return recordsWriter;
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
                                    if (NumericHelper.isBigDecimal(val)) {
                                        cell.setCellValue(BigDecimalParser.toBigDecimal(val).doubleValue());
                                    } else {
                                        cell.setCellValue(val);
                                    }
                                } catch (NumberFormatException e) {
                                    LOGGER.trace("Skip NumberFormatException and use string for value '{}' row '{}' column '{}'", //
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
    public void flush() throws IOException {

        // because workbook.write(out) close the given output (the http response), another temporary file is created to
        // to fully flush the xlsx result and the copy the content of the file to the http response

        // create a temp file
        final File yetAnotherTempFile = File.createTempFile("xlsWriter", "-2.csv");
        try {
            try (final FileOutputStream fileOutputStream = new FileOutputStream(yetAnotherTempFile)) {
                this.workbook.write(fileOutputStream);
                this.workbook.close();
            } catch (IOException ioe) {
                LOGGER.error("Could not write temp file with xls export", ioe);
                throw new TDPException(UNABLE_TO_PERFORM_EXPORT, ioe);
            }

            // copy the content of the temp file to the http response
            try (final FileInputStream fileInputStream = new FileInputStream(yetAnotherTempFile)) {
                IOUtils.copyLarge(fileInputStream, outputStream);
            } catch (IOException e) {
                LOGGER.error("Error sending the xls export content", e);
                throw new TDPException(UNABLE_TO_PERFORM_EXPORT, e);
            }

        } finally {
            // clean up the buffer file
            try {
                FilesHelper.delete(bufferFile);
            } catch (IOException e) {
                LOGGER.warn("Unable to delete temporary file '{}'", bufferFile, e);
            }
            // clean up temp file
            try {
                FilesHelper.delete(yetAnotherTempFile);
            } catch (IOException ioe) {
                LOGGER.warn("Unable to delete temporary file '{}'", yetAnotherTempFile, ioe);
            }
        }
    }

}
