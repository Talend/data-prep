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
package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

/**
 * Test class for ReorderColumn action.
 *
 * @see ReorderColumn
 */
public class ReorderColumnTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private ReorderColumn action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(ReorderColumnTest.class.getResourceAsStream("reorderAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void should_reorder_meta_only_2_columns() {

        // given
        DataSetRow row = createDataSetRow(2);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(2);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getName()).isEqualTo("1 col");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getName()).isEqualTo("0 col");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0000");
        assertValuesNotTouched(2, row);
    }

    @Test
    public void should_reorder_meta_with_4_columns_and_0000_to_0003() {

        // given
        DataSetRow row = createDataSetRow(4);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0000");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0003");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(4);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0003");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0000");
        assertValuesNotTouched(4, row);
    }

    @Test
    public void should_reorder_meta_with_4_columns_and_0003_to_0000() {

        // given
        DataSetRow row = createDataSetRow(4);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0003");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(4);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0003");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0002");
        assertValuesNotTouched(4, row);
    }

    @Test
    public void should_reorder_meta_with_5_columns_and_0004_to_0002() {

        // given
        DataSetRow row = createDataSetRow(5);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);
    }

    @Test
    public void should_reorder_2_times() {

        // -------------------------------------------------------
        // first reorder action:
        // -------------------------------------------------------
        // given
        DataSetRow row = createDataSetRow(5);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // second reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0002");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);
    }

    @Test
    public void should_reorder_3_times() {

        // -------------------------------------------------------
        // first reorder action:
        // -------------------------------------------------------
        // given
        DataSetRow row = createDataSetRow(5);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // second reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // third reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0002");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0004");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getId()).isEqualTo("0002");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(1).getId()).isEqualTo("0004");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(2).getId()).isEqualTo("0000");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(3).getId()).isEqualTo("0001");
        Assertions.assertThat(row.getRowMetadata().getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);
    }

    protected DataSetRow createDataSetRow(int columnNumber) {
        List<ColumnMetadata> input = new ArrayList<>(columnNumber);
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < columnNumber; i++) {
            input.add(createMetadata("000" + i, i + " col"));
            values.put("000" + i, "000" + i + " value");
        }
        RowMetadata rowMetadata = new RowMetadata(input);
        DataSetRow row = new DataSetRow(values);
        row.setRowMetadata(rowMetadata);
        return row;
    }

    protected void assertValuesNotTouched(int columnNumber, DataSetRow row) {
        for (int i = 0; i < columnNumber; i++) {
            Assertions.assertThat(row.get("000" + i)).isEqualTo("000" + i + " value");
        }
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.ANY)));
    }

}
