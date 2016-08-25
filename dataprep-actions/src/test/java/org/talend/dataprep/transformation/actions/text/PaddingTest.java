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

package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Padding action. Creates one consumer, and test it.
 *
 */
public class PaddingTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private Padding action = new Padding();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(PaddingTest.class.getResourceAsStream("paddingAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    @Test
    public void test_basic() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "10");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "0010");
        expectedValues.put("0002", "May 20th 2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }
}
