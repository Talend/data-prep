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

package org.talend.dataprep.schema.xls;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.schema.SchemaParser;

public class XlsUtils {

    /** this class' logger. */
    private static final transient Logger LOGGER = LoggerFactory.getLogger(XlsUtils.class);

    /**
     * Private constructor.
     */
    private XlsUtils() {
        // Utility class should not have a public constructor.
    }

    /**
     *
     * @param cell
     * @param formulaEvaluator
     * @return return the cell value as String (if needed evaluate the existing formula)
     */
    public static String getCellValueAsString( Cell cell, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return StringUtils.EMPTY;
        }
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_BLANK:
            return "";
        case Cell.CELL_TYPE_BOOLEAN:
            return cell.getBooleanCellValue() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
        case Cell.CELL_TYPE_ERROR:
            return "Cell Error type";
        case Cell.CELL_TYPE_FORMULA:
            return getCellValueAsString(cell, formulaEvaluator.evaluate(cell) );
        case Cell.CELL_TYPE_NUMERIC:
            return getNumericValue(cell, null, false);
        case Cell.CELL_TYPE_STRING:
            return StringUtils.trim(cell.getStringCellValue());
        default:
            return "Unknown Cell Type: " + cell.getCellType();
        }
    }

    /**
     *
     * @param cell
     * @param cellValue
     * @return internal method which switch on the formula result value type then return a String value
     */
    private static String getCellValueAsString(Cell cell, CellValue cellValue) {
        if (cellValue == null) {
            return StringUtils.EMPTY;
        }
        switch (cellValue.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return cellValue.getBooleanValue() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
            case Cell.CELL_TYPE_ERROR:
                return "Cell Error type";
            case Cell.CELL_TYPE_NUMERIC:
                return getNumericValue(cell, cellValue, cellValue != null);
            case Cell.CELL_TYPE_STRING:
                return StringUtils.trim(cell.getStringCellValue());
            default:
                return "Unknown Cell Type: " + cell.getCellType();
        }
    }

    /**
     * Return the numeric value.
     *
     * @param cell the cell to extract the value from.
     * @return the numeric value from the cell.
     */
    private static String getNumericValue(Cell cell, CellValue cellValue, boolean fromFormula) {
        // Date is typed as numeric
        if (HSSFDateUtil.isCellDateFormatted(cell)) { // TODO configurable??
            DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            return sdf.format(cell.getDateCellValue());
        }
        // Numeric type (use data formatter to get number format right)
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);

        if (cellValue == null){
            return formatter.formatCellValue(cell);
        }

        return fromFormula ? cellValue.formatAsString() : formatter.formatCellValue(cell);
    }


    /**
     * Return the {@link Workbook workbook} to be found in the stream (assuming stream contains an Excel file). If
     * stream is <b>not</b> an Excel content, returns <code>null</code>.
     * 
     * @param request A non-null stream that eventually contains an Excel file. ExtractUrlTokens.java* @return The
     * {@link Workbook workbook} in stream or <code>null</code> if stream is not an Excel file.
     * @throws IOException
     */
    public static Workbook getWorkbook(SchemaParser.Request request) throws IOException {

        final Marker marker = Markers.dataset(request.getMetadata().getId());
        LOGGER.debug(marker, "opening");

        if (request == null) {
            throw new IOException("cannot read null stream");
        }

        try {
            return WorkbookFactory.create(request.getContent()); // wrapped in a PushbackInputStream in the called
                                                                 // method
        } catch (Exception e) {
            LOGGER.debug(marker, "does not seem to be a valid excel (.xls or .xlsx) file : {}", e.getMessage());
            return null;
        }

    }

}
