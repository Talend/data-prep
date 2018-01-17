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

package org.talend.dataprep.transformation.actions.line;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.column.Concat;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.text.UpperCase;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class MakeLineHeaderTest extends AbstractMetadataBaseTest<MakeLineHeader> {

    public MakeLineHeaderTest() {
        super(new MakeLineHeader());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void should_be_in_data_cleansing_category() {
        // when
        final String name = action.getCategory(Locale.US);

        // then
        assertThat(name, is(DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void should_return_name() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("make_line_header"));
    }

    @Test
    public void should_delete_line_with_provided_row_id_and_not_skip() {
        // given
        long rowId = 120;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", row2.getTdpId().toString());
        parameters.put( MakeLineHeader.SKIP_UNTIL, Boolean.FALSE.toString() );

        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));

        //when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));

        assertThat(row2.isDeleted(), is(true));

        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
    }

    @Test
    public void should_keep_header_after_modifications() {
        // given
        Long rowId = 0L;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "Johnny");
        rowContent.put("0001", "Lennon");
        final DataSetRow row3 = new DataSetRow(rowContent);
        row3.setTdpId(rowId++);

        // when
        final Map<String, String> makeHeaderParameters = new HashMap<>();
        makeHeaderParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        makeHeaderParameters.put("row_id", row2.getTdpId().toString());
        final RunnableAction makeHeader = factory.create(action, makeHeaderParameters);
        final Map<String, String> upperCaseParameters = new HashMap<>();
        upperCaseParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        upperCaseParameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        final RunnableAction upperCase = factory.create(new UpperCase(), upperCaseParameters);
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, makeHeader, upperCase);

        // then
        assertEquals("John", row1.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row1.getRowMetadata().getById("0001").getName());
        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
        assertEquals("John", row3.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row3.getRowMetadata().getById("0001").getName());
    }

    @Test
    public void should_keep_header_after_modifications_with_concat() {
        // given
        Long rowId = 0L;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "Johnny");
        rowContent.put("0001", "Lennon");
        final DataSetRow row3 = new DataSetRow(rowContent);
        row3.setTdpId(rowId++);

        // when
        final Map<String, String> makeHeaderParameters = new HashMap<>();
        makeHeaderParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        makeHeaderParameters.put("row_id", row2.getTdpId().toString());
        final RunnableAction makeHeader = factory.create(action, makeHeaderParameters);
        final Map<String, String> concatColumnParameters = new HashMap<>();
        concatColumnParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        concatColumnParameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        concatColumnParameters.put(OtherColumnParameters.MODE_PARAMETER, "other_column_mode");
        concatColumnParameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        concatColumnParameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        final RunnableAction concat = factory.create(new Concat(), concatColumnParameters);
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, makeHeader, concat);

        // then
       // assertEquals(3, row3.getRowMetadata().getColumns().size());
        assertEquals("John", row1.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row1.getRowMetadata().getById("0001").getName());
        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
        assertEquals("John", row3.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row3.getRowMetadata().getById("0001").getName());
    }


    @Test
    public void should_delete_line_with_provided_row_id_and_previous_as_well() {
        // given
        long rowId = 120;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row3 = new DataSetRow(rowContent);
        row3.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", row2.getTdpId().toString());

        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(false));

        //when
        ActionTestWorkbench.test(Arrays.asList( row1,row2, row3 ), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(true));

        assertThat(row2.isDeleted(), is(true));

        assertThat(row3.isDeleted(), is(false));

        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
    }


    @Test
    public void should_use_mask_on_empty_cell() {
        // given
        long rowId = 120;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row3 = new DataSetRow(rowContent);
        row3.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", row2.getTdpId().toString());

        assertFalse(row1.isDeleted());
        assertFalse(row2.isDeleted());
        assertFalse(row3.isDeleted());

        //when
        ActionTestWorkbench.test(Arrays.asList( row1,row2, row3 ), actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row1.isDeleted());
        assertTrue(row2.isDeleted());
        assertFalse(row3.isDeleted());

        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Col 2", row2.getRowMetadata().getById("0001").getName());
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(4, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_NAME));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_ALL));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_DELETE_ROWS));
    }

}
