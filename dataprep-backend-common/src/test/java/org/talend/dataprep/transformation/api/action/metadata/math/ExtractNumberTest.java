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
import java.util.Map;

import org.assertj.core.api.Assertions;
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
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
        Assertions.assertThat(parameters).isNotNull().isNotEmpty().hasSize(5);
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
        ActionTestWorkbench.test(row, action.create(parameters));

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
        parameters.put(ExtractNumber.DECIMAL_SEPARATOR, ExtractNumber.DOT);

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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
        ActionTestWorkbench.test(row, action.create(parameters));

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
        parameters.put(ExtractNumber.DECIMAL_SEPARATOR, ExtractNumber.DOT);

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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
    public void extract_empty() throws Exception {
        // given
        DataSetRow row = getRow("");
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(1);
        parameters.put(ExtractNumber.DECIMAL_SEPARATOR, ExtractNumber.DOT);

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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
        parameters.put(ExtractNumber.DECIMAL_SEPARATOR, ExtractNumber.DOT);

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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
        parameters.put(ExtractNumber.DECIMAL_SEPARATOR, ExtractNumber.DOT);

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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