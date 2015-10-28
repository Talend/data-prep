// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for RoundFloor action. Creates one consumer, and test it.
 *
 * @see RoundFloor
 */
public class RoundFloorTest {

    /** The action ton test. */
    private RoundFloor action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new RoundFloor();

        parameters = ActionMetadataTestUtils.parseParameters(RoundFloorTest.class.getResourceAsStream("floorAction.json"));
    }

    @Test
    public void testName() {
        assertEquals(RoundFloor.ACTION_NAME, action.getName());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.MATH.getDisplayName()));
    }

    public void testCommon(String input, String expected) {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("aNumber", input);
        final DataSetRow row = new DataSetRow(values);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "aNumber");

        // then
        assertEquals(expected, row.get("aNumber"));
    }

    @Test
    public void testPositive() {
        testCommon("5.0", "5");
        testCommon("5.1", "5");
        testCommon("5.5", "5");
        testCommon("5.8", "5");
    }

    @Test
    public void testNegative() {
        testCommon("-5.0", "-5");
        testCommon("-5.4", "-6");
        testCommon("-5.6", "-6");
    }

    @Test
    public void test_huge_numbers_positive() {
        testCommon("131234567890.1", "131234567890");
        testCommon("89891234567897.9", "89891234567897");
        testCommon("34891234567899.9", "34891234567899");
        testCommon("678999999999999.9", "678999999999999");
    }

    @Test
    public void test_huge_numbers_negative() {
        testCommon("-131234567890.1", "-131234567891");
        testCommon("-89891234567897.9", "-89891234567898");
        testCommon("-34891234567899.9", "-34891234567900");
        testCommon("-678999999999999.9", "-679000000000000");
    }

    @Test
    public void testInteger() {
        testCommon("5", "5");
        testCommon("-5", "-5");
    }

    @Test
    public void testString() {
        testCommon("tagada", "tagada");
        testCommon("", "");
        testCommon("null", "null");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
