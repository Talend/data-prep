package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.DataFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class DeleteAllEmptyColumnsTest extends AbstractMetadataBaseTest<DeleteAllEmptyColumns> {

    /** the action to test. */
    private DeleteAllEmptyColumns action = new DeleteAllEmptyColumns();

    private Map<String, String> parameters;

    private RowMetadata rowMetadata;

    public DeleteAllEmptyColumnsTest() {
        super(new DeleteAllEmptyColumns());
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.NA;
    }

    @Before
    public void init() throws IOException {
        List<ColumnMetadata> columns = new ArrayList<>();
        Statistics statistics1 = new Statistics();
        statistics1.setDataFrequencies(Arrays.asList(new DataFrequency("blip", 10)));
        statistics1.setCount(10);
        ColumnMetadata columnMetadata = column() //
                .type(Type.INTEGER) //
                .computedId("0000") //
                .valid(10) //
                .statistics(statistics1) //
                .build();
        columns.add(columnMetadata);
        Statistics statistics2 = new Statistics();
        statistics2.setDataFrequencies(Arrays.asList(new DataFrequency("", 10)));
        statistics2.setCount(10);
        columnMetadata = column() //
                .type(Type.STRING) //
                .computedId("0001") //
                .valid(0) //
                .empty(10) //
                .invalid(0) //
                .statistics(statistics2) //
                .build();
        columns.add(columnMetadata);
        Statistics statistics3 = new Statistics();
        statistics3.setDataFrequencies(Arrays.asList(new DataFrequency("blop", 10)));
        statistics3.setCount(10);
        columnMetadata = column() //
                .type(Type.STRING) //
                .statistics(statistics3) //
                .computedId("0002") //
                .valid(10) //
                .build();
        columns.add(columnMetadata);
        Statistics statistics4 = new Statistics();
        statistics4.setDataFrequencies(Arrays.asList(new DataFrequency("", 4), new DataFrequency(" ", 6)));
        statistics4.setCount(10);
        columnMetadata = column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .empty(10) //
                .statistics(statistics4)
                .build();
        columns.add(columnMetadata);
        rowMetadata = new RowMetadata();
        rowMetadata.setColumns(columns);

        parameters = ActionMetadataTestUtils
                .parseParameters(DeleteAllEmptyColumns.class.getResourceAsStream("deleteAllEmptyColumnsAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void shouldAcceptColumn() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void shouldHaveExpectedBehavior() {
        assertEquals(3, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_DELETE_COLUMNS));
    }

    /**
     * Next tests only use column metadatas and not the column content
     */
    @Test
    public void shouldDeleteColumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "1");
        values.put("0001", "");
        values.put("0002", "blop");
        values.put("0003", " ");
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(DeleteAllEmptyColumns.ACTION_PARAMETER, DeleteAllEmptyColumns.DELETE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        assertEquals(2, rowSize);
        assertNotNull(row.getRowMetadata().getById("0000"));
        assertNull(row.getRowMetadata().getById("0001"));
        assertNotNull(row.getRowMetadata().getById("0002"));
        assertNull(row.getRowMetadata().getById("0003"));
    }

    @Test
    public void shouldDeleteOnlyOneColumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "1");
        values.put("0001", "");
        values.put("0002", "blop");
        values.put("0003", " ");
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(DeleteAllEmptyColumns.ACTION_PARAMETER, DeleteAllEmptyColumns.KEEP);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final int rowSize = row.getRowMetadata().getColumns().size();
        assertEquals(3, rowSize);
        assertNotNull(row.getRowMetadata().getById("0000"));
        assertNull(row.getRowMetadata().getById("0001"));
        assertNotNull(row.getRowMetadata().getById("0002"));
        assertNotNull(row.getRowMetadata().getById("0003"));
    }
}
