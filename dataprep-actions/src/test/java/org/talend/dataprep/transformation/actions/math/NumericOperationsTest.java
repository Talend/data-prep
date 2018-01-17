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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the NumericOperations action.
 *
 * @see NumericOperations
 */
public class NumericOperationsTest extends AbstractMetadataBaseTest<NumericOperations> {

    /** The action parameters. */
    private Map<String, String> parameters;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public NumericOperationsTest() {
        super(new NumericOperations());
    }

    @Before
    public void setUp() throws Exception {
        action = new NumericOperations();
        final InputStream parametersSource = NumericOperationsTest.class.getResourceAsStream("numericOpsAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("numeric_ops", action.getName());
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(7, parameters.size());
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), "operator")));
        assertTrue(parameters.stream().anyMatch(p -> StringUtils.equals(p.getName(), "mode")));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.MATH.getDisplayName(Locale.US)));
    }

    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void testCompute() {
        // valid
        assertEquals("5", action.compute("3", "+", "2"));
        assertEquals("6", action.compute("3", "x", "2"));
        assertEquals("1", action.compute("3", "-", "2"));
        assertEquals("2", action.compute("4", "/", "2"));

        // silently fail when divide by 0
        assertEquals("", action.compute("3", "/", "0"));

        // empty items:
        assertEquals("", action.compute("", "/", "2"));
        assertEquals("", action.compute("3", "", "2"));
        assertEquals("", action.compute("3", "/", ""));

        // invalid items:
        assertEquals("", action.compute("a", "/", "2"));
        assertEquals("", action.compute("3", "a", "2"));
        assertEquals("", action.compute("3", "/", "a"));

        // null items:
        assertEquals("", action.compute(null, "/", "2"));
        assertEquals("", action.compute("3", null, "2"));
        assertEquals("", action.compute("3", "/", null));

        // percentage
        assertEquals("2.03", action.compute("3%", "+", "2"));
        assertEquals("0.06", action.compute("3%", "x", "2"));
        assertEquals("-1.97", action.compute("3%", "-", "2"));
        assertEquals("0.02", action.compute("4%", "/", "2"));
        // should be a wrong value, so it returns an empty string
        assertEquals("", action.compute("5%56", "*", "2"));
    }

    @Test
    public void testComputeScaleAndRoundMultiply() {
        assertEquals("6.66", action.compute("3.33", "x", "2"));
        assertEquals("36.2222", action.compute("18.1111", "x", "2"));
    }

    @Test
    public void testComputeScaleAndRoundDivide() {
        assertEquals("1.5", action.compute("3", "/", "2"));
        assertEquals("2", action.compute("18", "/", "9"));
        assertEquals("2.111111111111111", action.compute("19", "/", "9"));
        assertEquals("211.111111111111111", action.compute("1900", "/", "9"));
        assertEquals("21111.111111111111111", action.compute("190000", "/", "9"));
    }

    @Test
    public void testComputeScaleAndRoundAdd() {
        assertEquals("5.33", action.compute("3.33", "+", "2"));
        assertEquals("20.1111", action.compute("18.1111", "+", "2"));
    }

    @Test
    public void testComputeScaleAndRoundSubstract() {
        assertEquals("1.33", action.compute("3.33", "-", "2"));
        assertEquals("16.1111", action.compute("18.1111", "-", "2"));
    }

    @Test
    public void testComputeDecimalOperand() {
        assertEquals("5.1", action.compute("3", "+", "2.1"));
        assertEquals("6.3", action.compute("3", "x", "2.1"));
        assertEquals("0.9", action.compute("3", "-", "2.1"));
        assertEquals("1.904761904761905", action.compute("4", "/", "2.1"));
    }

    @Test
    public void testComputeAltFormat() {
        assertEquals("5.15", action.compute("3,05", "+", "2.1"));
        assertEquals("6300", action.compute("3 000", "x", "2.1"));
    }

    @Test
    public void testComputeScientificOperand() {
        assertEquals("2400", action.compute("1.2E3", "x", "2"));
        assertEquals("2400", action.compute("2", "x", "1.2E3"));
        assertEquals("2640", action.compute("2.2", "x", "1.2E3"));

    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow rowExpected = getRow("5", "3", "Done !", "8");
        assertEquals(rowExpected, row);
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000 + 0001").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("8", "3", "Done !");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_created_column() {
        // given
        DataSetRow row = getRow("5", "3");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "3", "8", "11");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.remove(NumericOperations.SELECTED_COLUMN_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "3", "Done !", "7");
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
        parameters.remove(NumericOperations.OPERAND_PARAMETER);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source + selected").type(Type.DOUBLE).build();
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
        parameters.remove(NumericOperations.SELECTED_COLUMN_PARAMETER);
        parameters.put(NumericOperations.MODE_PARAMETER, NumericOperations.CONSTANT_MODE);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source + 2").type(Type.DOUBLE).build();
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
