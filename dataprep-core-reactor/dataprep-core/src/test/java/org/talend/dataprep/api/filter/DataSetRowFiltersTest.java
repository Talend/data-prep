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

package org.talend.dataprep.api.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createCompliesPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createContainsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createEmptyPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createGreaterOrEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createGreaterThanPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createInPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createInvalidPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createLowerOrEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createMatchesPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createRangePredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createValidPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;

public class DataSetRowFiltersTest {

    @Test
    public void testEqualsString() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "Test");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Test");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotEqualsString() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "test");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Test");
        assertFalse(test.test(row));
    }

    @Test
    public void testEqualsDecimal() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "3.0");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "3");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotEqualsDecimal() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "3.5");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "3");
        assertFalse(test.test(row));
    }

    @Test
    public void testEqualsInteger() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "3");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "3");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotEqualsInteger() throws Exception {
        Predicate<DataSetRow> test = createEqualsPredicate("0000", "3");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "35");
        assertFalse(test.test(row));
    }

    @Test
    public void testGreaterThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterThanPredicate("0000", "34");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "35");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotGreaterThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterThanPredicate("0000", "35");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "35");
        assertFalse(test.test(row));
    }

    @Test
    public void testLowerThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterThanPredicate("0000", "41");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotLowerThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterThanPredicate("0000", "42");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertFalse(test.test(row));
    }

    @Test
    public void testGreaterOrEqualsThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterOrEqualsPredicate("0000", "41");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotGreaterOrEqualsThan() throws Exception {
        Predicate<DataSetRow> test = createGreaterOrEqualsPredicate("0000", "43");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertFalse(test.test(row));
    }

    @Test
    public void testLowerOrEqualsThan() throws Exception {
        Predicate<DataSetRow> test = createLowerOrEqualsPredicate("0000", "43");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotLowerOrEqualsThan() throws Exception {
        Predicate<DataSetRow> test = createLowerOrEqualsPredicate("0000", "41");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertFalse(test.test(row));
    }

    @Test
    public void testIn() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("27");
        list.add("42");
        list.add("69");
        Predicate<DataSetRow> test = createInPredicate("0000", list);
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "42");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotIn() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("27");
        list.add("42");
        list.add("69");
        Predicate<DataSetRow> test = createInPredicate("0000", list);
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "31");
        assertFalse(test.test(row));
    }

    @Test
    public void testContains() throws Exception {
        Predicate<DataSetRow> test = createContainsPredicate("0000", "es");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Test");
        assertTrue(test.test(row));
    }

    @Test
    public void testNotContains() throws Exception {
        Predicate<DataSetRow> test = createContainsPredicate("0000", "as");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Test");
        assertFalse(test.test(row));
    }

    @Test
    public void testComplies() throws Exception {
        Predicate<DataSetRow> test = createCompliesPredicate("0000", "Aaa9");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Tes7");
        assertTrue(test.test(row));
        row.set("0000", "test");
        assertFalse(test.test(row));
        row.set("0000", "Test");
        assertFalse(test.test(row));
        row.set("0000", "T3s7");
        assertFalse(test.test(row));
    }

    @Test
    public void testInvalid() throws Exception {
        Predicate<DataSetRow> test = createInvalidPredicate("0000");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Tes7");
        assertFalse(test.test(row));
        row.setInvalid("0000");
        assertTrue(test.test(row));
    }

    @Test
    public void testValid() throws Exception {
        Predicate<DataSetRow> test = createValidPredicate("0000");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "Tes7");
        assertTrue(test.test(row));
        row.setInvalid("0000");
        assertFalse(test.test(row));
    }

    @Test
    public void testEmpty() throws Exception {
        Predicate<DataSetRow> test = createEmptyPredicate("0000");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "");
        assertTrue(test.test(row));
        row.set("0000", "Tes7");
        assertFalse(test.test(row));
    }

    @Test
    public void testRangeClosed() throws Exception {
        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().id(0).type(Type.STRING).build());
        Predicate<DataSetRow> test = createRangePredicate("0000", "1", "10", false, false, rowMetadata);
        DataSetRow row = new DataSetRow(rowMetadata);
        row.set("0000", "3");
        assertTrue(test.test(row));
        row.set("0000", "1");
        assertTrue(test.test(row));
        row.set("0000", "10");
        assertTrue(test.test(row));
        row.set("0000", "12");
        assertFalse(test.test(row));
        row.set("0000", "0");
        assertFalse(test.test(row));
    }

    @Test
    public void testRangeOpen() throws Exception {
        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().id(0).type(Type.STRING).build());
        Predicate<DataSetRow> test = createRangePredicate("0000", "1", "10", true, true, rowMetadata);
        DataSetRow row = new DataSetRow(rowMetadata);
        row.set("0000", "3");
        assertTrue(test.test(row));
        row.set("0000", "1");
        assertFalse(test.test(row));
        row.set("0000", "10");
        assertFalse(test.test(row));
        row.set("0000", "12");
        assertFalse(test.test(row));
        row.set("0000", "0");
        assertFalse(test.test(row));
    }

    @Test
    public void testDateRange() throws Exception {
        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().id(0).type(Type.DATE).build());
        // 1514761200000 -> 01/01/2018
        // 1517353200000 -> 31/01/2018
        Predicate<DataSetRow> test =
                createRangePredicate("0000", "1514761200000", "1517353200000", false, false, rowMetadata);
        DataSetRow row = new DataSetRow(rowMetadata);
        row.set("0000", "10/01/2018");
        assertTrue(test.test(row));
        row.set("0000", "01/03/2018");
        assertFalse(test.test(row));
        row.set("0000", "31/12/2016");
        assertFalse(test.test(row));
    }

    @Test
    public void testMatch() throws Exception {
        Predicate<DataSetRow> test = createMatchesPredicate("0000", "[a-z]+");
        DataSetRow row = new DataSetRow(new RowMetadata());
        row.set("0000", "toto");
        assertTrue(test.test(row));
        row.set("0000", "Toto");
        assertFalse(test.test(row));
    }
}
