//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.math;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

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
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the absolute actions.
 *
 * @see Absolute
 */
public class AbsoluteTest extends AbstractMetadataBaseTest<Absolute> {

    private static final String FLOAT_COLUMN = "0000"; //$NON-NLS-1$

    private static final String INT_COLUMN = "0000"; //$NON-NLS-1$

    private Map<String, String> absFloatParameters;

    private Map<String, String> absIntParameters;

    public AbsoluteTest() {
        super(new Absolute());
    }

    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    private void assertInteger(DataSetRow row, String expected) {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absIntParameters));

        // then
        assertEquals(expected, row.get(INT_COLUMN)); //$NON-NLS-1$
    }

    private void assertFloat(DataSetRow row, String expected) {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals(expected, row.get(FLOAT_COLUMN)); //$NON-NLS-1$
    }

    @Before
    public void init() throws IOException {
        absFloatParameters = ActionMetadataTestUtils
                .parseParameters(AbsoluteTest.class.getResourceAsStream("absoluteFloatAction.json"));
        absIntParameters = ActionMetadataTestUtils
                .parseParameters(AbsoluteTest.class.getResourceAsStream("absoluteIntAction.json"));
    }

    @Test
    public void testAdaptFloat() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.FLOAT).build();
        assertThat(action.adapt(column), not(is(action)));
    }

    @Test
    public void testAdaptInt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.INTEGER).build();
        assertThat(action.adapt(column), not(is(action)));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.MATH.getDisplayName(Locale.US)));
    }

    @Test
    public void testAbsoluteFloatWithPositiveFloat() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "5.42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

       assertFloat(row, "5.42");
    }

    @Test
    public void test_AbsoluteFloatWithHugeValue() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "12345678.1"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "12345678.1");
    }

    @Test
    public void test_AbsoluteFloatWithHugeNegativeValue() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-12345678.1"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "12345678.1");
    }

    @Test
    public void testAbsoluteIntWithPositiveFloat() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "5.42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "5.42");
    }

    @Test
    public void testAbsoluteFloatWithNegativePercentage() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-5%"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "0.05");
    }

    @Test
    public void testAbsoluteFloatWithNegativeFloat() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-5.42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "5.42");
    }

    @Test
    public void testAbsoluteFloatWithNegative_big_number() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-891234567898"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "891234567898");
    }

    @Test
    public void testAbsoluteFloatWithNegativeFloat_alt_decimal_sep() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-5,42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "5.42");
    }

    @Test
    public void testAbsoluteFloatWithNegativeScientific() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-1.2E3"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "1200");
    }

    @Test
    public void testAbsoluteIntWithNegativeFloat() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "-5.42"); //$NON-NLS-1$

        assertInteger(new DataSetRow(values), "5.42");
    }

    @Test
    public void testAbsoluteFloatWithPositiveInt() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "42");
    }

    @Test
    public void testAbsoluteIntWithPositiveInt() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "42"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "42");
    }

    @Test
    public void testAbsoluteFloatWithNegativeInt() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-542"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "542");
    }

    @Test
    public void testAbsoluteIntWithNegativeInt() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "-542"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "542");
    }

    @Test
    public void testAbsoluteFloatWithNegativeZero() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "-0"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "0");
    }

    @Test
    public void testAbsoluteIntWithNegativeZero() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "-0"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "0");
    }

    @Test
    public void testAbsoluteFloatWithEmpty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, ""); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "");
    }

    @Test
    public void testAbsoluteIntWithEmpty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, ""); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "");
    }

    @Test
    public void testAbsoluteFloatWithNonNumeric() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(FLOAT_COLUMN, "foobar"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertFloat(row, "foobar");
    }

    @Test
    public void testAbsoluteIntWithNonNumeric() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(INT_COLUMN, "foobar"); //$NON-NLS-1$
        final DataSetRow row = new DataSetRow(values);

        assertInteger(row, "foobar");
    }

    @Test
    public void testAbsoluteFloatWithMissingColumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("wrong_column", "-12"); //$NON-NLS-1$ //$NON-NLS-2$
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals("-12", row.get("wrong_column")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testAbsoluteIntWithMissingColumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("wrong_column", "-13"); //$NON-NLS-1$ //$NON-NLS-2$
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals("-13", row.get("wrong_column")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void test_apply_inplace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "-10");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "10");
        expectedValues.put("0002", "May 20th 2015");

        absFloatParameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "-10");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "-10");
        expectedValues.put("0002", "May 20th 2015");
        expectedValues.put("0003", "10");

        absFloatParameters.put("column_id", "0001");
        absFloatParameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_absolute").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_in_newcolumn_not_applicable() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "-10");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "-10");
        expectedValues.put("0002", "May 20th 2015");

        absFloatParameters.put("column_id", "0000");
        absFloatParameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, absFloatParameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
