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

package org.talend.dataprep.transformation.actions.math;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
 * Unit test for the absolute actions.
 *
 * @see Absolute
 */
public class AbsoluteTest extends AbstractMetadataBaseTest {

    private static final String FLOAT_COLUMN = "0000"; //$NON-NLS-1$

    private static final String INT_COLUMN = "0000"; //$NON-NLS-1$

    private Absolute absolute = new Absolute();

    private Map<String, String> absFloatParameters;

    private Map<String, String> absIntParameters;

    private void assertInteger(DataSetRow row, String expected) {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(absolute, absIntParameters));

        // then
        assertEquals(expected, row.get(INT_COLUMN)); //$NON-NLS-1$
    }

    private void assertFloat(DataSetRow row, String expected) {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(absolute, absFloatParameters));

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
        assertThat(absolute.adapt((ColumnMetadata) null), is(absolute));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.FLOAT).build();
        assertThat(absolute.adapt(column), not(is(absolute)));
    }

    @Test
    public void testAdaptInt() throws Exception {
        assertThat(absolute.adapt((ColumnMetadata) null), is(absolute));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.INTEGER).build();
        assertThat(absolute.adapt(column), not(is(absolute)));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(absolute.getCategory(), is(ActionCategory.MATH.getDisplayName()));
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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(absolute, absFloatParameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(absolute, absFloatParameters));

        // then
        assertEquals("-13", row.get("wrong_column")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void should_accept_column() {
        assertTrue(absolute.acceptField(getColumn(Type.INTEGER)));
        assertTrue(absolute.acceptField(getColumn(Type.FLOAT)));
        assertTrue(absolute.acceptField(getColumn(Type.DOUBLE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(absolute.acceptField(getColumn(Type.STRING)));
        assertFalse(absolute.acceptField(getColumn(Type.DATE)));
        assertFalse(absolute.acceptField(getColumn(Type.BOOLEAN)));
    }
}
