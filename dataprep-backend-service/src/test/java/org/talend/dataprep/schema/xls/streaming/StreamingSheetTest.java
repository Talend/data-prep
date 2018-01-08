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

package org.talend.dataprep.schema.xls.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.junit.Before;
import org.junit.Test;

public class StreamingSheetTest {

    private StreamingSheet streamingSheet;

    @Before
    public void setUp() throws Exception {
        OPCPackage pkg = OPCPackage.open(StreamingSheetTest.class.getResourceAsStream("../dates.xlsx"));
        XSSFReader reader = new XSSFReader(pkg);

        SharedStringsTable sst = reader.getSharedStringsTable();
        StylesTable styles = reader.getStylesTable();

        Iterator<InputStream> iter = reader.getSheetsData();
        XMLEventReader parser = XMLInputFactory.newInstance().createXMLEventReader(iter.next());
        final StreamingSheetReader streamingSheetReader = new StreamingSheetReader(sst, styles, parser, 10);
        streamingSheet = new StreamingSheet("name", streamingSheetReader);
    }

    @Test
    public void getReader() throws Exception {
        assertNotNull(streamingSheet.getReader());
    }

    @Test
    public void iterator() throws Exception {
        int i = 0;
        for (Row cells : streamingSheet) {
            i++;
        }
        assertEquals(6, i);
    }

    @Test
    public void rowIterator() throws Exception {
        final Iterator<Row> rowIterator = streamingSheet.rowIterator();
        int i = 0;
        while (rowIterator.hasNext()) {
            rowIterator.next();
            i++;
        }
        assertEquals(6, i);
    }

