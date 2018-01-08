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

package org.talend.dataprep.schema.xls.serialization;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.schema.xls.XlsSerializer.isHeaderLine;
import static org.talend.dataprep.schema.xls.XlsUtils.getCellValueAsString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Serialize XLS file.
 */
public class XlsRunnable implements Runnable {

    /** This class' logger. */
    private static final Logger LOG = getLogger(XlsRunnable.class);

    /** The input stream raw content. */
    private final InputStream rawContent;

    /** Where to serialize the json. */
    private final OutputStream jsonOutput;

    /** The dataset metadata. */
    private final DataSetMetadata metadata;

    private final long limit;

    /** A json factory to use for the serialization. */
    private final JsonFactory jsonFactory;

    /**
     * Constructor.
     *  @param rawContent the raw excel file content.
     * @param jsonOutput Where to serialize the json.
     * @param metadata The dataset metadata.
     * @param limit
     * @param factory A json factory to use for the serialization.
     */
    public XlsRunnable(InputStream rawContent, OutputStream jsonOutput, DataSetMetadata metadata, long limit, JsonFactory factory) {
        this.rawContent = rawContent;
        this.jsonOutput = jsonOutput;
        this.metadata = metadata;
        this.limit = limit;
        this.jsonFactory = factory;
    }

    /**
     * @see Runnable#run()
     */
    @Override
    public void run() {
        try {

            Workbook workbook = WorkbookFactory.create(rawContent);

            JsonGenerator generator = jsonFactory.createGenerator(jsonOutput);

            // if no sheet name just get the first one (take it easy mate :-) )
            Sheet sheet = isEmpty(metadata.getSheetName()) ? workbook.getSheetAt(0) : workbook.getSheet(metadata.getSheetName());

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
            List<ColumnMetadata> columns = metadata.getRowMetadata().getColumns();

            serializeColumns(workbook, generator, sheet, columns);

            generator.writeEndArray();
            generator.flush();
        } catch (Exception e) {
            // Consumer may very well interrupt consumption of stream (in case of limit(n) use for sampling).
            // This is not an issue as consumer is allowed to partially consumes results, it's up to the
            // consumer to ensure data it consumed is consistent.
            LOG.debug("Unable to continue serialization for {}. Skipping remaining content.", metadata.getId(), e);
        } finally {
            try {
                jsonOutput.close();
            } catch (IOException e) {
                LOG.error("Unable to close output", e);
            }
        }
    }

    private void serializeColumns(Workbook workbook, JsonGenerator generator, Sheet sheet, List<ColumnMetadata> columns)
            throws IOException {

        for (int i = 0, size = sheet.getLastRowNum(); i <= size; i++) {
            if (limit > 0 && i > limit) {
                break;
            }
            // is header line?
            Row row = sheet.getRow(i);
            if (isHeaderLine(i, columns) || row == null) {
                continue;
            }

            generator.writeStartObject();
            for (ColumnMetadata columnMetadata : columns) {

                // do not write the values if this has been detected as an header
                if (i < columnMetadata.getHeaderSize()) {
                    continue;
                }

                int colId = Integer.parseInt(columnMetadata.getId());
                String cellValue = getCellValueAsString(row.getCell(colId), workbook.getCreationHelper().createFormulaEvaluator());
                LOG.trace("cellValue for {}/{}: {}", i, colId, cellValue);
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
