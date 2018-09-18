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

import static org.talend.dataprep.transformation.actions.category.ActionScope.INVALID;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Delete row when value is invalid.
 */
@Action(DeleteInvalid.DELETE_INVALID_ACTION_NAME)
public class DeleteInvalid extends AbstractDelete {

    /** the action name. */
    public static final String DELETE_INVALID_ACTION_NAME = "delete_invalid"; //$NON-NLS-1$

    @Override
    public String getName() {
        return DELETE_INVALID_ACTION_NAME;
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(INVALID.getDisplayName());
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public boolean toDelete(DataSetRow dataSetRow, String columnId, ActionContext context) {
        return dataSetRow.isInvalid(columnId);
    }

    @Override
    public Set<Behavior> getBehavior() {
        Set<Behavior> behaviors = super.getBehavior();
        behaviors.add(Behavior.NEED_STATISTICS_INVALID);
        return behaviors;
    }
}
