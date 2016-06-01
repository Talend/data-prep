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

package org.talend.dataprep.schema.xls.serialization;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.talend.dataprep.schema.xls.XlsSerializer.isHeaderLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.xls.XlsUtils;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * The ui and statistics calculation will not be happy with null from some cell.
 * So as the parser event may not generate event for empty, we need to build a list with empty currentLineValues
 * and populate it with currentLineValues and calculate the column index from the cell reference.
 */
class XlsxContentHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    /** This class' logger. */
    private static final Logger logger = LoggerFactory.getLogger(XlsxContentHandler.class);
    /** The json generator to use. */
    private final JsonGenerator generator;
    /** The dataset metadata to use for the serialization. */
    private final DataSetMetadata metadata;
    /** The number of columns. */
    private final int columnsNumber;
    /** If it's a header line. */
    private boolean headerLine = false;
    /** List of current line values. */
    private List<String> currentLineValues;

    /**
     * Constructor.
     * @param generator The json generator to use.
     * @param metadata The dataset metadata to use for the serialization.
     */
    XlsxContentHandler(JsonGenerator generator, DataSetMetadata metadata) {
        this.generator = generator;
        this.metadata = metadata;
        this.columnsNumber = metadata.getRowMetadata().getColumns().size();
    }

    /**
     * @see XSSFSheetXMLHandler.SheetContentsHandler#cell(String, String, XSSFComment)
     */
    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        if (!headerLine) {
            logger.trace("cell {} -> {}", cellReference, formattedValue);
            int columnIndex = XlsUtils.getColumnNumberFromCellRef( cellReference );
            // in case of formula error poi return a string starting with "ERROR:"
            // "ERROR:"
            // FIXME this may be wrong if a user really this!! but we do not have control here
            // except overriding XSSFSheetXMLHandler#endElement
            this.currentLineValues.set(columnIndex, startsWith(formattedValue, "ERROR:") ? EMPTY : formattedValue);
        }
    }

    /**
     * @see XSSFSheetXMLHandler.SheetContentsHandler#startRow(int)
     */
    @Override
    public void startRow(int rowNum) {
        logger.trace("startRow {}", rowNum);
        // is header line?
        if (isHeaderLine(rowNum, metadata.getRowMetadata().getColumns())) {
            headerLine = true;
        } else {
            this.currentLineValues = createListWithEmpty(columnsNumber);
        }
    }

    /**
     * @see XSSFSheetXMLHandler.SheetContentsHandler#endRow(int)
     */
    @Override
    public void endRow(int rowNum) {

        logger.trace("endRow {}", rowNum);

        if (!headerLine) {
            try {
                generator.writeStartObject();
                for (int j = 0; j < metadata.getRowMetadata().getColumns().size(); j++) {
                    ColumnMetadata columnMetadata = metadata.getRowMetadata().getColumns().get(j);
                    String cellValue = this.currentLineValues.get(j);
                    generator.writeFieldName(columnMetadata.getId());
                    if (cellValue != null) {
                        generator.writeString(cellValue);
                    } else {
                        generator.writeNull();
                    }
                }
                generator.writeEndObject();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
            }
        }
        headerLine = false;
    }

    /**
     * @see XSSFSheetXMLHandler.SheetContentsHandler#headerFooter(String, boolean, String) r
     */
    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        logger.trace("headerFooter");
    }

    /**
     * Return a list of empty string from the given size.
     * @param size the wanted list size.
     * @return a list of empty string from the given size.
     */
    public static List<String> createListWithEmpty(int size) {
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(EMPTY);
        }
        return list;
    }
}
