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

import org.apache.poi.hssf.usermodel.HSSFCell;
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
        for (int i = 0; i <= nbSheet1-1; i++) {
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
        }

        boolean equalCells = false;
        int type1 = cell1.getCellTypeEnum().getCode();
        int type2 = cell2.getCellTypeEnum().getCode();
        if (type1 == type2) {
            if (cell1.getCellStyle().equals(cell2.getCellStyle())) {
                // Compare cells based on its type
                switch (cell1.getCellTypeEnum().getCode()) {
                case HSSFCell.CELL_TYPE_FORMULA:
                    if (cell1.getCellFormula().equals(cell2.getCellFormula())) {
                        equalCells = true;
                    }
                    break;
                case HSSFCell.CELL_TYPE_NUMERIC:
                    if (cell1.getNumericCellValue() == cell2.getNumericCellValue()) {
                        equalCells = true;
                    }
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    if (cell1.getStringCellValue().equals(cell2.getStringCellValue())) {
                        equalCells = true;
                    }
                    break;
                case HSSFCell.CELL_TYPE_BLANK:
                    if (cell2.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                        equalCells = true;
                    }
                    break;
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    if (cell1.getBooleanCellValue() == cell2.getBooleanCellValue()) {
                        equalCells = true;
                    }
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    if (cell1.getErrorCellValue() == cell2.getErrorCellValue()) {
                        equalCells = true;
                    }
                    break;
                default:
                    if (cell1.getStringCellValue().equals(cell2.getStringCellValue())) {
                        equalCells = true;
                    }
                    break;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return equalCells;
    }

}
