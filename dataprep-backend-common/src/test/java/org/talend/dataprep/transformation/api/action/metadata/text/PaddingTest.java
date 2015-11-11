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
package org.talend.dataprep.transformation.api.action.metadata.text;

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
 * Test class for Padding action. Creates one consumer, and test it.
 *
 */
public class PaddingTest {

    /** The action to test. */
    private Padding action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new Padding();

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                PaddingTest.class.getResourceAsStream("paddingAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    /**
     * @see LowerCase#create(Map)
     */
    @Test
    public void test_basic() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("0001", "10");
        values.put("joined", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("name", "Vincent");
        expectedValues.put("0001", "0010");
        expectedValues.put("joined", "May 20th 2015");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testApplyOnNumerics() {
        assertEquals("0000", action.apply("", 4, '0', Padding.LEFT_POSITION));
        assertEquals("0010", action.apply("10", 4, '0', Padding.LEFT_POSITION));
        assertEquals("0-10", action.apply("-10", 4, '0', Padding.LEFT_POSITION));
        assertEquals("12345", action.apply("12345", 4, '0', Padding.LEFT_POSITION));
        assertEquals("123456789", action.apply("123456789", 4, '0', Padding.LEFT_POSITION));
    }

    @Test
    public void testApplyOnStrings() {
        assertEquals("Tagada", action.apply("agada", 6, 'T', Padding.LEFT_POSITION));
        assertEquals("tagada", action.apply("tagada", 4, 'T', Padding.LEFT_POSITION));


        assertEquals("agadaT", action.apply("agada", 6, 'T', Padding.RIGHT_POSITION));
        assertEquals("tagada", action.apply("tagada", 4, 'T', Padding.RIGHT_POSITION));
    }

    @Test
    public void test_some_special_values() {
        assertEquals("", action.apply(null, 5, '0', "Left"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
