package org.talend.dataprep.schema.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Service("serializer#xls")
public class XlsSerializer implements Serializer {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(XlsSerializer.class);

    @Override
    public InputStream serialize(InputStream rawContent, DataSetMetadata metadata) {

        try {

            Workbook workbook = XlsUtils.getWorkbook(new SchemaParser.Request(rawContent, metadata));

            StringWriter writer = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(writer);

            // if no sheet name just get the first one (take it easy mate :-) )
            Sheet sheet = StringUtils.isEmpty(metadata.getSheetName()) ? workbook.getSheetAt(0) : //
                    workbook.getSheet(metadata.getSheetName());

            if (sheet == null) {
                // auto generated sheet name so take care!! "sheet-" + i
                if (StringUtils.startsWith(metadata.getSheetName(), "sheet-")) {
                    String sheetNumberStr = StringUtils.removeStart(metadata.getSheetName(), "sheet-");

                    sheet = workbook.getSheetAt(Integer.valueOf(sheetNumberStr));
                }
                // still null so use the first one
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                }
            }

            generator.writeStartArray();

            List<ColumnMetadata> columns = metadata.getRow().getColumns();

            for (int i = 0, size = sheet.getLastRowNum(); i <= size; i++) {

                if (columns != null) {
                    // is header line?
                    if (isHeaderLine(i, columns)) {
                        continue;
                    }

                    Row row = sheet.getRow(i);
                    if (row != null) {
                        generator.writeStartObject();
                        for (int j = 0; j < columns.size(); j++) {
                            ColumnMetadata columnMetadata = columns.get(j);

                            // do not write the values if this has been detected as an header
                            if (i < columnMetadata.getHeaderSize()) {
                                continue;
                            }

                            int colId = Integer.valueOf(columnMetadata.getId());
                            String cellValue = XlsUtils.getCellValueAsString(row.getCell(colId));
                            LOGGER.trace("cellValue for {}/{}: {}", i, colId, cellValue);
                            generator.writeFieldName(columnMetadata.getId());
                            if (cellValue != null) {
                                generator.writeString(cellValue);
                            } else {
                                generator.writeNull();
                            }
                        }
                        generator.writeEndObject();

                    }
                }
            }

            generator.writeEndArray();
            generator.flush();
            return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }

    }

    protected boolean isHeaderLine(int lineIndex, List<ColumnMetadata> columns) {
        boolean headerLine = false;
        for (ColumnMetadata columnMetadata : columns) {
            if (lineIndex < columnMetadata.getHeaderSize()) {
                headerLine = true;
            }
        }
        return headerLine;
    }

}
