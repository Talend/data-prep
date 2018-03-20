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

package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.COLUMN;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.LINE;

import java.util.*;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Delete action. Creates one consumer, and test it.
 *
 * @see Delete
 */
public class DeleteTest extends AbstractMetadataBaseTest<Delete> {

    public DeleteTest() {
        super(new Delete());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void should_be_in_data_cleansing_category() {
        //when
        final String name = action.getCategory(Locale.US);

        //then
        assertThat(name, is(DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void should_return_name() {
        //when
        final String name = action.getName();

        //then
        assertThat(name, is("delete"));
    }

    @Test
    public void should_adapt_to_line_scope() {

        //when
        final ActionDefinition adaptedAction = action.adapt(LINE);

        //then
        assertThat(adaptedAction.getDescription(Locale.US), is("Delete this row"));
        assertThat(adaptedAction.getLabel(Locale.US), is("Delete row"));

        assertThat( adaptedAction, not(is(action)) );
    }

    @Test
    public void should_adapt_to_column_scope() {
        //when
        final ActionDefinition adaptedAction = action.adapt(COLUMN);

        //then
        assertThat(adaptedAction.getDescription(Locale.US), is("Delete this column"));
        assertThat(adaptedAction.getLabel(Locale.US), is("Delete column"));
    }

    @Test
    public void should_delete_line_with_provided_row_id() {
        //given
        final Long rowId = 120L;

        final Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "toto");
        rowContent.put("0001", "tata");
        final DataSetRow row = new DataSetRow(rowContent);
        row.setTdpId(rowId);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", rowId.toString());

        assertThat(row.isDeleted(), is(false));

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.isDeleted(), is(true));
    }

    @Test
    public void should_NOT_delete_line_with_different_row_id() {
        //given
        final Long rowId = 120L;
        final Long parameterRowId = 1L;

        final Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "toto");
        rowContent.put("0001", "tata");
        final DataSetRow row = new DataSetRow(rowContent);
        row.setTdpId(rowId);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", parameterRowId.toString());

        assertThat(row.isDeleted(), is(false));

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.isDeleted(), is(false));
    }

    @Test
    public void multipleLineDelete() {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Paris");
        final DataSetRow original = new DataSetRow(values);
        final List<DataSetRow> rows = Arrays.asList(original.clone(), original.clone(), original.clone(), original.clone());
        for (long i = 0; i < rows.size(); i++) {
            final DataSetRow dataSetRow = rows.get((int) i);
            dataSetRow.setTdpId(i);
        }

        //when
        parameters.put("row_id", "1");
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));
        parameters.put("row_id", "2");
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(rows.get(0).isDeleted());
        assertTrue(rows.get(1).isDeleted());
        assertTrue(rows.get(2).isDeleted());
        assertFalse(rows.get(3).isDeleted());
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(3, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_ALL));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_DELETE_ROWS));
    }

}
