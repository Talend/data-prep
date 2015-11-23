package org.talend.dataprep.schema.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    public static String getCellValueAsString(Cell cell) {
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
            return cell.getCellFormula();
        case Cell.CELL_TYPE_NUMERIC:
            return getNumericValue(cell);
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
    private static String getNumericValue(Cell cell) {
        // Date is typed as numeric
        if (HSSFDateUtil.isCellDateFormatted(cell)) { // TODO configurable??
            DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            return sdf.format(cell.getDateCellValue());
        }
        // Numeric type (use data formatter to get number format right)
        DataFormatter formatter = new HSSFDataFormatter(Locale.ENGLISH);
        return formatter.formatCellValue(cell);
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

        // Depending on the excel file used the poi object to use is different
        // so we try one (catch exception then try the other one)
        // TODO that's a pain as we have to keep this :-(
        // TODO use ByteBuffer or mark/reset the input if supported ?
        // but for some reasons new HSSFWorkbook consume part of the stream
        if (request == null) {
            throw new IOException("cannot read null stream");
        }

        byte[] bytes = IOUtils.toByteArray(request.getContent());
        try {
            final XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
            LOGGER.debug(marker, "opened as XSSFWorkbook (.xlsx)");
            return workbook;
        } catch (Exception e) {
            LOGGER.debug(marker, "does not seem to be a XSSFWorkbook (.xlsx) : {}", e.getMessage());
            try {
                final HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes));
                LOGGER.debug(marker, "opened as HSSFWorkbook (.xls)");
                return workbook;
            } catch (Exception e1) {
                LOGGER.debug(marker, "does not seem to be a HSSFWorkbook (.xls) neither : {}", e1.getMessage());
                return null;
            }
        }

    }

}
