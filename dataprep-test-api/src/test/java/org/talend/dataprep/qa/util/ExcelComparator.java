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

package org.talend.dataprep.qa.util;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * From http://blog.sodhanalibrary.com/2015/03/compare-excel-sheets-using-java.html
 */
public class ExcelComparator {

    public static boolean compareTwoFile(XSSFWorkbook workbook1, XSSFWorkbook workbook2) {
        int nbSheet1 = workbook1.getNumberOfSheets();
        int nbSheet2 = workbook2.getNumberOfSheets();
        if (nbSheet1 != nbSheet2) {
            return false;
        }
        boolean equalFile = true;
        for (int i = 0; i <= nbSheet1 - 1; i++) {
            XSSFSheet sheet1 = workbook1.getSheetAt(i);
            XSSFSheet sheet2 = workbook2.getSheetAt(i);
            if (!compareTwoSheets(sheet1, sheet2)) {
                equalFile = false;
                break;
            }
        }

        return equalFile;
    }

    // Compare Two Sheets
    public static boolean compareTwoSheets(XSSFSheet sheet1, XSSFSheet sheet2) {
        int firstRow1 = sheet1.getFirstRowNum();
        int lastRow1 = sheet1.getLastRowNum();
        boolean equalSheets = true;
        for (int i = firstRow1; i <= lastRow1; i++) {

            XSSFRow row1 = sheet1.getRow(i);
            XSSFRow row2 = sheet2.getRow(i);
            if (!compareTwoRows(row1, row2)) {
                equalSheets = false;
                break;
            }
        }
        return equalSheets;
    }

    // Compare Two Rows
    public static boolean compareTwoRows(XSSFRow row1, XSSFRow row2) {
        if ((row1 == null) && (row2 == null)) {
            return true;
        } else if ((row1 == null) || (row2 == null)) {
            return false;
        }

        int firstCell1 = row1.getFirstCellNum();
        int lastCell1 = row1.getLastCellNum();
        boolean equalRows = true;

        // Compare all cells in a row
        for (int i = firstCell1; i <= lastCell1; i++) {
            XSSFCell cell1 = row1.getCell(i);
            XSSFCell cell2 = row2.getCell(i);
            if (!compareTwoCells(cell1, cell2)) {
                equalRows = false;
                break;
            }
        }
        return equalRows;
    }

    // Compare Two Cells
    public static boolean compareTwoCells(XSSFCell cell1, XSSFCell cell2) {
        if ((cell1 == null) && (cell2 == null)) {
            return true;
        } else if ((cell1 == null) || (cell2 == null)) {
            return false;
        } else if (cell1.getCellType() != cell2.getCellType()) {
            return false;
        }

        boolean equalCells = false;
        // Compare cells based on their type
        switch (cell1.getCellType()) {
        case FORMULA:
            equalCells = (cell1.getCellFormula().equals(cell2.getCellFormula()));
            break;
        case NUMERIC:
            equalCells = (cell1.getNumericCellValue() == cell2.getNumericCellValue());
            break;
        case STRING:
            equalCells = (cell1.getStringCellValue().equals(cell2.getStringCellValue()));
            break;
        case BLANK:
            equalCells = true;
            break;
        case BOOLEAN:
            equalCells = (cell1.getBooleanCellValue() == cell2.getBooleanCellValue());
            break;
        case ERROR:
            equalCells = (cell1.getErrorCellValue() == cell2.getErrorCellValue());
            break;
        default:
            equalCells = (cell1.getStringCellValue().equals(cell2.getStringCellValue()));
            break;
        }
        return equalCells;
    }

}
