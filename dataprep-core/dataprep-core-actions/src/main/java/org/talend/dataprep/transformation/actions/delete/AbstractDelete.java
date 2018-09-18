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

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract class used as base class for delete actions.
 */
public abstract class AbstractDelete extends AbstractActionMetadata implements ColumnAction {

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    /**
     * Return true if the given value should be deleted.
     *
     * @param dataSetRow the value to delete.
     * @param columnId the column to delete.
     * @param context the current action context (holds row metadata & parameters...).
     * @return true if the given value should be deleted.
     */
    public abstract boolean toDelete(final DataSetRow dataSetRow, String columnId, ActionContext context);

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        if (toDelete(row, columnId, context)) {
            row.setDeleted(true);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_ALL);
    }
}
