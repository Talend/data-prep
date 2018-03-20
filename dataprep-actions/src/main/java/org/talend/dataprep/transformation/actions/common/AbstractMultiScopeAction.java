// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
// ============================================================================

package org.talend.dataprep.transformation.actions.common;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/** AbstractMultiScopeAction is usefull for action with multiple scopes
 * the behavior of this class is to got less modifications when you want to add datascope to an existing class */
public abstract class AbstractMultiScopeAction extends AbstractActionMetadata implements ColumnAction, DataSetAction {

    protected ScopeCategory scope;

    public AbstractMultiScopeAction(ScopeCategory scope) {
        this.scope = scope;
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            if (acceptField(column)) {
                apply(row, column.getId(), column.getId(), context);
            }
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        apply(row, context.getColumnId(), ActionsUtils.getTargetColumnId(context), context);
    }

    /** apply will be Override by action and will contain the function */
    public abstract void apply(DataSetRow row, String columnId, String targetColumnId, ActionContext context);

}
