package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Delete row when value is invalid.
 */
@Component(ClearInvalid.ACTION_BEAN_PREFIX + ClearInvalid.ACTION_NAME)
public class ClearInvalid extends ActionMetadata implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_invalid"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }


    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);
        final ColumnMetadata colMetadata = row.getRowMetadata().getById(columnId);
        if (!isValid(colMetadata, value)) {
            row.set(columnId, "");
        }
    }

    public boolean isValid(ColumnMetadata colMetadata, String value) {
        // update invalid values of column metadata to prevent unnecessary future analysis
        if (ActionMetadataUtils.checkInvalidValue(colMetadata, value)) {
            return false;
        }
        // valid value
        return true;
    }

}
