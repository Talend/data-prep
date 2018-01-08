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

package org.talend.dataprep.schema.xls;

import static org.talend.dataprep.api.type.Type.*;
import static org.talend.dataprep.i18n.DataprepBundle.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.xls.streaming.StreamingReader;
import org.talend.dataprep.schema.xls.streaming.StreamingSheet;

/**
 * This class is in charge of parsing excel file (note apache poi is used for reading .xls)
 *
 * @see <a hrerf="https://poi.apache.org/
 */
@Service("parser#xls")
public class XlsSchemaParser implements SchemaParser {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XlsSchemaParser.class);

    /** Constant used to record blank cell. */
    private static final String BLANK = "blank";

    @Value("${dataset.import.xls.size.column.max:1000}")
    private int maxNumberOfColumns;

    /**
     * @see SchemaParser#parse(Request)
     */
    @Override
    public Schema parse(Request request) {
        final Marker marker = Markers.dataset(request.getMetadata().getId());
        LOGGER.debug(marker, "parsing {} ");
        try {
            List<Schema.SheetContent> sheetContents = parseAllSheets(request);
            Schema result;
            if (!sheetContents.isEmpty()) {
                // only one sheet
                if (sheetContents.size() == 1) {
                    result = Schema.Builder.parserResult() //
                            .sheetContents(sheetContents) //
                            .draft(false) //
                            .sheetName(sheetContents.get(0).getName()) //
                            .build();
                } else {
                    // multiple sheet, set draft flag on
                    result = Schema.Builder.parserResult() //
                            .sheetContents(sheetContents) //
                            .draft(true) //
                            .sheetName(sheetContents.get(0).getName()) //
                            .build();
                }
            }
            // nothing to parse
            else {
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT);
            }
            return result;
        } catch (TDPException e) {
            throw e;
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
    protected List<Schema.SheetContent> parseAllSheets(Request request) throws IOException {
        InputStream inputStream = request.getContent();
        if (!inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }
        boolean newExcelFormat = XlsUtils.isNewExcelFormat(inputStream);
        // parse the xls input stream using the correct format
        if (newExcelFormat) {
            return parseAllSheetsStream(new Request(inputStream, request.getMetadata()));
        } else {
            return parseAllSheetsOldFormat(new Request(inputStream, request.getMetadata()));
        }
    }

    private List<Schema.SheetContent> parseAllSheetsStream(Request request) {
        Workbook workbook = StreamingReader.builder() //
                .bufferSize(4096) //
                .rowCacheSize(1) //
                .open(request.getContent());
        try {
            List<Schema.SheetContent> schemas = new ArrayList<>();
            int sheetNumber = 0;
            for (Sheet sheet : workbook) {
                List<ColumnMetadata> columnsMetadata = createMetadataFromFirstNonEmptyRowAndInitSheet(sheet);
                int totalColumnsNumber = getTotalColumnsNumber((StreamingSheet) sheet);

                /*
                 * Protecting the app against too large data sets => It would break mongo by submitting too large empty
                 * column metadata or saturate the memory during analysis.
                 *
                 * @see https://jira.talendforge.org/browse/TDP-3459
                 */
                if (totalColumnsNumber > maxNumberOfColumns) {
                    throw new TDPException(DataSetErrorCodes.DATASET_HAS_TOO_MANY_COLUMNS, ExceptionContext.build()
                            .put("number-of-columns", totalColumnsNumber).put("max-allowed", maxNumberOfColumns));
                }

                String sheetName = sheet.getSheetName();
                Schema.SheetContent sheetContent = new Schema.SheetContent(
                        StringUtils.isEmpty(sheetName) ? "sheet-" + sheetNumber : sheetName, columnsMetadata);

                // if less columns found than the metadata we complete
                completeWithEmptyColumnsMetadata(columnsMetadata, totalColumnsNumber);
                schemas.add(sheetContent);
            }
            return schemas;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                LOGGER.error("Unable to close excel file.", e);
            }
        }
    }

    private int getTotalColumnsNumber(StreamingSheet sheet) {
        int maxColNumber = sheet.getReader().getColNumber();
        String dimension = sheet.getReader().getDimension();

        if (StringUtils.isNotEmpty(dimension)) {
            int maxColNumberFromDimension = XlsUtils.getColumnsNumberFromDimension(dimension);
            // well for some files they can disagree so we use the biggest one
            if (maxColNumberFromDimension > maxColNumber) {
                maxColNumber = maxColNumberFromDimension;
            }
        }
        return maxColNumber;
    }

    private void completeWithEmptyColumnsMetadata(List<ColumnMetadata> columnsMetadata, int maxColNumber) {
        int numberOfColumnsAlreadyFound = columnsMetadata.size();
        if (numberOfColumnsAlreadyFound < maxColNumber) {
            int columnId = 0;
            for (int appendedColumnId = numberOfColumnsAlreadyFound; appendedColumnId < maxColNumber; appendedColumnId++) {
                columnsMetadata.add(ColumnMetadata.Builder //
                        .column() //
                        .name("col_" + columnId++) //
                        .type(Type.STRING) //
                        .headerSize(1) //
                        .build());
            }
        }
    }

    private List<ColumnMetadata> createMetadataFromFirstNonEmptyRowAndInitSheet(Sheet sheet) {
        List<ColumnMetadata> columnsMetadata = new ArrayList<>();
        int rowNumber = 0;
        for (Row r : sheet) {
            if (rowNumber < 1) {
                if (((StreamingSheet) sheet).getReader().getFirstRowIndex() == 1) {
                    for (Cell c : r) {
                        String headerText = StringUtils.trim(c.getStringCellValue());
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
                }
            } else {
                break;
            }
            rowNumber++;
        }
        return columnsMetadata;
    }

    /**
     * Parse all xls sheets for old excel document type
     *
     * @param request the xls request.
     * @return The parsed sheets request.
     */
    private List<Schema.SheetContent> parseAllSheetsOldFormat(Request request) {

        final Marker marker = Markers.dataset(request.getMetadata().getId());

        try {
            InputStream inputStream = request.getContent();
            if (!inputStream.markSupported()) {
                inputStream = new PushbackInputStream(inputStream, 8);
            }
            Workbook hssfWorkbook = WorkbookFactory.create(inputStream);

            List<Schema.SheetContent> schemas;
            try {
                if (hssfWorkbook == null) {
                    throw new IOException("could not open " + request.getMetadata().getId() + " as an excel file");
                }
                int sheetNumber = hssfWorkbook.getNumberOfSheets();
                if (sheetNumber < 1) {
                    LOGGER.debug(marker, "has not sheet to read");
                    return Collections.emptyList();
                }
                schemas = new ArrayList<>();
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
                    schemas.add(new Schema.SheetContent(sheetName == null ? "sheet-" + i : sheetName, columnsMetadata));
                }
            } finally {
                hssfWorkbook.close();
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

            String headerText = null;
            if (averageHeaderSize == 1 && sheet.getRow(0) != null) {
                // so header value is the first row of the column
                Cell headerCell = sheet.getRow(0).getCell(colId);
                headerText = XlsUtils.getCellValueAsString(headerCell, formulaEvaluator);
            }

            // header text cannot be null so use a default one
            if (StringUtils.isEmpty(headerText)) {
                // +1 because it starts from 0
                headerText = message("import.local.generated_column_name", colId + 1);
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

        // FIXME think more about header size calculation
        // currently can fail so force an header of size 1
        int averageHeaderSize = 1;
        LOGGER.debug("averageHeaderSize (forced to): {}, cellTypeChange: {}", averageHeaderSize, cellTypeChange);
        return averageHeaderSize;
    }

}
