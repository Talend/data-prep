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

package org.talend.dataprep.transformation.actions.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class DeleteColumnTest extends AbstractMetadataBaseTest<DeleteColumn> {

    private RowMetadata rowMetadata;

    private Map<String, String> parameters;

    public DeleteColumnTest() {
        super(new DeleteColumn());
    }

    @Before
    public void init() {
        List<ColumnMetadata> columns = new ArrayList<>();
        ColumnMetadata columnMetadata = ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0001") //
                .domain("ID") //
                .domainFrequency(1) //
                .domainLabel("Identifier") //
                .build();
        columns.add(columnMetadata);
        columnMetadata = ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0002") //
                .domain("ANL") //
                .domainFrequency(1) //
                .domainLabel("Animal") //
                .build();
        columns.add(columnMetadata);
        rowMetadata = new RowMetadata();
        rowMetadata.setColumns(columns);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void shouldDeleteColumn() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "1");
        values.put("0002", "Wolf");
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        assertEquals(1, rowSize);
    }

    @Test
    public void shouldDeleteColumnWhenMoreThanOneRow() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "1");
        values.put("0002", "Wolf");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, String> values2 = new HashMap<>();
        values2.put("0001", "2");
        values2.put("0002", "Lion");
        final DataSetRow row2 = new DataSetRow(rowMetadata, values2);

        // when
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
        ActionTestWorkbench.test(Arrays.asList(row, row2), actionRegistry, factory.create(action, parameters));

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        final int row2Size = row2.getRowMetadata().getColumns().size();
        assertEquals(1, rowSize);
        assertEquals(1, row2Size);
    }

    @Test
    public void shouldDeleteAllColumns() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "1");
        values.put("0002", "Wolf");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, String> values2 = new HashMap<>();
        values.put("0001", "2");
        values.put("0002", "Lion");
        final DataSetRow row2 = new DataSetRow(rowMetadata, values2);

        // when
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
        final RunnableAction action1 = factory.create(action, parameters);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");
        final RunnableAction action2 = factory.create(action, parameters);
        ActionTestWorkbench.test(row, actionRegistry, action1, action2);
        ActionTestWorkbench.test(row2, actionRegistry, action1, action2);

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        final int row2Size = row2.getRowMetadata().getColumns().size();
        assertEquals(0, rowSize);
        assertEquals(0, row2Size);
    }

    @Test
    public void testCategory() throws Exception {
        // We test the real value of the category here (not based on the enum), because the frontent use this label for
        // display purpose:
        assertThat(action.getCategory(Locale.US), is("columns"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_DELETE_COLUMNS));
    }

}
