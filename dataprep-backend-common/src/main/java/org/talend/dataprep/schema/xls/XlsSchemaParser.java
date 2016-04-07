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

import static org.talend.dataprep.api.type.Type.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.ctc.wstx.sax.WstxSAXParserFactory;

/**
 * This class is in charge of parsing excel file (note apache poi is used for reading .xls) see https://poi.apache.org/
 */
@Service("parser#xls")
public class XlsSchemaParser implements SchemaParser {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsSchemaParser.class);

    /** Constant used to record blank cell. */
    private static final String BLANK = "blank";

    /**
     * @see SchemaParser#parse(Request)
     */
    @Override
    public SchemaParserResult parse(Request request) {

        final Marker marker = Markers.dataset(request.getMetadata().getId());

        LOGGER.debug(marker, "parsing {} ");

        try {
            List<SchemaParserResult.SheetContent> sheetContents = parseAllSheets(request);

            SchemaParserResult result;

            if (!sheetContents.isEmpty()) {
                // only one sheet
                if (sheetContents.size() == 1) {
                    result = SchemaParserResult.Builder.parserResult() //
                            .sheetContents(sheetContents) //
                            .draft(false) //
                            .build();
                }
                // multiple sheet, set draft flag on
                else {
                    result = SchemaParserResult.Builder.parserResult() //
                            .sheetContents(sheetContents) //
                            .draft(true) //
                            .sheetName(sheetContents.get(0).getName()) //
                            .build();
                }
            }
            // nothing to parse
            else {
                result = SchemaParserResult.Builder.parserResult() //
                        .sheetContents(Collections.emptyList()) //
                        .draft(false) //
                        .build();
            }

            return result;
        } catch (Exception e) {
            LOGGER.debug(marker, "IOException during parsing xls request :" + e.getMessage(), e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

    }


    /**
     * Parse all xls sheets.
     *
     * @param request the schema parser request.
     * @return the list of parsed xls sheet.
     * @throws IOException if an error occurs.
     */
    protected List<SchemaParserResult.SheetContent> parseAllSheets(Request request) throws IOException {

        InputStream inputStream = request.getContent();
        if (!inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }

        boolean newExcelFormat = XlsUtils.isNewExcelFormat(inputStream);

        // parse the xls input stream using the correct format
        if (newExcelFormat) {
            return parseAllSheetsNew(new Request(inputStream, request.getMetadata()));
        }
        else {
            return parseAllSheetsOldFormat(new Request(inputStream, request.getMetadata()));
        }
    }

    /**
     * parse excel document using SAX like technology (only available for modern excel documents)
     * 
     * @param request
     * @return
     */
    private List<SchemaParserResult.SheetContent> parseAllSheetsNew(Request request) {

        final Marker marker = Markers.dataset(request.getMetadata().getId());
        List<SchemaParserResult.SheetContent> schemas = new ArrayList<>();

        try {
            OPCPackage container = OPCPackage.open(request.getContent());

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
            XSSFReader xssfReader = new XSSFReader(container);
            List<String> activeSheetNames = XlsUtils.getActiveSheetsFromWorkbookSpec(xssfReader.getWorkbookData());
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            int i = 0;
            while (iterator.hasNext()) {
                try (InputStream sheetInputStream = iterator.next()) {
                    String sheetName = iterator.getSheetName();
                    if (!activeSheetNames.contains(sheetName)) {
                        // we ignore non active sheets
                        continue;
                    }

                    if (sheetInputStream.markSupported()) {
                        sheetInputStream.mark(1);
                    }

                    String dimension = XlsUtils.getDimension(sheetInputStream);
                    // the parsing may not find all columns so we complete using the found dimension from metadata
                    int colNum = XlsUtils.getColumnsNumberFromDimension(dimension);

                    // reset as read from metadata parsing
                    sheetInputStream.reset();
                    InputSource sheetSource = new InputSource(sheetInputStream);

                    DefaultSheetContentsHandler defaultSheetContentsHandler = new DefaultSheetContentsHandler(true);

                    XMLReader sheetParser = new WstxSAXParserFactory().newSAXParser().getXMLReader();
                    ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, defaultSheetContentsHandler, true);

                    sheetParser.setContentHandler(handler);
                    try {
                        sheetParser.parse(sheetSource);
                    } catch (FastStopParsingException e) {
                        // expected here as we stop after the first row
                        LOGGER.debug(marker, "FastStopParsingException : "+ e.getMessage());
                    }
                    SchemaParserResult.SheetContent sheetContent = //
                    new SchemaParserResult.SheetContent(StringUtils.isEmpty(sheetName) ? "sheet-" + i : sheetName, //
                            defaultSheetContentsHandler.columnsMetadata);

                    List<ColumnMetadata> columnsMetadata = sheetContent.getColumnMetadatas();

                    // if less columns found than the metadata we complete
                    if (columnsMetadata.size() < colNum) {
                        for (int j = 0; j < colNum; j++) {
                            columnsMetadata.add(ColumnMetadata.Builder //
                                    .column() //
                                    .name("col_" + j) //
                                    .type(Type.STRING) //
                                    .headerSize(1) //
                                    .build());
                        }
                    }
                    schemas.add(sheetContent);
                    i++;

                }
            }
        } catch (Exception e) {
            LOGGER.debug(marker, "IOException during parsing xls request :" + e.getMessage(), e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

        return schemas;
    }

    private static class FastStopParsingException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public FastStopParsingException() {
            // no op
        }
    }

    /**
     * Class used to read Xls sheet with a SAX parser (low memory footprint).
     * Throws a {@link FastStopParsingException} when parsing is finished even if the sheet is not finished.
     */
    static class DefaultSheetContentsHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        /** This class' logger. */
        private Logger logger = LoggerFactory.getLogger(getClass());
        /** The columns metadata. */
        private List<ColumnMetadata> columnsMetadata = new ArrayList<>();
        /** True if this content handler should throw a {@link FastStopParsingException} when finished to stop the processing. */
        private boolean fastStop;

        private int lastColumnNumber = 0;

        /**
         * Constructor.
         * @param fastStop if this content handler should throw a {@link FastStopParsingException} as soon as it's finished.
         */
        public DefaultSheetContentsHandler(boolean fastStop) {
            this.fastStop = fastStop;
        }

        /**
         * @see XSSFSheetXMLHandler.SheetContentsHandler#cell(String, String, XSSFComment)
         */
        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            logger.debug("cell {}", cellReference);

            int colNumber = XlsUtils.getColumnNumberFromCellRef( cellReference);

            // here we need to populate empty columns as we need to have same number for columns meta and values
            // so we check the difference with the last column number
            int colNumberDiff = colNumber - lastColumnNumber;
            if (colNumberDiff > 1) {
                // so populate here empty columns
                for (int i = colNumberDiff; i > 1; i--) {
                    addColumn(null);
                }
            }
            addColumn(formattedValue);
            lastColumnNumber = colNumber;
        }

        private void addColumn(String formattedValue) {
            String headerText = StringUtils.trim(formattedValue);
            // header text cannot be null so use a default one
            if (StringUtils.isEmpty(headerText)) {
                headerText = "col_" + (columnsMetadata.size() + 1); // +1 because it starts from 0
            }

            columnsMetadata.add(ColumnMetadata.Builder //
                    .column() //
                    .name(headerText) //
                    .type(Type.STRING) //
                    .headerSize(1) //
                    .build());
        }

        /**
         * @see XSSFSheetXMLHandler.SheetContentsHandler#startRow(int)
         */
        @Override
        public void startRow(int rowNum) {
            logger.debug("startRow {}", rowNum);
            if (rowNum > 0 && fastStop) {
                throw new FastStopParsingException();
            }
        }

        /**
         * @see XSSFSheetXMLHandler.SheetContentsHandler#endRow(int)
         */
        @Override
        public void endRow(int rowNum) {
            logger.debug("endRow {}", rowNum);
        }

        /**
         * @see XSSFSheetXMLHandler.SheetContentsHandler#headerFooter(String, boolean, String)
         */
        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            logger.debug("headerFooter");
        }
    }

    /**
     * Parse all xls sheets for old excel document type
     *
     * @param request the xls request.
     * @return The parsed sheets request.
     */
    private List<SchemaParserResult.SheetContent> parseAllSheetsOldFormat(Request request) {

        final Marker marker = Markers.dataset(request.getMetadata().getId());

        try {
            InputStream inputStream = request.getContent();
            if (!inputStream.markSupported()) {
                inputStream = new PushbackInputStream(inputStream, 8);
            }
            Workbook hssfWorkbook = WorkbookFactory.create(inputStream);

            if (hssfWorkbook == null) {
                throw new IOException("could not open " + request.getMetadata().getId() + " as an excel file");
            }

            int sheetNumber = hssfWorkbook.getNumberOfSheets();
            if (sheetNumber < 1) {
                LOGGER.debug(marker, "has not sheet to read");
                return Collections.emptyList();
            }

            List<SchemaParserResult.SheetContent> schemas = new ArrayList<>();
            for (int i = 0; i < sheetNumber; i++) {
                Sheet sheet = hssfWorkbook.getSheetAt(i);

                if (sheet.getLastRowNum() < 1) {
                    LOGGER.debug(marker, "sheet '{}' do not have rows skip ip", sheet.getSheetName());
                    continue;
                }

                List<ColumnMetadata> columnsMetadata = parsePerSheet(sheet, //
                        request.getMetadata().getId(), //
                        hssfWorkbook.getCreationHelper().createFormulaEvaluator());

                String sheetName = sheet.getSheetName();

                // update XlsSerializer if this default sheet naming change!!!
                schemas.add(new SchemaParserResult.SheetContent(sheetName == null ? "sheet-" + i : sheetName, columnsMetadata));

            }

            return schemas;

        } catch (Exception e) {
            LOGGER.debug(marker, "Exception during parsing xls request :" + e.getMessage(), e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Return the columns metadata for the given sheet.
     *
     * @param sheet the sheet to look at.
     * @param datasetId the dataset id.
     * @return the columns metadata for the given sheet.
     */
    private List<ColumnMetadata> parsePerSheet(Sheet sheet, String datasetId, FormulaEvaluator formulaEvaluator) {

        LOGGER.debug(Markers.dataset(datasetId), "parsing sheet '{}'", sheet.getSheetName());

        // Map<ColId, Map<RowId, type>>
        SortedMap<Integer, SortedMap<Integer, String>> cellsTypeMatrix = collectSheetTypeMatrix(sheet, formulaEvaluator);
        int averageHeaderSize = guessHeaderSize(cellsTypeMatrix);

        // here we have information regarding types for each rows/col (yup a Matrix!! :-) )
        // so we can analyse and guess metadata (column type, header value)
        final List<ColumnMetadata> columnsMetadata = new ArrayList<>(cellsTypeMatrix.size());

        cellsTypeMatrix.forEach((colId, typePerRowMap) -> {

            Type type = guessColumnType(colId, typePerRowMap, averageHeaderSize);

            String headerText = "col" + colId;
            if (averageHeaderSize == 1 && sheet.getRow(0) != null) {
                // so header value is the first row of the column
                Cell headerCell = sheet.getRow(0).getCell(colId);
                headerText = XlsUtils.getCellValueAsString(headerCell, formulaEvaluator);
            }

            // header text cannot be null so use a default one
            if (StringUtils.isEmpty(headerText)) {
                headerText = "col_" + (colId + 1); // +1 because it starts from 0
            }

            // FIXME what do we do if header size is > 1 concat all lines?
            columnsMetadata.add(ColumnMetadata.Builder //
                    .column() //
                    .headerSize(averageHeaderSize) //
                    .name(headerText) //
                    .type(type) //
                    .build());

        });

        return columnsMetadata;
    }

    /**
     *
     *
     * @param colId the column id.
     * @param columnRows all rows with previously guessed type: key=row number, value= guessed type
     * @param averageHeaderSize
     * @return
     */
    private Type guessColumnType(Integer colId, SortedMap<Integer, String> columnRows, int averageHeaderSize) {

        // calculate number per type

        Map<String, Long> perTypeNumber = columnRows.tailMap(averageHeaderSize).values() //
                .stream() //
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        OptionalLong maxOccurrence = perTypeNumber.values().stream().mapToLong(Long::longValue).max();

        if (!maxOccurrence.isPresent()) {
            return ANY;
        }

        List<String> duplicatedMax = new ArrayList<>();

        perTypeNumber.forEach((type1, aLong) -> {
            if (aLong >= maxOccurrence.getAsLong()) {
                duplicatedMax.add(type1);
            }
        });

        String guessedType;
        if (duplicatedMax.size() == 1) {
            guessedType = duplicatedMax.get(0);
        } else {
            // as we have more than one type we guess ANY
            guessedType = ANY.getName();
        }

        LOGGER.debug("guessed type for column #{} is {}", colId, guessedType);
        return Type.get(guessedType);
    }

    /**
     * We store (cell types per row) per column.
     *
     * @param sheet key is the column number, value is a Map with key row number and value Type
     * @return A Map&lt;colId, Map&lt;rowId, type&gt;&gt;
     */
    private SortedMap<Integer, SortedMap<Integer, String>> collectSheetTypeMatrix(Sheet sheet,
            FormulaEvaluator formulaEvaluator) {

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        LOGGER.debug("firstRowNum: {}, lastRowNum: {}", firstRowNum, lastRowNum);

        SortedMap<Integer, SortedMap<Integer, String>> cellsTypeMatrix = new TreeMap<>();

        // we start analysing rows
        for (int rowCounter = firstRowNum; rowCounter <= lastRowNum; rowCounter++) {

            int cellCounter = 0;

            Row row = sheet.getRow(rowCounter);
            if (row == null) {
                continue;
            }

            Iterator<Cell> cellIterator = row.cellIterator();

            String currentType;

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                int xlsType = Cell.CELL_TYPE_STRING;

                try {
                    xlsType = cell.getCellType() == Cell.CELL_TYPE_FORMULA ? //
                            formulaEvaluator.evaluate(cell).getCellType() : cell.getCellType();
                } catch (Exception e) {
                    // ignore formula error evaluation get as a String with the formula
                }
                switch (xlsType) {
                case Cell.CELL_TYPE_BOOLEAN:
                    currentType = BOOLEAN.getName();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    currentType = getTypeFromNumericCell(cell);
                    break;
                case Cell.CELL_TYPE_BLANK:
                    currentType = BLANK;
                    break;
                case Cell.CELL_TYPE_FORMULA:
                case Cell.CELL_TYPE_STRING:
                    currentType = STRING.getName();
                    break;
                case Cell.CELL_TYPE_ERROR:
                    // we cannot really do anything with an error
                default:
                    currentType = ANY.getName();
                }

                SortedMap<Integer, String> cellInfo = cellsTypeMatrix.get(cellCounter);

                if (cellInfo == null) {
                    cellInfo = new TreeMap<>();
                }
                cellInfo.put(rowCounter, currentType);

                cellsTypeMatrix.put(cellCounter, cellInfo);
                cellCounter++;
            }
        }

        LOGGER.trace("cellsTypeMatrix: {}", cellsTypeMatrix);
        return cellsTypeMatrix;
    }

    private String getTypeFromNumericCell(Cell cell) {
        try {
            return HSSFDateUtil.isCellDateFormatted(cell) ? DATE.getName() : NUMERIC.getName();
        } catch (IllegalStateException e) {
            return ANY.getName();
        }
    }

    /**
     * <p>
     * As we can try to be smart and user friendly and not those nerd devs who doesn't mind about users so we try to
     * guess the header size (we assume those bloody users don't have complicated headers!!)
     * </p>
     * <p>
     * We scan all entries to find a common header size value (i.e row line with value type change) more simple all
     * columns/lines with type String
     * </p>
     *
     * @param cellsTypeMatrix key: column number value: row where the type change from String to something else
     * @return The guessed header size.
     */
    private int guessHeaderSize(Map<Integer, SortedMap<Integer, String>> cellsTypeMatrix) {
        SortedMap<Integer, Integer> cellTypeChange = new TreeMap<>();

        cellsTypeMatrix.forEach((colId, typePerRow) -> {

            String firstType = null;
            int rowChange = 0;

            for (Map.Entry<Integer, String> typePerRowEntry : typePerRow.entrySet()) {
                if (firstType == null) {
                    firstType = typePerRowEntry.getValue();
                } else {
                    if (!typePerRowEntry.getValue().equals(firstType) && !typePerRowEntry.getValue().equals(STRING.getName())) {
                        rowChange = typePerRowEntry.getKey();
                        break;
                    }
                }
            }

            cellTypeChange.put(colId, rowChange);

            firstType = null;
            rowChange = 0;

        });

        // average cell type change
        // double averageHeaderSizeDouble =
        // cellTypeChange.values().stream().mapToInt(Integer::intValue).average().getAsDouble();
        // int averageHeaderSize = (int) Math.ceil(averageHeaderSizeDouble);

        // FIXME think more about header size calculation
        // currently can fail so force an header of size 1
        int averageHeaderSize = 1;
        LOGGER.debug("averageHeaderSize (forced to): {}, cellTypeChange: {}", averageHeaderSize, cellTypeChange);
        return averageHeaderSize;
    }

}