    @Test
    public void getSheetName() throws Exception {
        final String name = streamingSheet.getSheetName();
        assertEquals("name", name);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createRow() throws Exception {
        streamingSheet.createRow(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeRow() throws Exception {
        streamingSheet.removeRow(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRow() throws Exception {
        streamingSheet.getRow(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPhysicalNumberOfRows() throws Exception {
        streamingSheet.getPhysicalNumberOfRows();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getFirstRowNum() throws Exception {
        streamingSheet.getFirstRowNum();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLastRowNum() throws Exception {
        streamingSheet.getLastRowNum();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setColumnHidden() throws Exception {
        streamingSheet.setColumnHidden(0, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isColumnHidden() throws Exception {
        streamingSheet.isColumnHidden(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRightToLeft() throws Exception {
        streamingSheet.setRightToLeft(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isRightToLeft() throws Exception {
        streamingSheet.isRightToLeft();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setColumnWidth() throws Exception {
        streamingSheet.setColumnWidth(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getColumnWidth() throws Exception {
        streamingSheet.getColumnWidth(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getColumnWidthInPixels() throws Exception {
        streamingSheet.getColumnWidthInPixels(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDefaultColumnWidth() throws Exception {
        streamingSheet.setDefaultColumnWidth(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDefaultColumnWidth() throws Exception {
        streamingSheet.getDefaultColumnWidth();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDefaultRowHeight() throws Exception {
        streamingSheet.getDefaultRowHeight();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDefaultRowHeightInPoints() throws Exception {
        streamingSheet.getDefaultRowHeightInPoints();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDefaultRowHeight() throws Exception {
        streamingSheet.setDefaultRowHeight(((short) 0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDefaultRowHeightInPoints() throws Exception {
        streamingSheet.setDefaultRowHeightInPoints(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getColumnStyle() throws Exception {
        streamingSheet.getColumnStyle(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addMergedRegion() throws Exception {
        streamingSheet.addMergedRegion(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setVerticallyCenter() throws Exception {
        streamingSheet.setVerticallyCenter(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setHorizontallyCenter() throws Exception {
        streamingSheet.setHorizontallyCenter(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getHorizontallyCenter() throws Exception {
        streamingSheet.getHorizontallyCenter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getVerticallyCenter() throws Exception {
        streamingSheet.getVerticallyCenter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeMergedRegion() throws Exception {
        streamingSheet.removeMergedRegion(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getNumMergedRegions() throws Exception {
        streamingSheet.getNumMergedRegions();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getMergedRegion() throws Exception {
        streamingSheet.getMergedRegion(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getMergedRegions() throws Exception {
        streamingSheet.getMergedRegions();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setForceFormulaRecalculation() throws Exception {
        streamingSheet.setForceFormulaRecalculation(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getForceFormulaRecalculation() throws Exception {
        streamingSheet.getForceFormulaRecalculation();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setAutobreaks() throws Exception {
        streamingSheet.setAutobreaks(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDisplayGuts() throws Exception {
        streamingSheet.setDisplayGuts(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDisplayZeros() throws Exception {
        streamingSheet.setDisplayZeros(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isDisplayZeros() throws Exception {
        streamingSheet.isDisplayZeros();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setFitToPage() throws Exception {
        streamingSheet.setFitToPage(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRowSumsBelow() throws Exception {
        streamingSheet.setRowSumsBelow(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRowSumsRight() throws Exception {
        streamingSheet.setRowSumsRight(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAutobreaks() throws Exception {
        streamingSheet.getAutobreaks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDisplayGuts() throws Exception {
        streamingSheet.getDisplayGuts();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getFitToPage() throws Exception {
        streamingSheet.getFitToPage();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRowSumsBelow() throws Exception {
        streamingSheet.getRowSumsBelow();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRowSumsRight() throws Exception {
        streamingSheet.getRowSumsRight();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isPrintGridlines() throws Exception {
        streamingSheet.isPrintGridlines();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setPrintGridlines() throws Exception {
        streamingSheet.setPrintGridlines(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPrintSetup() throws Exception {
        streamingSheet.getPrintSetup();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getHeader() throws Exception {
        streamingSheet.getHeader();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getFooter() throws Exception {
        streamingSheet.getFooter();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setSelected() throws Exception {
        streamingSheet.setSelected(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getMargin() throws Exception {
        streamingSheet.getMargin(Sheet.BottomMargin);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setMargin() throws Exception {
        streamingSheet.setMargin(Sheet.BottomMargin, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getProtect() throws Exception {
        streamingSheet.getProtect();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void protectSheet() throws Exception {
        streamingSheet.protectSheet("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getScenarioProtect() throws Exception {
        streamingSheet.getScenarioProtect();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setZoom() throws Exception {
        streamingSheet.setZoom(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getTopRow() throws Exception {
        streamingSheet.getTopRow();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLeftCol() throws Exception {
        streamingSheet.getLeftCol();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void showInPane() throws Exception {
        streamingSheet.showInPane(Sheet.BottomMargin, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shiftRows() throws Exception {
        streamingSheet.shiftRows(0, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shiftRows1() throws Exception {
        streamingSheet.shiftRows(0, 0, 0, false, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createFreezePane() throws Exception {
        streamingSheet.createFreezePane(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createFreezePane1() throws Exception {
        streamingSheet.createFreezePane(0, 0, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createSplitPane() throws Exception {
        streamingSheet.createSplitPane(0, 0, 0, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPaneInformation() throws Exception {
        streamingSheet.getPaneInformation();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDisplayGridlines() throws Exception {
        streamingSheet.setDisplayGridlines(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isDisplayGridlines() throws Exception {
        streamingSheet.isDisplayGridlines();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDisplayFormulas() throws Exception {
        streamingSheet.setDisplayFormulas(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isDisplayFormulas() throws Exception {
        streamingSheet.isDisplayFormulas();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDisplayRowColHeadings() throws Exception {
        streamingSheet.setDisplayRowColHeadings(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isDisplayRowColHeadings() throws Exception {
        streamingSheet.isDisplayRowColHeadings();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRowBreak() throws Exception {
        streamingSheet.setRowBreak(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isRowBroken() throws Exception {
        streamingSheet.isRowBroken(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeRowBreak() throws Exception {
        streamingSheet.removeRowBreak(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRowBreaks() throws Exception {
        streamingSheet.getRowBreaks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getColumnBreaks() throws Exception {
        streamingSheet.getColumnBreaks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setColumnBreak() throws Exception {
        streamingSheet.setColumnBreak(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isColumnBroken() throws Exception {
        streamingSheet.isColumnBroken(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeColumnBreak() throws Exception {
        streamingSheet.removeColumnBreak(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setColumnGroupCollapsed() throws Exception {
        streamingSheet.setColumnGroupCollapsed(0, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void groupColumn() throws Exception {
        streamingSheet.groupColumn(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ungroupColumn() throws Exception {
        streamingSheet.ungroupColumn(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void groupRow() throws Exception {
        streamingSheet.groupRow(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ungroupRow() throws Exception {
        streamingSheet.ungroupRow(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRowGroupCollapsed() throws Exception {
        streamingSheet.setRowGroupCollapsed(0, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setDefaultColumnStyle() throws Exception {
        streamingSheet.setDefaultColumnStyle(0, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void autoSizeColumn() throws Exception {
        streamingSheet.autoSizeColumn(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void autoSizeColumn1() throws Exception {
        streamingSheet.autoSizeColumn(0, true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCellComment() throws Exception {
        streamingSheet.getCellComment(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createDrawingPatriarch() throws Exception {
        streamingSheet.createDrawingPatriarch();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getWorkbook() throws Exception {
        streamingSheet.getWorkbook();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void isSelected() throws Exception {
        streamingSheet.isSelected();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setArrayFormula() throws Exception {
        streamingSheet.setArrayFormula("", null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeArrayFormula() throws Exception {
        streamingSheet.removeArrayFormula(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDataValidationHelper() throws Exception {
        streamingSheet.getDataValidationHelper();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDataValidations() throws Exception {
        streamingSheet.getDataValidations();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addValidationData() throws Exception {
        streamingSheet.addValidationData(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setAutoFilter() throws Exception {
        streamingSheet.setAutoFilter(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSheetConditionalFormatting() throws Exception {
        streamingSheet.getSheetConditionalFormatting();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRepeatingRows() throws Exception {
        streamingSheet.getRepeatingRows();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRepeatingColumns() throws Exception {
        streamingSheet.getRepeatingColumns();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRepeatingRows() throws Exception {
        streamingSheet.setRepeatingRows(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setRepeatingColumns() throws Exception {
        streamingSheet.setRepeatingColumns(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getColumnOutlineLevel() throws Exception {
        streamingSheet.getColumnOutlineLevel(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getActiveCell() throws Exception {
        streamingSheet.getActiveCell();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCellComment1() throws Exception {
        streamingSheet.getCellComment(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getCellComments() throws Exception {
        streamingSheet.getCellComments();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getDrawingPatriarch() throws Exception {
        streamingSheet.getDrawingPatriarch();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getHyperlink() throws Exception {
        streamingSheet.getHyperlink(0, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getHyperlinkList() throws Exception {
        streamingSheet.getHyperlinkList();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setActiveCell() throws Exception {
        streamingSheet.setActiveCell(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setZoom1() throws Exception {
        streamingSheet.setZoom(0, 0);
    }

}
