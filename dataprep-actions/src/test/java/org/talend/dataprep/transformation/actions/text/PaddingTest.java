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

package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
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
public class PaddingTest extends AbstractMetadataBaseTest<Padding> {

    private Map<String, String> parameters;

    public PaddingTest() {
        super(new Padding());
    }

    @Before
    public void init() throws IOException {
        parameters =
                ActionMetadataTestUtils.parseParameters(PaddingTest.class.getResourceAsStream("paddingAction.json"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS_ADVANCED.getDisplayName(Locale.US)));
    }

    @Test
    public void testApplyWithStringParameter() throws Exception {
        //size less than length and padding is surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 5, "𠀀", Padding.LEFT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄"));
        //size more than length and padding is surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 14, "𠀀", Padding.LEFT_POSITION), is("𠀀中崎𠀀𠀁𠀂𠀃𠀄"));
        //size less than length and padding is not surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 5, "我", Padding.LEFT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄"));
        //size more than length and padding is not surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 13, "我", Padding.LEFT_POSITION), is("我中崎𠀀𠀁𠀂𠀃𠀄"));
        //size less than length and padding is surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 5, "𠀀", Padding.RIGHT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄"));
        //size more than length and padding is surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 14, "𠀀", Padding.RIGHT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄𠀀"));
        //size less than length and padding is not surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 5, "我", Padding.RIGHT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄"));
        //size more than length and padding is not surrogate pair
        assertThat(action.apply("中崎𠀀𠀁𠀂𠀃𠀄", 13, "我", Padding.RIGHT_POSITION), is("中崎𠀀𠀁𠀂𠀃𠀄我"));
    }

    @Test
    public void testGetRealSize() throws Exception {
        // size more than length and padding is surrogate pair
        assertThat(action.getRealSize("中崎𠀀𠀁𠀂𠀃𠀄", 8, true), is(14));
        // size less than length and padding is surrogate pair
        assertThat(action.getRealSize("中崎𠀀𠀁𠀂𠀃𠀄", 5, true), is(5));
        // size more than length and padding is not surrogate pair
        assertThat(action.getRealSize("中崎𠀀𠀁𠀂𠀃𠀄", 8, false), is(13));
        // size less than length and padding is not surrogate pair
        assertThat(action.getRealSize("中崎𠀀𠀁𠀂𠀃𠀄", 5, false), is(5));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "10");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "10");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "0010");

        parameters.put(CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_padded").type(Type.INTEGER).build();
        expected.getStatistics().setValid(1);
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyOnSurrogatePairLeftPaddingIsSurrogatePair() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "𠀀𠀀𠀀中崎𠀀𠀁𠀂𠀃𠀄");

        parameters.put(CREATE_NEW_COLUMN, "true");
        parameters.put("padding_char", "𠀀");
        parameters.put("size", "10");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_padded").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyOnSurrogatePairLeftPaddingIsNotSurrogatePair() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "我我我中崎𠀀𠀁𠀂𠀃𠀄");

        parameters.put(CREATE_NEW_COLUMN, "true");
        parameters.put("padding_char", "我");
        parameters.put("size", "10");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_padded").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyOnSurrogatePairRightPaddingIsSurrogatePair() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "中崎𠀀𠀁𠀂𠀃𠀄𠀀𠀀𠀀");

        parameters.put(CREATE_NEW_COLUMN, "true");
        parameters.put("padding_char", "𠀀");
        parameters.put("size", "10");
        parameters.put("padding_position", "right");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_padded").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void testApplyOnSurrogatePairRightPaddingIsNotSurrogatePair() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "中崎𠀀𠀁𠀂𠀃𠀄");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "中崎𠀀𠀁𠀂𠀃𠀄我我我");

        parameters.put(CREATE_NEW_COLUMN, "true");
        parameters.put("padding_char", "我");
        parameters.put("size", "10");
        parameters.put("padding_position", "right");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_padded").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
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

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
