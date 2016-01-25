package org.talend.dataprep.transformation.format;

import static org.talend.dataprep.transformation.format.XlsFormat.XLSX;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

@Scope("prototype")
@Component("writer#" + XLSX)
public class XlsWriter implements TransformerWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsWriter.class);

    private final OutputStream outputStream;

    private final Workbook workbook;

    private final Sheet sheet;

    private int rowIdx = 0;

    private List<ColumnMetadata> columnsMetadata;

    public XlsWriter(final OutputStream output, Map<String, String> parameters) {
        this(output);
    }

    public XlsWriter(final OutputStream output) {
        this.outputStream = output;

        this.workbook = new XSSFWorkbook();
        // TODO sheet name as an option?
        this.sheet = this.workbook.createSheet("sheet1");
    }

    @Override
    public void write(RowMetadata columns) throws IOException {
        LOGGER.debug("write RowMetadata: {}", columns);
        if (columns.getColumns().isEmpty()) {
            return;
        }

        this.columnsMetadata = columns.getColumns();

        CreationHelper createHelper = this.workbook.getCreationHelper();

        // writing headers so first row
        Row headerRow = this.sheet.createRow(rowIdx++);

        int cellIdx = 0;

        for (ColumnMetadata columnMetadata : columns.getColumns()) {

            // TODO apply some formatting as it's an header cell?
            headerRow.createCell(cellIdx++).setCellValue(createHelper.createRichTextString(columnMetadata.getName()));

        }

    }

    @Override
    public void write(DataSetRow dataSetRow) throws IOException {
        LOGGER.debug("write DataSetRow: {}", dataSetRow);
        // writing datas

        Row row = this.sheet.createRow(rowIdx++);

        int cellIdx = 0;

        for (ColumnMetadata columnMetadata : this.columnsMetadata) {

            Cell cell = row.createCell(cellIdx++);
            switch (Type.get(columnMetadata.getType())) {
            case NUMERIC:
            case INTEGER:
            case DOUBLE:
            case FLOAT:
                String val = dataSetRow.get(columnMetadata.getId());
                try {
                    if (!StringUtils.isEmpty(val)) {
                        cell.setCellValue(Double.valueOf(val));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("skip NumberFormatException and use string for value {} row {} cell {}", //
                            dataSetRow.get(columnMetadata.getId()), rowIdx - 1, cellIdx - 1);
                    cell.setCellValue(val);
                }
                break;
            case BOOLEAN:
                cell.setCellValue(Boolean.valueOf(dataSetRow.get(columnMetadata.getId())));
                break;
            // FIXME ATM we don't have any idea about the date format so this can generate exceptions
            // case "date":
            // cell.setCellValue( );
            default:
                cell.setCellValue(dataSetRow.get(columnMetadata.getId()));
            }

        }

    }

    @Override
    public void flush() throws IOException {
        this.workbook.write(outputStream);
    }

    @Override
    public boolean requireMetadataForHeader() {
        return true;
    }

}
