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

package org.talend.dataprep.schema.xls;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * some utils methods for excel files
 */
public class XlsUtils {

    /** this class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsUtils.class);

    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputSingletonHolder.xmlInputFactory;

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
    public static String getCellValueAsString(Cell cell, FormulaEvaluator formulaEvaluator) {
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
            try {
                return getCellValueAsString(cell, formulaEvaluator.evaluate(cell));
            } catch (Exception e) {
                // log error message and the formula
                LOGGER.warn("Unable to evaluate cell (line: {}, col: {}) with formula '{}': {}", cell.getRowIndex(),
                        cell.getColumnIndex(), cell.getCellFormula(), e.getMessage(), e);
                return StringUtils.EMPTY;
            }
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

        if (cellValue == null) {
            return formatter.formatCellValue(cell);
        }

        return fromFormula ? cellValue.formatAsString() : formatter.formatCellValue(cell);
    }

    /**
     * Detect the excel format with only peeking at the first 8 bytes of the input stream (leaving the stream untouched).
     *
     * @param inputStream the xls input stream.
     * @return true if the given input stream is a xls new format.
     * @throws IOException if an error occurs.
     */
    public static boolean isNewExcelFormat(InputStream inputStream) throws IOException {

        boolean newFormat = false;

        // Ensure that there is at least some data there
        byte[] headers = IOUtils.peekFirst8Bytes(inputStream);

        if (NPOIFSFileSystem.hasPOIFSHeader(headers)) {
            newFormat = false;
        }
        if (POIXMLDocument.hasOOXMLHeader(new ByteArrayInputStream(headers))) {
            newFormat = true;
        }

        return newFormat;

    }

    /**
     * read workbook xml spec to get non hidden sheets
     *
     * @param inputStream
     * @return
     */
    public static List<String> getActiveSheetsFromWorkbookSpec(InputStream inputStream) throws XMLStreamException {
        // If doesn't support mark, wrap up
        if (!inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }
        XMLStreamReader streamReader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream);
        try {
            /*
             *
             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> <workbook
             * xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
             * xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"> <fileVersion appName="xl"
             * lastEdited="5" lowestEdited="5" rupBuild="9303" codeName="{8C4F1C90-05EB-6A55-5F09-09C24B55AC0B}"/>
             * <workbookPr codeName="ThisWorkbook" defaultThemeVersion="124226"/> <bookViews> <workbookView xWindow="0"
             * yWindow="732" windowWidth="22980" windowHeight="8868" firstSheet="1" activeTab="8"/> </bookViews>
             * <sheets> <sheet name="formdata" sheetId="4" state="hidden" r:id="rId1"/> <sheet name="MONDAY" sheetId="1"
             * r:id="rId2"/> <sheet name="TUESDAY" sheetId="8" r:id="rId3"/> <sheet name="WEDNESDAY" sheetId="10"
             * r:id="rId4"/> <sheet name="THURSDAY" sheetId="11" r:id="rId5"/> <sheet name="FRIDAY" sheetId="12"
             * r:id="rId6"/> <sheet name="SATURDAY" sheetId="13" r:id="rId7"/> <sheet name="SUNDAY" sheetId="14"
             * r:id="rId8"/> <sheet name="WEEK SUMMARY" sheetId="15" r:id="rId9"/> </sheets>
             *
             */
            // we only want sheets not with state=hidden

            List<String> names = new ArrayList<>();

            while (streamReader.hasNext()) {
                switch (streamReader.next()) {
                case START_ELEMENT:
                    if (StringUtils.equals(streamReader.getLocalName(), "sheet")) {
                        Map<String, String> attributesValues = getAttributesNameValue(streamReader);
                        if (!attributesValues.isEmpty()) {
                            String sheetState = attributesValues.get("state");
                            if (!StringUtils.equals(sheetState, "hidden")) {
                                String sheetName = attributesValues.get("name");
                                names.add(sheetName);
                            }
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (StringUtils.equals(streamReader.getLocalName(), "sheets")) {
                        // shortcut to stop parsing
                        return names;
                    }
                    break;
                default:
                    // no op
                }
            }
            return names;
        } finally {
            if (streamReader != null) {
                streamReader.close();
            }
        }
    }

    /**
     *
     * @param inputStream xls sheet inputStream
     * @return the column number from reading sheet metadata or -1 if unknown
     * @throws XMLStreamException
     * @throws IOException
     */
    public static int getColumnsNumber(InputStream inputStream) throws XMLStreamException, IOException {
        // If doesn't support mark, wrap up
        if (!inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }

        int colNumber = 0;

        // TDP-1781 xlsx files may not containing dimension so we fallback to col element number

        XMLStreamReader streamReader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream);
        try {
            while (streamReader.hasNext()) {
                switch (streamReader.next()) {
                case START_ELEMENT:
                    if (StringUtils.equals(streamReader.getLocalName(), "dimension")) {
                        Map<String, String> attributesValues = getAttributesNameValue(streamReader);
                        if (!attributesValues.isEmpty()) {
                            return getColumnsNumberFromDimension(attributesValues.get("ref"));
                        }
                    }
                    if (StringUtils.equals(streamReader.getLocalName(), "col")) {
                        colNumber++;
                    }
                    break;
                case END_ELEMENT:
                    if (StringUtils.equals(streamReader.getLocalName(), "cols")) {
                        return colNumber;
                    }
                default:
                    // no op
                }
            }
        } finally {
            if (streamReader != null) {
                streamReader.close();
            }
        }
        return -1;
    }

    /**
     * transform all attributes to a Map (key: att name, value: att value)
     *
     * @param streamReader
     * @return
     */
    private static Map<String, String> getAttributesNameValue(XMLStreamReader streamReader) {
        int size = streamReader.getAttributeCount();
        if (size > 0) {
            Map<String, String> attributesValues = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String attributeName = streamReader.getAttributeLocalName(i);
                String attributeValue = streamReader.getAttributeValue(i);
                if (StringUtils.isNotEmpty(attributeName)) {
                    attributesValues.put(attributeName, attributeValue);
                }
            }
            return attributesValues;
        }
        return Collections.emptyMap();
    }

    /**
     * xlsx xml contains information for the dimension in a format as "B1:AG142" A1:D5 so the column number is given
     * by the letters from the second part. we don't mind about the start we always start from A1 as data-prep doesn't
     * want to ignore empty columns
     *
     * @param dimension
     * @return 0 if <code>null</code>
     */
    public static int getColumnsNumberFromDimension(String dimension) {
        if (StringUtils.isEmpty(dimension)) {
            return 0;
        }
        String[] parts = StringUtils.split(dimension, ':');

        if (parts.length < 2) {
            return 0;
        }
        String secondPart = parts[1];

        return getColumnNumberFromCellRef(secondPart) + 1;
    }

    /**
     * return the column number from a cell reference (AA242)
     *
     * @param lastCell
     * @return
     */
    public static int getColumnNumberFromCellRef(String lastCell) {

        StringBuilder letters = new StringBuilder();
        // get all letters to remove row number
        StringCharacterIterator iter = new StringCharacterIterator(lastCell);
        for (char c = iter.first(); c != StringCharacterIterator.DONE; c = iter.next()) {
            if (!NumberUtils.isNumber(String.valueOf(c))) {
                letters.append(c);
            }
        }
        // use poi api to calculate column number from an excell column format
        return CellReference.convertColStringToIndex(letters.toString());

    }

    private static class XMLInputSingletonHolder {

        private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

        static {
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        }
    }
}
