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
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
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
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractCompareAction;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the CompareNumbers action.
 *
 * @see CompareNumbers
 */
public class CompareNumbersTest extends AbstractMetadataBaseTest<CompareNumbers> {

    /** The action parameters. */
    private Map<String, String> parameters;

    public CompareNumbersTest() {
        super(new CompareNumbers());
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = CompareNumbersTest.class.getResourceAsStream("compareNumbersAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("compare_numbers", action.getName());
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(7, parameters.size());
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), CompareNumbers.COMPARE_MODE)));
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), CompareNumbers.MODE_PARAMETER)));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.NUMBERS.getDisplayName(Locale.US)));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void testComputeIntegerOperand() {
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("3").setValue2("3").setMode("eq"))));
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("003").setValue2("3.0").setMode("eq"))));
        assertFalse(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("1 200").setValue2("2,300").setMode("gt"))));
    }

    @Test
    public void testComputeDecimalOperand() {
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("3.0").setValue2("003").setMode("eq"))));
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("003.5333").setValue2("0").setMode("gt"))));
        assertFalse(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("1 200.5").setValue2("2,300.5").setMode("gt"))));
    }

    @Test
    public void testComputeScientificOperand() {
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("1.2E3").setValue2("1200").setMode("eq"))));
        assertFalse(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("1.2E3").setValue2("1200").setMode("ne"))));
    }

    @Test
    public void testComputePercentage() {
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("12%").setValue2("0.12").setMode("eq"))));
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("0.12").setValue2("12%").setMode("eq"))));
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("12%").setValue2("12%").setMode("eq"))));
        assertTrue(Boolean.parseBoolean( action.toStringCompareResult(new AbstractCompareAction.ComparisonRequest().setValue1("13%").setValue2("0.12").setMode("ne"))));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "3", "Done !", "false");
        assertEquals(expected, row);
    }

    @Test
    public void test_apply_inplace() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("false", "3", "Done !");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_equals() {
        // given
        DataSetRow row = getRow("05", "5", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("05", "5", "Done !", "true");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_equals_constant_1() {
        // given
        DataSetRow row = getRow("05", "5", "Done !");
        parameters.put(CompareNumbers.MODE_PARAMETER, CompareNumbers.CONSTANT_MODE);
        parameters.put(CompareNumbers.CONSTANT_VALUE, "6");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("05", "5", "Done !", "false");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_equals_constant_2() {
        // given
        DataSetRow row = getRow("05", "5", "Done !");
        parameters.put(CompareNumbers.MODE_PARAMETER, CompareNumbers.CONSTANT_MODE);
        parameters.put(CompareNumbers.CONSTANT_VALUE, "6");
        parameters.put(CompareNumbers.COMPARE_MODE, "ne");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("05", "5", "Done !", "true");
        assertEquals(expected, row);
    }

    @Test
    public void should_set_new_column_name() {
        // given
        final DataSetRow row = builder() //
                .with(value("5").type(Type.STRING).name("source")) //
                .with(value("3").type(Type.STRING).name("selected")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source_eq_selected?").type(Type.BOOLEAN)
                .build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_constant() {
        // given
        final DataSetRow row = builder() //
                .with(value("5").type(Type.STRING).name("source")) //
                .with(value("3").type(Type.STRING).name("selected")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();
        parameters.put(CompareNumbers.MODE_PARAMETER, CompareNumbers.CONSTANT_MODE);
        parameters.put(CompareNumbers.CONSTANT_VALUE, "3");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source_eq_3?").type(Type.BOOLEAN).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_not_apply_on_wrong_column() {
        // given
        DataSetRow row = getRow("5");

        // when
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "5");
    }

    @Test
    public void should_fail_wrong_column() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.put(NumericOperations.SELECTED_COLUMN_PARAMETER, "youpi");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "5");
        assertEquals(row.get("0001"), "3");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_fail_constant_missing_operand() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.remove(NumericOperations.OPERAND_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "5");
        assertEquals(row.get("0001"), "3");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_apply_on_column_not_valid() {
        // given
        DataSetRow row = getRow("5", "Beer", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "Beer", "Done !", "");
        assertEquals(expected, row);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
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
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
