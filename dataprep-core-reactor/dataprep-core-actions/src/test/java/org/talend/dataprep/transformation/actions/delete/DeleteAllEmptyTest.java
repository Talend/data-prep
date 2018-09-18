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

package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DeleteAllEmpty action. Creates one consumer, and test it.
 *
 * @see DeleteAllEmpty
 */
public class DeleteAllEmptyTest extends AbstractMetadataBaseTest<DeleteAllEmpty> {

    /** The action to test. */
    private DeleteAllEmpty action = new DeleteAllEmpty();

    private Map<String, String> parameters;

    public DeleteAllEmptyTest() {
        super(new DeleteAllEmpty());
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.NA;
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(DeleteEmptyTest.class.getResourceAsStream("deleteAllEmptyAction.json"));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), hasItem("empty"));
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
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_delete_with_empty_row() {
        //given
        // row 1
        final DataSetRow row1 = getRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getRow("David", "Bowie");

        // row 3
        final DataSetRow row3 = getRow("", "");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(true));
    }

    @Test
    public void should_delete_with_blank_row() {
        //given
        // row 1
        final DataSetRow row1 = getRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getRow("David", "Bowie");

        // row 3
        final DataSetRow row3 = getRow("", " ");

        // row 4
        final DataSetRow row4 = getRow("     ", " ");

        // row 5
        final DataSetRow row5 = getRow("", "\n\t");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5), actionRegistry,
                factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(true));
        assertThat(row4.isDeleted(), is(true));
        assertThat(row5.isDeleted(), is(true));
    }

    @Test
    public void should_not_delete_with_keep() {
        //given
        // row 1
        final DataSetRow row1 = getRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getRow("David", "Bowie");

        // row 3
        final DataSetRow row3 = getRow(" ", "    ");

        // row4
        final DataSetRow row4 = getRow("", "");

        // row 5
        final DataSetRow row5 = getRow("", "\n\t");

        parameters.put(DeleteAllEmpty.ACTION_PARAMETER, DeleteAllEmpty.KEEP);
        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5), actionRegistry,
                factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(false));
        assertThat(row4.isDeleted(), is(true));
        assertThat(row5.isDeleted(), is(false));
    }

    @Test
    public void should_delete_with_multiple_empty() {
        //given
        // row 1
        final DataSetRow row1 = getRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getRow("", "");

        // row 3
        final DataSetRow row3 = getRow("", "");

        // row 4
        final DataSetRow row4 = getRow("David", "Bowie");

        // row 5
        final DataSetRow row5 = getRow("", null);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5), actionRegistry,
                factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(true));
        assertThat(row3.isDeleted(), is(true));
        assertThat(row4.isDeleted(), is(false));
        assertThat(row5.isDeleted(), is(true));
    }

    @Test
    public void should_not_delete_when_only_cells_empty() {
        //given
        // row 1
        final DataSetRow row1 = getRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getRow("a", "");

        // row 3
        final DataSetRow row3 = getRow(" ", "b");

        // row 4
        final DataSetRow row4 = getRow(null, "c");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4), actionRegistry,
                factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(false));
        assertThat(row4.isDeleted(), is(false));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_DELETE_ROWS));
    }
}
