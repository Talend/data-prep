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

package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

public class DeleteColumnTest {

    private DeleteColumn deleteColumn;

    private RowMetadata rowMetadata;
    private Map<String, String> parameters;

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
        deleteColumn = new DeleteColumn();

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
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
        ActionTestWorkbench.test(row, deleteColumn.create(parameters).getRowAction());

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        assertEquals(1, rowSize);
        assertFalse(row.values().containsKey("0002"));
        assertEquals(1, row.values().size());
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
        final DataSetRowAction rowAction = deleteColumn.create(parameters).getRowAction();
        ActionTestWorkbench.test(Arrays.asList(row, row2), rowAction);

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        final int row2Size = row2.getRowMetadata().getColumns().size();
        assertEquals(1, rowSize);
        assertEquals(1, row2Size);
        assertFalse(row.values().containsKey("0002"));
        assertFalse(row2.values().containsKey("0002"));
        assertEquals(1, row.values().size());
        assertEquals(1, row2.values().size());
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
        final DataSetRowAction action1 = deleteColumn.create(parameters).getRowAction();
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");
        final DataSetRowAction action2 = deleteColumn.create(parameters).getRowAction();
        ActionTestWorkbench.test(row, action1, action2);
        ActionTestWorkbench.test(row2, action1, action2);

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        final int row2Size = row2.getRowMetadata().getColumns().size();
        assertEquals(0, rowSize);
        assertEquals(0, row2Size);
        assertFalse(row.values().containsKey("0002"));
        assertFalse(row2.values().containsKey("0002"));
        assertEquals(0, row.values().size());
        assertEquals(0, row2.values().size());
    }

    @Test
    public void testCategory() throws Exception {
        // We test the real value of the category here (not based on the enum), because the frontent use this label for
        // display purpose:
        assertThat(deleteColumn.getCategory(), is("column_metadata"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(deleteColumn.acceptColumn(getColumn(Type.STRING)));
        assertTrue(deleteColumn.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(deleteColumn.acceptColumn(getColumn(Type.FLOAT)));
        assertTrue(deleteColumn.acceptColumn(getColumn(Type.DATE)));
        assertTrue(deleteColumn.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(deleteColumn.adapt((ColumnMetadata) null), is(deleteColumn));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(deleteColumn.adapt(column), is(deleteColumn));
    }

}
