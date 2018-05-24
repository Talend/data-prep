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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.util.FilesHelper;
import org.talend.dataprep.util.NumericHelper;

@Scope("prototype")
@Component("writer#" + XLSX)
public class XlsWriter implements TransformerWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsWriter.class);

    // The separator to be used in temporary record buffer
    private static final char BUFFER_CSV_SEPARATOR = ',';

    private final OutputStream outputStream;

    private final SXSSFWorkbook workbook;

    private final Sheet sheet;

    // Holds a temporary buffer on disk of records to be written
    private ObjectBuffer<String[]> rowsBuffer;

    private RowMetadata writtenMetadata;

    private int rowIdx = 0;

    private boolean closed = false;

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
            rowsBuffer = new ObjectBuffer<>(String[].class);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_USE_EXPORT, e);
        }
    }

    @Override
    public void write(DataSetRow row) throws IOException {
        if (!row.values().isEmpty() && row.getRowMetadata().getColumns().isEmpty()) {
            throw new IllegalStateException(
                    " If a dataset row has some values it should at least have columns just before writing the result of a non json transformation.");
        }
        // values need to be written in the same order as the columns
        String[] rowValues = row.order().toArray(DataSetRow.SKIP_TDP_ID);
        if (writtenMetadata == null) {
            rowsBuffer.appendRow(rowValues);
        } else {
            internalWriteRow(writtenMetadata, rowValues);
        }
    }

    @Override
    public void write(RowMetadata metadata) throws IOException {
        LOGGER.debug("write RowMetadata: {}", metadata);
        if (!metadata.getColumns().isEmpty()) {
            writeHeader(metadata);
            writtenMetadata = metadata;

            // Empty buffer
            rowsBuffer.readAll().forEach(row -> internalWriteRow(metadata, row));
            safeCloseObjectBuffer();
        }
    }

    /** writing headers so first row. */
    private void writeHeader(RowMetadata metadata) {
        CreationHelper createHelper = this.workbook.getCreationHelper();
        Row headerRow = this.sheet.createRow(rowIdx++);
        int cellIdx = 0;
        for (ColumnMetadata columnMetadata : metadata.getColumns()) {
            // TODO apply some formatting as it's an header cell?
            headerRow.createCell(cellIdx++).setCellValue(createHelper.createRichTextString(columnMetadata.getName()));
        }
    }

    private void internalWriteRow(RowMetadata metadata, String[] nextRow) {
        int cellIdx;// writing data
        Row row = this.sheet.createRow(rowIdx++);
        cellIdx = 0;
        for (ColumnMetadata columnMetadata : metadata.getColumns()) {
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

    @Override
    public void close() throws IOException {
        if (!closed) {
            // first close the buffer
            safeCloseObjectBuffer();
            // because workbook.write(out) close the given output (the http response), another temporary file is created to
            // to fully flush the xlsx result and the copy the content of the file to the http response

            // create a temp file
            final File workbookTempFile = File.createTempFile("xlsWriter", "-2.xlsx");
            try (final FileOutputStream fileOutputStream = new FileOutputStream(workbookTempFile)) {
                workbook.write(fileOutputStream);
                workbook.close();
            } catch (IOException ioe) {
                LOGGER.error("Could not write temp file with xls export", ioe);
                throw new TDPException(UNABLE_TO_PERFORM_EXPORT, ioe);
            }

            // copy the content of the temp file to the http response
            try (final FileInputStream fileInputStream = new FileInputStream(workbookTempFile)) {
                IOUtils.copyLarge(fileInputStream, outputStream);
            } catch (IOException e) {
                LOGGER.error("Error sending the xls export content", e);
                throw new TDPException(UNABLE_TO_PERFORM_EXPORT, e);
            } finally {
                FilesHelper.deleteQuietly(workbookTempFile);
            }
            closed = true;
        }
    }

    private void safeCloseObjectBuffer() throws IOException {
        if (rowsBuffer != null) {
            rowsBuffer.close();
            rowsBuffer = null;
        }
    }

}
