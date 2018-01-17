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
package org.talend.dataprep.transformation.actions.column;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for ReorderColumn action.
 *
 * @see ReorderColumn
 */
public class ReorderColumnTest extends AbstractMetadataBaseTest<ReorderColumn> {

    private Map<String, String> parameters;

    public ReorderColumnTest() {
        super(new ReorderColumn());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata rowMetadata = row.getRowMetadata();
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(2);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getName()).isEqualTo("1 col");
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getName()).isEqualTo("0 col");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0000");
        assertValuesNotTouched(2, row);
    }

    @Test
    public void should_reorder_meta_with_4_columns_and_0000_to_0003() {

        // given
        DataSetRow row = createDataSetRow(4);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0000");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0003");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata rowMetadata = row.getRowMetadata();
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(4);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0003");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0000");
        assertValuesNotTouched(4, row);
    }

    @Test
    public void should_reorder_meta_with_4_columns_and_0003_to_0000() {

        // given
        DataSetRow row = createDataSetRow(4);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0003");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata actual = row.getRowMetadata();
        Assertions.assertThat(actual.getColumns()).isNotEmpty().hasSize(4);
        Assertions.assertThat(actual.getColumns().get(0).getId()).isEqualTo("0003");
        Assertions.assertThat(actual.getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(actual.getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(actual.getColumns().get(3).getId()).isEqualTo("0002");
        assertValuesNotTouched(4, row);
    }

    @Test
    public void should_reorder_meta_with_5_columns_and_0004_to_0002() {

        // given
        DataSetRow row = createDataSetRow(5);

        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata rowMetadata = row.getRowMetadata();
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata rowMetadata = row.getRowMetadata();
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // second reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0002");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0004");
        Assertions.assertThat(rowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata actualRowMetadata = row.getRowMetadata();
        final RowMetadata rowMetadata = actualRowMetadata;
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0000");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0004");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // second reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0004");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(rowMetadata.getColumns().get(0).getId()).isEqualTo("0004");
        Assertions.assertThat(rowMetadata.getColumns().get(1).getId()).isEqualTo("0000");
        Assertions.assertThat(rowMetadata.getColumns().get(2).getId()).isEqualTo("0001");
        Assertions.assertThat(rowMetadata.getColumns().get(3).getId()).isEqualTo("0002");
        Assertions.assertThat(rowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);

        // -------------------------------------------------------
        // third reorder action:
        // -------------------------------------------------------
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0002");
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0004");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(actualRowMetadata.getColumns()).isNotEmpty().hasSize(5);
        Assertions.assertThat(actualRowMetadata.getColumns().get(0).getId()).isEqualTo("0002");
        Assertions.assertThat(actualRowMetadata.getColumns().get(1).getId()).isEqualTo("0004");
        Assertions.assertThat(actualRowMetadata.getColumns().get(2).getId()).isEqualTo("0000");
        Assertions.assertThat(actualRowMetadata.getColumns().get(3).getId()).isEqualTo("0001");
        Assertions.assertThat(actualRowMetadata.getColumns().get(4).getId()).isEqualTo("0003");

        assertValuesNotTouched(5, row);
    }

    protected DataSetRow createDataSetRow(int columnNumber) {
        ValuesBuilder builder = builder();
        for (int i = 0; i < columnNumber; i++) {
            builder = builder.with(value("000" + i + " value").type(Type.STRING).name(i + " col"));
        }
        return builder.build();
    }

    protected void assertValuesNotTouched(int columnNumber, DataSetRow row) {
        for (int i = 0; i < columnNumber; i++) {
            Assertions.assertThat(row.get("000" + i)).isEqualTo("000" + i + " value");
        }
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
