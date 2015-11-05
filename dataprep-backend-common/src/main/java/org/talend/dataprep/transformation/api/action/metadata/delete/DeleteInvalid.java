package org.talend.dataprep.transformation.api.action.metadata.delete;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionScope;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionScope.INVALID;

/**
 * Delete row when value is invalid.
 */
@Component(DeleteInvalid.ACTION_BEAN_PREFIX + DeleteInvalid.DELETE_INVALID_ACTION_NAME)
public class DeleteInvalid extends AbstractDelete {

    /** the action name. */
    public static final String DELETE_INVALID_ACTION_NAME = "delete_invalid"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return DELETE_INVALID_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getActionScope()
     */
    @Override
    public List<String> getActionScope() {
        return Arrays.asList(new String[] { INVALID.getDisplayName() });
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    public boolean toDelete(ColumnMetadata colMetadata, Map<String, String> parsedParameters, String value) {
        // update invalid values of column metadata to prevent unnecessary future analysis
        if (ActionMetadataUtils.checkInvalidValue(colMetadata, value)) {
            return true;
        }
        // valid value
        return false;
    }
}
