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

package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractCompareAction;
import org.talend.dataprep.transformation.api.action.metadata.common.CompareAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Unit test for the CompareNumbers action.
 *
 * @see CompareNumbers
 */
public class CompareNumbersTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private CompareNumbers action;

    /** The action parameters. */
    private Map<String, String> parameters;

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
        final List<Parameter> parameters = action.getParameters();
        assertEquals(6, parameters.size());
        assertTrue(parameters.stream().filter(p -> StringUtils.equals(p.getName(), CompareNumbers.COMPARE_MODE)).findFirst().isPresent());
        assertTrue(parameters.stream().filter(p -> StringUtils.equals(p.getName(), CompareNumbers.MODE_PARAMETER)).findFirst().isPresent());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.MATH.getDisplayName()));
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
    public void should_apply_on_column_not_equals() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "3", "Done !", "false");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_equals() {
        // given
        DataSetRow row = getRow("05", "5", "Done !");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("05", "5", "Done !", "true");
        assertEquals(expected, row);
    }

    @Test
    public void should_set_new_column_name() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source_eq_selected?").type(Type.BOOLEAN)
                .build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.put(CompareNumbers.MODE_PARAMETER, CompareNumbers.CONSTANT_MODE);
        parameters.put(CompareNumbers.CONSTANT_VALUE, "3");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("5", "Beer", "Done !", "");
        assertEquals(expected, row);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

}