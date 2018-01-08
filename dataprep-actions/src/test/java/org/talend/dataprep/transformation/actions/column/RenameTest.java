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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Rename action. Creates one consumer, and test it.
 *
 * @see Rename
 */
public class RenameTest extends AbstractMetadataBaseTest<Rename> {

    private Map<String, String> parameters;

    public RenameTest() {
        super(new Rename());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(CopyColumnTest.class.getResourceAsStream("renameAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), not(is(action)));
        boolean hasMetExpectedParameter = false;
        for (Parameter parameter : action.adapt(column).getParameters(Locale.US)) {
            if (Rename.NEW_COLUMN_NAME_PARAMETER_NAME.equals(parameter.getName())) {
                assertThat(parameter.getDefault(), is(column.getName()));
                hasMetExpectedParameter = true;
            }
        }
        assertThat(hasMetExpectedParameter, is(true));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.COLUMN_METADATA.getDisplayName(Locale.US)));
    }

    @Test
    public void should_update_metadata() {
        // given
        final ColumnMetadata metadata = //
        column() //
                .id(1) //
                .name("first name") //
                .type(Type.STRING) //
                .headerSize(102) //
                .empty(0) //
                .invalid(2) //
                .valid(5) //
                .build();
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(metadata);
        final RowMetadata rowMetadata = new RowMetadata(input);

        final ColumnMetadata renamedMetadata = column() //
                .id(1) //
                .name("NAME_FIRST") //
                .type(Type.STRING) //
                .headerSize(102) //
                .empty(0) //
                .invalid(2) //
                .valid(5) //
                .build();
        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(renamedMetadata);

        //when
        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row.getRowMetadata().getColumns());
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
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_NAME));
    }

}
