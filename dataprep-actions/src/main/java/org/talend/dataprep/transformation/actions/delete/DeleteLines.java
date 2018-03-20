// ============================================================================
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

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This action is used to delete lines that match a filter.
 *
 * With no filter, it will delete all lines!
 *
 */
@Action(DeleteLines.DELETE_LINES_ACTION_NAME)
public class DeleteLines extends AbstractFilteringAction {

    static final String DELETE_LINES_ACTION_NAME = "delete_lines";

    @Override
    public String getName() {
        return DELETE_LINES_ACTION_NAME;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (context.getFilter().test(row)) {
            row.setDeleted(true);
        }
    }

}
