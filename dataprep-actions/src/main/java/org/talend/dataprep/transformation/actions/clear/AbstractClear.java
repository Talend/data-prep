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

package org.talend.dataprep.transformation.actions.clear;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract class used as base class for clear cells actions.
 */
abstract class AbstractClear extends AbstractActionMetadata implements ColumnAction {

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        if (toClear(row, columnId, context)) {
            row.set(columnId, StringUtils.EMPTY);
        }
    }

    /**
     *
     * @param dataSetRow The data set row to be checked.
     * @param columnId The column id to check in <code>dataSetRow</code>.
     * @param actionContext The current action context.
     * @return return <code>true</code> if the column must be cleared
     */
    protected abstract boolean toClear(DataSetRow dataSetRow, String columnId, ActionContext actionContext);

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
