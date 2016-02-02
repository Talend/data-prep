package org.talend.dataprep.transformation.api.action.metadata.clear;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Delete row
 */
public abstract class AbstractClear extends ActionMetadata implements ColumnAction {

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        final ColumnMetadata colMetadata = row.getRowMetadata().getById(columnId);
        if (toClear(colMetadata, value, context)) {
            row.set(columnId, "");
        }
    }

    /**
     *
     * @param colMetadata
     * @param value
     * @param context
     * @return return <code>true</code> if the column must be cleared
     */
    protected abstract boolean toClear(ColumnMetadata colMetadata, String value, ActionContext context);

}
