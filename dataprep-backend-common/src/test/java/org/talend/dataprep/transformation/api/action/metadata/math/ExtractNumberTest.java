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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
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
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

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
        // given
        DataSetRow row = getRow("5000");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5000");

    }

    @Test
    public void extract_simple_with_digit() throws Exception {
        // given
        DataSetRow row = getRow("5000.231");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5000.231");

    }

    @Test
    public void extract_simple_with_k() throws Exception {
        // given
        DataSetRow row = getRow("5k");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5000");

    }

    @Test
    public void extract_simple_with_k_and_euro() throws Exception {
        // given
        DataSetRow row = getRow("\u20ac5k");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
            .isNotEmpty().hasSize(2)
            .contains(ColumnMetadata.Builder //
                    .column() //
                    .name("0000" + "_number") //
                    .type(Type.NUMERIC) //
                    .computedId("0001") //
                    .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5000");

    }


    @Test
    public void extract_simple_with_k_and_digit() throws Exception {
        // given
        DataSetRow row = getRow("5.5k");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5500");

    }

    @Test
    public void extract_simple_with_k_first_and_digit() throws Exception {
        // given
        DataSetRow row = getRow("k5.5");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
            .isNotEmpty().hasSize(2)
            .contains(ColumnMetadata.Builder //
                    .column() //
                    .name("0000" + "_number") //
                    .type(Type.NUMERIC) //
                    .computedId("0001") //
                    .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("5500");
    }

    /**
     * Test that if we have a valid number as input, we keep as it as output.
     */
    @Test
    public void test_valid_numbers() throws Exception {
        test_valid_numbers("1.862E4", true);
        test_valid_numbers("(12.5)", true);
        test_valid_numbers("5.5", true);
        test_valid_numbers("1.86k", false);
        test_valid_numbers("1.86€", false);
    }

    public void test_valid_numbers(String input, boolean expected) throws Exception {
        // given
        DataSetRow row = getRow(input);
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2).contains(ColumnMetadata.Builder //
                .column() //
                .name("0000" + "_number") //
                .type(Type.NUMERIC) //
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
        // given
        DataSetRow row = getRow("");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("0");
    }

    @Test
    public void extract_nan() throws Exception {
        // given
        DataSetRow row = getRow("beer");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("0");

    }

    @Test
    public void extract_with_only_digits_and_k() throws Exception {
        // given
        DataSetRow row = getRow(".01k");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
                .isNotEmpty().hasSize(2)
                .contains(ColumnMetadata.Builder //
                        .column() //
                        .name("0000" + "_number") //
                        .type(Type.NUMERIC) //
                        .computedId("0001") //
                        .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("10");

    }

    @Test
    public void extract_with_only_digits_and_k_and_euro() throws Exception {
        // given
        DataSetRow row = getRow("\u20ac.01k");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()) //
            .isNotEmpty().hasSize(2)
            .contains(ColumnMetadata.Builder //
                          .column() //
                          .name("0000" + "_number") //
                          .type(Type.NUMERIC) //
                          .computedId("0001") //
                          .build());

        Assertions.assertThat(row.values()).isNotEmpty().hasSize(2);

        Assertions.assertThat(row.get("0001")).isEqualTo("10");

    }

}