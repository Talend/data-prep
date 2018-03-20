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
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Contains action. Creates one consumer, and test it.
 *
 * @see Contains
 */
public class ContainsTest extends AbstractMetadataBaseTest<Contains> {

    private Map<String, String> parameters;

    public ContainsTest() {
        super(new Contains());
    }

    @Before
    public void init() throws IOException {
        parameters = new HashMap<>();
        parameters.put("column_id", "0001");
        parameters.put("column_name", "name");
        parameters.put("scope", "column");
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("contains", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS.getDisplayName(Locale.US)));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final DataSetRow row = builder() //
                .with(value("first").type(Type.STRING).name("source")) //
                .with(value("second").type(Type.STRING).name("name")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(Contains.MODE_PARAMETER, Contains.CONSTANT_MODE);
        parameters.put(Contains.CONSTANT_VALUE, "toubidou");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("name_contains_toubidou").type(Type.BOOLEAN)
                .build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_column_mode() {
        // given
        final DataSetRow row = builder() //
                .with(value("first").type(Type.STRING).name("source")) //
                .with(value("second").type(Type.STRING).name("name")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(Contains.MODE_PARAMETER, Contains.OTHER_COLUMN_MODE);
        parameters.put(Contains.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("name_contains_source").type(Type.BOOLEAN)
                .build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void contains_on_two_columns_true() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau tagada que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.OTHER_COLUMN_MODE);
        parameters.put(Contains.SELECTED_COLUMN_PARAMETER, "0000");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau tagada que voila");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void contains_on_two_columns_false() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau toubidou que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.OTHER_COLUMN_MODE);
        parameters.put(Contains.SELECTED_COLUMN_PARAMETER, "0000");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau toubidou que voila");
        expectedValues.put("0003", "false");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void contains_on_constant_true() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau toubidou que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.CONSTANT_MODE);
        parameters.put(Contains.CONSTANT_VALUE, "toubidou");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau toubidou que voila");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void contains_on_constant_false() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau toubidou que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.CONSTANT_MODE);
        parameters.put(Contains.CONSTANT_VALUE, "TOUBIDOU");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau toubidou que voila");
        expectedValues.put("0003", "false");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void wrong_params_with_no_mode() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Contains.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !");
        assertEquals(expected, row);
    }

    @Test
    public void wrong_params_on_constant_mode() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau toubidou que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.CONSTANT_MODE);
        parameters.remove(Contains.CONSTANT_VALUE);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau toubidou que voila");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void wrong_params_on_column_mode() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "tagada");
        values.put("0001", "oh le beau toubidou que voila");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Contains.MODE_PARAMETER, Contains.OTHER_COLUMN_MODE);
        parameters.remove(Contains.SELECTED_COLUMN_PARAMETER);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "tagada");
        expectedValues.put("0001", "oh le beau toubidou que voila");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(5, parameters.size());
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), "mode")));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
        assertFalse(action.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
