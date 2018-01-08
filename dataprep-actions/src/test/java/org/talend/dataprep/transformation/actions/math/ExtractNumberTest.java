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
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
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
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the ExtractNumber action.
 *
 * @see ExtractNumber
 */
public class ExtractNumberTest extends AbstractMetadataBaseTest<ExtractNumber> {

    /** The action parameters. */
    private Map<String, String> parameters;

    public ExtractNumberTest() {
        super(new ExtractNumber());
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = ExtractNumberTest.class.getResourceAsStream("extractNumberAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("extract_number", action.getName());
    }

    @Test
    public void shouldAcceptAllColumns() throws Exception {
        for (Type type : Type.values()) {
            Assert.assertTrue(action.acceptField(getColumn(type)));
        }
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        Assertions.assertThat(parameters).isNotNull().isNotEmpty().hasSize(4);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.SPLIT.getDisplayName(Locale.US)));
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
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "j'ai 10 ans");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "j'ai 10 ans");
        expectedValues.put("0003", "0");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void extract_simple() throws Exception {
        inner_test("5000", "5000", Type.INTEGER);
    }

    @Test
    public void extract_percentage() throws Exception {
        inner_test("50%", "0.50", Type.DOUBLE);
    }

    @Test
    public void extract_simple_with_digit() throws Exception {
        inner_test("5000.231", "5000.231", Type.DOUBLE);
    }

    @Test
    public void extract_simple_with_k() throws Exception {
        inner_test("5k", "5000", Type.INTEGER);
    }

    @Test
    public void extract_simple_with_k_and_euro() throws Exception {
        inner_test("\u20ac5k", "5000", Type.INTEGER);
    }

    @Test
    public void extract_simple_with_k_and_digit() throws Exception {
        inner_test("5.5k", "5500", Type.INTEGER);
    }

    @Test
    public void extract_simple_with_k_first_and_digit() throws Exception {
        inner_test("k5.5", "5500", Type.INTEGER);
    }

    @Test
    public void extract_simple_with_n_decimal() throws Exception {
        inner_test("1n", "0.000000001", Type.DOUBLE);
    }

    /**
     * Test that if we have a valid number as input, we keep as it as output.
     */
    @Test
    public void test_valid_numbers() throws Exception {
        test_valid_numbers("1.862E+4", true, Type.DOUBLE);
        test_valid_numbers("5.5", true, Type.DOUBLE);
        test_valid_numbers("1.86k", false, Type.INTEGER);
        test_valid_numbers("1.86€", false, Type.DOUBLE);
    }

    @Test
    public void test_number_parenthesis() throws Exception {
        test_valid_numbers("(12.5)", false, Type.DOUBLE); // (n) must be interpreted as -n (accounting convention).
    }

    private void test_valid_numbers(String input, boolean expected, Type expectedType) throws Exception {
        // given
        DataSetRow row = getRow(input);
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                analyzerService, // Test requires some analysis in asserts
                actionRegistry,
                factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2) //
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(expectedType) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        if (expected) {
            Assertions.assertThat(row.get("0001")).isEqualTo(input);
        } else {
            Assertions.assertThat(row.get("0001")).isNotEqualTo(input);
        }
    }

    @Test
    public void extract_empty() throws Exception {
        inner_test("", "0", Type.INTEGER);
    }

    @Test
    public void extract_nan() throws Exception {
        inner_test("beer", "0", Type.INTEGER);
    }

    @Test
    public void extract_with_only_digits_and_k() throws Exception {
        inner_test(".01k", "10", Type.INTEGER);
    }

    @Test
    public void extract_with_only_digits_and_k_and_euro() throws Exception {
        inner_test("\u20ac.01k", "10", Type.INTEGER);
    }

    @Test
    public void extract_with_G_and_other_prefix_before() throws Exception {
        inner_test("deca 2G", "2000000000", Type.INTEGER);
    }

    @Test
    public void extract_with_G_and_other_prefix_before_2() throws Exception {
        inner_test("h2G", "2000000000", Type.INTEGER);
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    private void inner_test(String from, String expected, Type expectedType) {
        // given
        DataSetRow row = getRow(from);
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                analyzerService, // Test requires some analysis in asserts
                actionRegistry, //
                factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2) //
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(expectedType) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo(expected);
    }

}
