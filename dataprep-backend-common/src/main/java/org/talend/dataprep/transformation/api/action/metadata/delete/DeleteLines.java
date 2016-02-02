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

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.FILTERED;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * This action is used to delete lines that match a filter.
 *
 * With no filter, it will delete all lines!
 *
 */
@Component(DeleteLines.ACTION_BEAN_PREFIX + DeleteLines.DELETE_LINES_ACTION_NAME)
public class DeleteLines extends ActionMetadata implements ColumnAction {

    public static final String DELETE_LINES_ACTION_NAME = "delete_lines";

    @Override
    public String getName() {
        return DELETE_LINES_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return FILTERED.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    protected boolean implicitFilter() {
        return false;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (getFilter(context.getParameters()).test(row)) {
            row.setDeleted(true);
        }
    }
}
