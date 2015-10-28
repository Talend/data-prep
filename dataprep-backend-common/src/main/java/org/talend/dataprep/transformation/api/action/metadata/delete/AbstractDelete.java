package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.util.Map;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Abstract class used as base class for delete actions.
 */
public abstract class AbstractDelete extends ActionMetadata implements ColumnAction {

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    /**
     * Return true if the given value should be deleted.
     *
     * @param colMetadata      the column metadata.
     * @param parsedParameters the delete action parameters.
     * @param value            the value to delete.
     * @return true if the given value should be deleted.
     */
    public abstract boolean toDelete(final ColumnMetadata colMetadata, final Map<String, String> parsedParameters, final String value);

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);
        final ColumnMetadata colMetadata = row.getRowMetadata().getById(columnId);
        if (toDelete(colMetadata, parameters, value)) {
            row.setDeleted(true);
        }
    }
}
