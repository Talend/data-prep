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

package org.talend.dataprep.transformation.actions.delete;

import static org.talend.dataprep.transformation.actions.category.ActionScope.EMPTY;

import java.util.Collections;
import java.util.List;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Delete row when value is empty.
 */
@Action(DeleteEmpty.DELETE_EMPTY_ACTION_NAME)
public class DeleteEmpty extends AbstractDelete implements ColumnAction {

    /**
     * The action name.
     */
    public static final String DELETE_EMPTY_ACTION_NAME = "delete_empty"; //$NON-NLS-1$

    @Override
    public String getName() {
        return DELETE_EMPTY_ACTION_NAME;
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(EMPTY.getDisplayName());
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public void compile(ActionContext actionContext) {
        // This action is able to deal with missing column, overrides default behavior
        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
    }

    /**
     * @see AbstractDelete#toDelete(DataSetRow, String, ActionContext)
     */
    @Override
    public boolean toDelete(final DataSetRow dataSetRow, String columnId, ActionContext context) {
        final String value = dataSetRow.get(columnId);
        return value == null || value.trim().length() == 0;
    }
}
