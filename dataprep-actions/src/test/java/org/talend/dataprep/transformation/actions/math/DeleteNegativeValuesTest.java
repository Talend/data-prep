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
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DeleteOnValue action. Creates one consumer, and test it.
 *
 * @see DeleteNegativeValues
 */
public class DeleteNegativeValuesTest extends AbstractMetadataBaseTest<DeleteNegativeValues> {

    private Map<String, String> parameters;

    public DeleteNegativeValuesTest() {
        super(new DeleteNegativeValues());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(DeleteNegativeValuesTest.class.getResourceAsStream("deleteNegativeValuesAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testName() throws Exception {
        assertEquals(action.getName(), "delete_negative_values");
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void should_delete() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-12", row.get("0001"));
    }

    @Test
    public void should_delete_percentage() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12%");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-12%", row.get("0001"));
    }

    @Test
    public void should_not_delete_percentage() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "12%");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("12%", row.get("0001"));
    }

    @Test
    public void should_delete_alt_format_1() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-1 200");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_delete_alt_format_2() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "(1 200,55)");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_delete_even_with_leading_space() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " -12"); // notice the space before ' Berlin'
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" -12", row.get("0001"));
    }

    @Test
    public void should_delete_even_with_trailing_space() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12 "); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-12 ", row.get("0001"));
    }

    @Test
    public void should_delete_even_with_enclosing_spaces() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " -12 "); // notice the spaces enclosing ' Berlin '
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" -12 ", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_not_found() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_delete_because_value_different() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "68");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("68", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_different_zero() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "0");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("0", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_null_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Michael Jordan");
        values.put("age", null);
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("Michael Jordan", row.get("name"));
        values.put("age", null);
    }

    @Test
    public void should_not_delete_because_nan() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Michael Jordan");
        values.put("age", "undefined");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("Michael Jordan", row.get("name"));
        values.put("age", "undefined");
    }

    @Test
    public void should_delete_even_with_space_between_sign_and_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "- 6");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("- 6", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_ALL));
    }

}
