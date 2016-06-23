// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.schema.xls.streaming;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.monitorjbl.xlsx.exceptions.CloseException;
import com.monitorjbl.xlsx.impl.StreamingCell;
import com.monitorjbl.xlsx.impl.StreamingRow;

public class StreamingSheetReader implements Iterable<Row> {

    private static final Logger LOGGER = LoggerFactory.getLogger( StreamingSheetReader.class);

    private final SharedStringsTable sst;

    private final StylesTable stylesTable;

    private final XMLEventReader parser;

    private final DataFormatter dataFormatter = new CustomDataFormatter();

    private int rowCacheSize;

    private List<Row> rowCache = new ArrayList<>();

    private Iterator<Row> rowCacheIterator;

    private int colNumber = 0;

    private String lastContents;

    private StreamingRow currentRow;

    private StreamingCell currentCell;

    // we need to track empty rows
    private int firstRowIndex = -1;

    private boolean parsingCols = false;

    // sheet dimension
    // <dimension ref="A1:B60"/>
    private String dimension = StringUtils.EMPTY;

    public StreamingSheetReader(SharedStringsTable sst, StylesTable stylesTable, XMLEventReader parser, int rowCacheSize) {
        this.sst = sst;
        this.stylesTable = stylesTable;
        this.parser = parser;
        this.rowCacheSize = rowCacheSize;
    }

    /**
     * Read through a number of rows equal to the rowCacheSize field or until there is no more data to read
     *
     * @return true if data was read
     */
    private boolean getRow() {
        try {
            rowCache.clear();
            while (rowCache.size() < rowCacheSize && parser.hasNext()) {
                handleEvent(parser.nextEvent());
            }
            rowCacheIterator = rowCache.iterator();
            return rowCacheIterator.hasNext();
        } catch (XMLStreamException | SAXException e) {
            LOGGER.debug( "End of stream");
        }
        return false;
    }

    public int getColNumber() {
        // the last col element is the aggregation of end of columns so it's not a real one
        return colNumber - 1;
    }

    public int getFirstRowIndex() {
        return firstRowIndex;
    }

    public String getDimension() {
        return dimension;
    }

    /**
     * Handles a Stream event.
     *
     * @param event
     * @throws SAXException
     */
    private void handleEvent(XMLEvent event) throws SAXException {
        if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
            Characters c = event.asCharacters();
            lastContents += c.getData();
        } else if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
            StartElement startElement = event.asStartElement();
            String tagLocalName = startElement.getName().getLocalPart();


            if ("row".equals(tagLocalName)) {
                Attribute rowIndex = startElement.getAttributeByName(new QName("r"));
                if (firstRowIndex == -1) {
                    firstRowIndex = Integer.parseInt(rowIndex.getValue());
                }
                currentRow = new StreamingRow(Integer.parseInt(rowIndex.getValue()) - 1);
            } else if ("cols".equals(tagLocalName)) {
                parsingCols = true;
            } else if ("col".equals(tagLocalName) && parsingCols) {
                colNumber = colNumber +1;
            } else if ("c".equals(tagLocalName)) {
                Attribute ref = startElement.getAttributeByName(new QName("r"));

                String[] coord = ref.getValue().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                currentCell = new StreamingCell(CellReference.convertColStringToIndex(coord[0]), Integer.parseInt(coord[1]) - 1);
                setFormatString(startElement, currentCell);

                Attribute type = startElement.getAttributeByName(new QName("t"));
                if (type != null) {
                    currentCell.setType(type.getValue());
                } else {
                    currentCell.setType("n");
                }

                Attribute style = startElement.getAttributeByName(new QName("s"));
                if (style != null) {
                    String indexStr = style.getValue();
                    try {
                        int index = Integer.parseInt(indexStr);
                        currentCell.setCellStyle(stylesTable.getStyleAt(index));
                    } catch (NumberFormatException nfe) {
                        LOGGER.warn( "Ignoring invalid style index {}", indexStr);
                    }
                }
            // we store the dimension as well to revert with this method when cols not found
            // can happen see xlsx attached here https://jira.talendforge.org/browse/TDP-1957
            // <dimension ref="A1:B60"/>
            } else if ("dimension".equals( tagLocalName )) {
                Attribute attribute = startElement.getAttributeByName( new QName( "ref" ) );
                if (attribute != null){
                    this.dimension = attribute.getValue();
                }
            }

