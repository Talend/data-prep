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

package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.COLUMN;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.LINE;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

/**
 * Test class for Delete action. Creates one consumer, and test it.
 *
 * @see Delete
 */
public class DeleteTest extends AbstractMetadataBaseTest {

    @Autowired
    private Delete action;


    @Test
    public void should_be_in_data_cleansing_category() {
        //when
        final String name = action.getCategory();

        //then
        assertThat(name, is(DATA_CLEANSING.getDisplayName()));
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
        final ActionMetadata adaptedAction = action.adapt(LINE);

        //then
        assertThat(adaptedAction.getDescription(), is("Delete this line"));
        assertThat(adaptedAction.getLabel(), is("Delete Line"));

        assertThat( adaptedAction, not(is(action)) );
    }

    @Test
    public void should_adapt_to_column_scope() {
        //when
        final ActionMetadata adaptedAction = action.adapt(COLUMN);

        //then
        assertThat(adaptedAction.getDescription(), is("Delete this column"));
        assertThat(adaptedAction.getLabel(), is("Delete Column"));
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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        assertThat(row.isDeleted(), is(false));
    }
}
