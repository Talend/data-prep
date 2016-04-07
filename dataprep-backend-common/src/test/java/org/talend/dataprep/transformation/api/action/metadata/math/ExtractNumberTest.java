// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
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
import org.talend.dataprep.parameters.Parameter;

/**
 * Unit test for the ExtractNumber action.
 *
 * @see ExtractNumber
 */
public class ExtractNumberTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private ExtractNumber action;

    /** The action parameters. */
    private Map<String, String> parameters;
    private Locale previousLocale;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = ExtractNumberTest.class.getResourceAsStream("extractNumberAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
        previousLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(previousLocale);
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("extract_number", action.getName());
    }

    @Test
    public void shouldAcceptAllColumns() throws Exception {
        for (Type type : Type.values()) {
            Assert.assertTrue(action.acceptColumn(getColumn(type)));
        }
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
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
        assertThat(action.getCategory(), is(ActionCategory.SPLIT.getDisplayName()));
    }

    @Test
    public void extract_simple() throws Exception {
        inner_test("5000", "5000", Type.INTEGER);
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

    public void test_valid_numbers(String input, boolean expected, Type expectedType) throws Exception {
        // given
        DataSetRow row = getRow(input);
        assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                c -> analyzerService.schemaAnalysis(c), // Test requires some analysis in asserts
                c -> analyzerService.full(c), // Test requires some analysis in asserts
                factory.create(action, parameters));

        // then
        assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2) //
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(expectedType) //
                        .computedId("0001") //
                        .build());

        assertThat(row.values()).isNotEmpty().hasSize(2);

        if (expected) {
            assertThat(row.get("0001")).isEqualTo(input);
        } else {
            assertThat(row.get("0001")).isNotEqualTo(input);
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

    public void inner_test(String from, String expected, Type expectedType) throws Exception {
        // given
        DataSetRow row = getRow(from);
        assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                c -> analyzerService.schemaAnalysis(c), // Test requires some analysis in asserts
                c -> analyzerService.full(c), // Test requires some analysis in asserts
                factory.create(action, parameters));

        // then
        assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2) //
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(expectedType) //
                        .computedId("0001") //
                        .build());

        assertThat(row.values()).isNotEmpty().hasSize(2);

        assertThat(row.get("0001")).isEqualTo(expected);
    }

}