            // Clear contents cache
            lastContents = "";
        } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
            EndElement endElement = event.asEndElement();
            String tagLocalName = endElement.getName().getLocalPart();

            if ("v".equals(tagLocalName) || "t".equals(tagLocalName)) {
                currentCell.setRawContents(unformattedContents());
                currentCell.setContents(formattedContents());
            } else if ("row".equals(tagLocalName) && currentRow != null) {
                rowCache.add(currentRow);
            } else if ("c".equals(tagLocalName)) {
                currentRow.getCellMap().put(currentCell.getColumnIndex(), currentCell);
            } else if ("cols".equals(tagLocalName)) {
                parsingCols = false;
            }

        }
    }

    /**
     * Read the numeric format string out of the styles table for this cell. Stores the result in the Cell.
     *
     * @param startElement
     * @param cell
     */
    void setFormatString(StartElement startElement, StreamingCell cell) {
        Attribute cellStyle = startElement.getAttributeByName(new QName("s"));
        String cellStyleString = (cellStyle != null) ? cellStyle.getValue() : null;
        XSSFCellStyle style = null;

        if (cellStyleString != null) {
            style = stylesTable.getStyleAt(Integer.parseInt(cellStyleString));
        } else if (stylesTable.getNumCellStyles() > 0) {
            style = stylesTable.getStyleAt(0);
        }

        if (style != null) {
            cell.setNumericFormatIndex(style.getDataFormat());
            String formatString = style.getDataFormatString();

            if (formatString != null) {
                cell.setNumericFormat(formatString);
            } else {
                cell.setNumericFormat(BuiltinFormats.getBuiltinFormat(cell.getNumericFormatIndex()));
            }
        } else {
            cell.setNumericFormatIndex(null);
            cell.setNumericFormat(null);
        }
    }

    /**
     * Tries to format the contents of the last contents appropriately based on the type of cell and the discovered
     * numeric format.
     *
     * @return
     */
    String formattedContents() {
        switch (currentCell.getType()) {
        case "s": // string stored in shared table
            int idx = Integer.parseInt(lastContents);
            return new XSSFRichTextString(sst.getEntryAt(idx)).toString();
        case "inlineStr": // inline string (not in sst)
            return new XSSFRichTextString(lastContents).toString();
        case "str": //
            return lastContents;
        case "e": // error type
            return StringUtils.EMPTY;// "ERROR:  " + lastContents;
        case "n": // numeric type
            if (currentCell.getNumericFormat() != null && lastContents.length() > 0) {
                return dataFormatter.formatRawCellContents(Double.parseDouble(lastContents), currentCell.getNumericFormatIndex(),
                        currentCell.getNumericFormat());
            } else {
                return lastContents;
            }
        default:
            return lastContents;
        }
    }

    /**
     * Returns the contents of the cell, with no formatting applied
     *
     * @return
     */
    String unformattedContents() {
        switch (currentCell.getType()) {
        case "s": // string stored in shared table
            int idx = Integer.parseInt(lastContents);
            return new XSSFRichTextString(sst.getEntryAt(idx)).toString();
        case "inlineStr": // inline string (not in sst)
            return new XSSFRichTextString(lastContents).toString();
        default:
            return lastContents;
        }
    }

    /**
     * Returns a new streaming iterator to loop through rows. This iterator is not guaranteed to have all rows in
     * memory, and any particular iteration may trigger a load from disk to read in new data.
     *
     * @return the streaming iterator
     */
    @Override
    public Iterator<Row> iterator() {
        return new StreamingRowIterator();
    }

    public void close() {
        try {
            parser.close();
        } catch (XMLStreamException e) {
            throw new CloseException(e);
        }
    }

    class StreamingRowIterator implements Iterator<Row> {

        public StreamingRowIterator() {
            if (rowCacheIterator == null) {
                hasNext();
            }
        }

        @Override
        public boolean hasNext() {
            return (rowCacheIterator != null && rowCacheIterator.hasNext()) || getRow();
        }

        @Override
        public Row next() {
            return rowCacheIterator.next();
        }

        @Override
        public void remove() {
            throw new RuntimeException("NotSupported");
        }
    }


    class CustomDataFormatter extends DataFormatter {

        @Override
        public String formatRawCellContents(double value, int formatIndex, String formatString, boolean use1904Windowing) {
            // TDP-1656 (olamy) for some reasons poi use date format with only 2 digits for years
            // even the excel data ws using 4 so force the pattern here
            if ( DateUtil.isValidExcelDate( value) && StringUtils.countMatches( formatString, "y") == 2) {
                formatString = StringUtils.replace(formatString, "yy", "yyyy");
            }
            if (DateUtil.isValidExcelDate(value) && StringUtils.countMatches(formatString, "Y") == 2) {
                formatString = StringUtils.replace(formatString, "YY", "YYYY");
            }
            return super.formatRawCellContents(value, formatIndex, formatString, use1904Windowing);

        }
    }
}
