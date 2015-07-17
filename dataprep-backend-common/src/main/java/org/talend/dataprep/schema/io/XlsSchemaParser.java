package org.talend.dataprep.schema.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

/**
 * This class is responsible to parse excel file (note poi is used for reading .xls)
 */
@Service("parser#xls")
public class XlsSchemaParser implements SchemaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsSchemaParser.class);

    @Override
    public SchemaParserResult parse(Request request) {

        // FIXME ATM only first sheet but need to be discuss
        // maybe return List<List<ColumnMetadata>> ??
        // so we could return all sheets

        List<SchemaParserResult.SheetContent> sheetContents = parseAllSheets(request.getContent());

        if (!sheetContents.isEmpty()) {
            return sheetContents.size() == 1 ? //
            SchemaParserResult.Builder.parserResult() //
                    .sheetContents(sheetContents) //
                    .draft(false) //
                    .build() //
                    : //
                    SchemaParserResult.Builder.parserResult() //
                            .sheetContents(sheetContents) //
                            .draft(true) //
                            .sheetName(sheetContents.get(0).getName()) //
                            .build();
        }

        return SchemaParserResult.Builder.parserResult() //
                .sheetContents(Collections.emptyList()) //
                .draft(false) //
                .build();

    }

    public List<SchemaParserResult.SheetContent> parseAllSheets(InputStream content) {
        try {
            Workbook hssfWorkbook = XlsUtils.getWorkbook(content);

            int sheetNumber = hssfWorkbook.getNumberOfSheets();

            if (sheetNumber < 1) {
                return Collections.emptyList();
            }

            List<SchemaParserResult.SheetContent> schemas = new ArrayList<>();

            for (int i = 0; i < sheetNumber; i++) {
                Sheet sheet = hssfWorkbook.getSheetAt(i);

                if (sheet.getLastRowNum() < 1) {
                    LOGGER.debug("sheet '{}' do not have rows skip ip", sheet.getSheetName());
                    continue;
                }

                List<ColumnMetadata> columnMetadatas = parsePerSheet(sheet);

                String sheetName = sheet.getSheetName();

                // update XlsSerializer if this default sheet naming change!!!
                schemas.add(new SchemaParserResult.SheetContent(sheetName == null ? "sheet-" + i : sheetName, columnMetadatas));

            }

            return schemas;

        } catch (IOException e) {
            LOGGER.debug("IOEXception during parsing xls content :" + e.getMessage(), e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    protected List<ColumnMetadata> parsePerSheet(Sheet sheet) {

        SortedMap<Integer, SortedMap<Integer, Type>> cellsTypeMatrix = collectSheetTypeMatrix(sheet);

        LOGGER.trace("cellsTypeMatrix: {}", cellsTypeMatrix);

        Map<Integer, Integer> cellTypeChange = guessHeaderChange(cellsTypeMatrix);

        // average cell type change
        // double averageHeaderSizeDouble =
        // cellTypeChange.values().stream().mapToInt(Integer::intValue).average().getAsDouble();
        // int averageHeaderSize = (int) Math.ceil(averageHeaderSizeDouble);

        // FIXME think more about header size calculation
        // currently can fail so force an header of size 1
        int averageHeaderSize = 1;

        LOGGER.debug("averageHeaderSize: {}, cellTypeChange: {}", averageHeaderSize, cellTypeChange);

        // here we have informations regarding types for each rows/col (yup a Matrix!! :-) )
        // so we can analyse and guess metadatas (column type, header value)
        final List<ColumnMetadata> columnMetadatas = new ArrayList<>(cellsTypeMatrix.size());

        cellsTypeMatrix.forEach((integer, integerTypeSortedMap) -> {

            Type type = guessColumnType(integerTypeSortedMap, averageHeaderSize);

            String headerText = "col" + integer;
            if (averageHeaderSize == 1) {
                // so header value is the first row of the column
                Cell headerCell = sheet.getRow(0).getCell(integer);
                headerText = XlsUtils.getCellValueAsString(headerCell);
            }

            // header text cannot be null so use a default one
            if (StringUtils.isEmpty(headerText)) {
                headerText = "col_" + integer;
            }

            // FIXME what do we do if header size is > 1 concat all lines?

            columnMetadatas.add(ColumnMetadata.Builder //
                    .column() //
                    .headerSize(averageHeaderSize) //
                    .id(integer) //
                    .name(headerText) //
                    .type(type) //
                    .build());

        });

        return columnMetadatas;
    }

    /**
     * 
     * @param columnRows all rows with previously guessed type: key=row number, value= guessed type
     * @param averageHeaderSize
     * @return
     */
    protected Type guessColumnType(SortedMap<Integer, Type> columnRows, int averageHeaderSize) {

        // calculate number per type

        Map<Type, Long> perTypeNumber = columnRows.tailMap(averageHeaderSize).values() //
                .stream() //
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        OptionalLong maxOccurrence = perTypeNumber.values().stream().mapToLong(Long::longValue).max();

        if (!maxOccurrence.isPresent()) {
            return Type.ANY;
        }

        List<Type> duplicatedMax = new ArrayList<>();

        perTypeNumber.forEach((type1, aLong) -> {
            if (aLong >= maxOccurrence.getAsLong()) {
                duplicatedMax.add(type1);
            }
        });

        if (duplicatedMax.size() == 1) {
            return duplicatedMax.get(0);
        }

        // as we have more than one type we guess ANY
        return Type.ANY;
    }

    /**
     * we store cell types per with the row list
     * 
     * @param sheet key is the column number, value is a Map with key row number and value Type
     * @return
     */
    protected SortedMap<Integer, SortedMap<Integer, Type>> collectSheetTypeMatrix(Sheet sheet) {

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        LOGGER.debug("firstRowNum: {}, lastRowNum: {}", firstRowNum, lastRowNum);

        SortedMap<Integer, SortedMap<Integer, Type>> cellsTypeMatrix = new TreeMap<>();

        // we start analysing rows
        for (int rowCounter = firstRowNum; rowCounter <= lastRowNum; rowCounter++) {

            int cellCounter = 0;

            Row row = sheet.getRow(rowCounter);

            if (row == null) {
                continue;
            }

            Iterator<Cell> cellIterator = row.cellIterator();

            Type currentType;

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    currentType = Type.BOOLEAN;
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (HSSFDateUtil.isCellDateFormatted( cell )){
                        currentType = Type.DATE;
                    } else {
                        currentType = Type.NUMERIC;
                    }
                    break;
                case Cell.CELL_TYPE_BLANK:
                    continue;
                case Cell.CELL_TYPE_STRING:
                    currentType = Type.STRING;
                    break;
                case Cell.CELL_TYPE_ERROR | Cell.CELL_TYPE_FORMULA:
                    // we cannot really do anything with a formula
                default:
                    currentType = Type.ANY;

                }

                SortedMap<Integer, Type> cellInfo = cellsTypeMatrix.get(cellCounter);

                if (cellInfo == null) {
                    cellInfo = new TreeMap<>();
                }
                cellInfo.put(rowCounter, currentType);

                cellsTypeMatrix.put(cellCounter, cellInfo);
                cellCounter++;
            }

        }

        return cellsTypeMatrix;
    }

    /**
     * <p>
     * As we can try to be smart and user friendly and not those nerd devs who doesn't mind about users so we try to
     * guess the header size (we assume those bloody users don't have complicated headers!!)
     * </p>
     * <p>
     * we scan all entries to find a common header size value (i.e row line with value type change) more simple all
     * columns/lines with type String
     * </p>
     * 
     * @param cellsTypeMatrix key: column number value: row where the type change from String to something else
     * @return
     */
    protected SortedMap<Integer, Integer> guessHeaderChange(Map<Integer, SortedMap<Integer, Type>> cellsTypeMatrix) {
        SortedMap<Integer, Integer> cellTypeChange = new TreeMap<>();

        cellsTypeMatrix.forEach((integer, integerTypeSortedMap) -> {

            Type firstType = null;
            int rowChange = 0;

            for (Map.Entry<Integer, Type> sortedMapEntry : integerTypeSortedMap.entrySet()) {
                if (firstType == null) {
                    firstType = sortedMapEntry.getValue();
                } else {
                    if (sortedMapEntry.getValue() != firstType && sortedMapEntry.getValue() != Type.STRING) {
                        rowChange = sortedMapEntry.getKey();
                        break;
                    }
                }
            }

            cellTypeChange.put(integer, rowChange);

            firstType = null;
            rowChange = 0;

        });

        return cellTypeChange;
    }
}
