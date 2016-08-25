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

package org.talend.dataprep.schema.xls;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class XlsUtilsTest {

    @Test
    public void detect_old_excel_version() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("Workbook-xls.xls");
        Assertions.assertThat(XlsUtils.isNewExcelFormat(inputStream)).isFalse();
    }

    @Test
    public void detect_new_excel_version() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("Workbook-xlsx.xlsx");
        Assertions.assertThat(XlsUtils.isNewExcelFormat(inputStream)).isTrue();

        inputStream = getClass().getResourceAsStream("Workbook-xls.xls");
        Assertions.assertThat(XlsUtils.isNewExcelFormat(inputStream)).isFalse();
    }

    @Test
    public void no_detect_excel() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("fake.xls");
        Assertions.assertThat(XlsUtils.isNewExcelFormat(inputStream)).isFalse();

        inputStream = getClass().getResourceAsStream("fake.xls");
        Assertions.assertThat(XlsUtils.isNewExcelFormat(inputStream)).isFalse();
    }

    @Test
    public void get_active_sheets() throws Exception {

        OPCPackage container = OPCPackage.open(getClass().getResourceAsStream("000_DTA_DailyTimeLog.xlsm"));
        XSSFReader xssfReader = new XSSFReader(container);

        List<String> names = XlsUtils.getActiveSheetsFromWorkbookSpec(xssfReader.getWorkbookData());

        Assertions.assertThat(names).isNotEmpty() //
                .containsExactly("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY", "WEEK SUMMARY");

    }

    @Test
    public void get_active_sheets_simple() throws Exception {

        List<String> names = XlsUtils.getActiveSheetsFromWorkbookSpec(getClass().getResourceAsStream("simple_workbook.xml"));

        Assertions.assertThat(names).isNotEmpty() //
                .containsExactly("Feuille1", "Feuille2", "Feuille3");

    }

    @Test
    public void get_ColumnsNumber() throws Exception {
        Assertions.assertThat(XlsUtils.getColumnsNumber(getClass().getResourceAsStream("data_xls.xml"))) //
                .isEqualTo(33);

        Assertions.assertThat(XlsUtils.getColumnsNumber(getClass().getResourceAsStream("simple_data.xml"))) //
                .isEqualTo(4);

        Assertions.assertThat(XlsUtils.getColumnsNumber(getClass().getResourceAsStream("TDP-1781.xml"))) //
            .isEqualTo(6);
    }

    @Test
    public void dimension_calculation() throws Exception {
        Assertions.assertThat(XlsUtils.getColumnsNumberFromDimension("A1:D5")).isEqualTo(4);
        Assertions.assertThat(XlsUtils.getColumnsNumberFromDimension("A1:A151")).isEqualTo(1);
        Assertions.assertThat(XlsUtils.getColumnsNumberFromDimension("B1:AG142")).isEqualTo(33);
    }

    @Test
    public void col_number_from_cell_ref() throws Exception {
        Assertions.assertThat(XlsUtils.getColumnNumberFromCellRef( "A5")).isEqualTo( 0);
        Assertions.assertThat(XlsUtils.getColumnNumberFromCellRef( "D5")).isEqualTo( 3);
        Assertions.assertThat(XlsUtils.getColumnNumberFromCellRef( "A15")).isEqualTo( 0);
        Assertions.assertThat(XlsUtils.getColumnNumberFromCellRef( "AG142")).isEqualTo( 32);
        Assertions.assertThat(XlsUtils.getColumnNumberFromCellRef( "BB11")).isEqualTo( 53);
    }

}
