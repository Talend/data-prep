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
package org.talend.dataprep.transformation.actions.fill;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.util.*;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test to generate a sequence function.
 */
public class GenerateSequenceTest extends AbstractMetadataBaseTest<GenerateSequence> {

    public GenerateSequenceTest() {
        super(new GenerateSequence());
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("generate_a_sequence", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.NUMBERS.getDisplayName(Locale.US)));
    }

    @Test
    public void should_accept_every_column() {
        assertTrue(action.acceptField(null));
    }

    @Test
    public void testGetParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(7, parameters.size());
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");
        row.setTdpId(1L);
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "0");
        parameters.put(GenerateSequence.STEP_VALUE, "2");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "0012.50", "tata", "0");
        assertEquals(expectedRow.values(), row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_sequence").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // given
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "0");
        parameters.put(GenerateSequence.STEP_VALUE, "2");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        final DataSetRow row = getRow("toto", "0012.50", "tata");
        row.setTdpId(1L);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("0", "0012.50", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_generate_even() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "0");
        parameters.put(GenerateSequence.STEP_VALUE, "2");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        //row1
        Map<String, String> values = new HashMap<>();
        values.put("0000", " ");
        DataSetRow row1 = new DataSetRow(values);
        row1.setTdpId(1L);

        // row2
        Map<String, String> values2 = new HashMap<>();
        values2.put("0000", "");
        DataSetRow row2 = new DataSetRow(values2);
        row2.setTdpId(2L);

        // row3
        Map<String, String> values3 = new HashMap<>();
        values3.put("0000", " ");
        DataSetRow row3 = new DataSetRow(values3);
        row3.setTdpId(3L);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "0");

        Map<String, Object> expectedValues2 = new LinkedHashMap<>();
        expectedValues2.put("0000", "2");

        Map<String, Object> expectedValues3 = new LinkedHashMap<>();
        expectedValues3.put("0000", "4");

        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row1.values());
        assertEquals(expectedValues2, row2.values());
        assertEquals(expectedValues3, row3.values());
    }

    @Test
    public void should_generate_odd() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "1");
        parameters.put(GenerateSequence.STEP_VALUE, "2");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        //row1
        Map<String, String> values = new HashMap<>();
        values.put("0000", "John");
        DataSetRow row1 = new DataSetRow(values);
        row1.setTdpId(1L);

        // row2
        Map<String, String> values2 = new HashMap<>();
        values2.put("0000", "Lily");
        DataSetRow row2 = new DataSetRow(values2);
        row2.setTdpId(2L);

        // row3
        Map<String, String> values3 = new HashMap<>();
        values3.put("0000", "Lucy");
        DataSetRow row3 = new DataSetRow(values3);
        row3.setTdpId(3L);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "1");

        Map<String, Object> expectedValues2 = new LinkedHashMap<>();
        expectedValues2.put("0000", "3");

        Map<String, Object> expectedValues3 = new LinkedHashMap<>();
        expectedValues3.put("0000", "5");

        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row1.values());
        assertEquals(expectedValues2, row2.values());
        assertEquals(expectedValues3, row3.values());
    }

    @Test
    public void should_not_generate_with_empty_start_value() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, null);
        parameters.put(GenerateSequence.STEP_VALUE, "2");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Lily");
        final DataSetRow row1 = new DataSetRow(values);

        // row2
        Map<String, String> values2 = new HashMap<>();
        values2.put("0000", "Lucy");
        DataSetRow row2 = new DataSetRow(values2);
        row2.setTdpId(2L);

        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));
        // then
        assertEquals("Lily", row1.get("0000"));
        assertEquals("Lucy", row2.get("0000"));
    }

    @Test
    public void should_not_generate_with_empty_step_value() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "000");
        parameters.put(GenerateSequence.START_VALUE, "1");
        parameters.put(GenerateSequence.STEP_VALUE, " ");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Lily");
        final DataSetRow row = new DataSetRow(values);

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        // then
        assertEquals("Lily", row.get("0000"));
    }

    @Test
    public void should_generate_with_deleted_row() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "1");
        parameters.put(GenerateSequence.STEP_VALUE, "2");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        // row1
        Map<String, String> values = new HashMap<>();
        values.put("0000", "John");
        DataSetRow row1 = new DataSetRow(values);
        row1.setTdpId(1L);

        // row2
        Map<String, String> values2 = new HashMap<>();
        values2.put("0000", "Lily");
        DataSetRow row2 = new DataSetRow(values2);
        row2.setTdpId(2L);
        row2.setDeleted(true);

        // row3
        Map<String, String> values3 = new HashMap<>();
        values3.put("0000", "Lucy");
        DataSetRow row3 = new DataSetRow(values3);
        row3.setTdpId(3L);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "1");

        Map<String, Object> expectedValues2 = new LinkedHashMap<>();
        expectedValues2.put("0000", "Lily");

        Map<String, Object> expectedValues3 = new LinkedHashMap<>();
        expectedValues3.put("0000", "3");

        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row1.values());
        assertEquals(expectedValues2, row2.values());
        assertEquals(expectedValues3, row3.values());
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
    }

    @Test
    public void test_CalcSequence() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(GenerateSequence.START_VALUE, "1");
        parameters.put(GenerateSequence.STEP_VALUE, "2");

        final GenerateSequence.CalcSequence sequence = new GenerateSequence.CalcSequence(parameters);

        assertEquals("1", sequence.getNextValue());
        assertEquals("3", sequence.getNextValue());
        assertEquals("5", sequence.getNextValue());
    }
}
