package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.util.Map;
import java.util.Set;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Base class for all abstract fillIfInvalid actions.
 */
public abstract class AbstractFillIfInvalid extends AbstractActionMetadata implements ColumnAction {

    /** Default parameter name. */
    public static final String DEFAULT_VALUE_PARAMETER = "invalid_default_value"; //$NON-NLS-1$

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {

        final String value = row.get(columnId);

        // olamy do we really consider null as a valid value?
        // note we don't for Date see @FillWithDateIfInvalid
        if (value == null) {
            return;
        }

        final ColumnMetadata colMetadata = row.getRowMetadata().getById(columnId);

        if (ActionMetadataUtils.checkInvalidValue(colMetadata, value)) {
            String newValue = getDefaultValue(row, parameters, columnId);
            row.set(columnId, newValue);
            // update invalid values of column metadata to prevent unnecessary future analysis
            final Set<String> invalidValues = colMetadata.getQuality().getInvalidValues();
            invalidValues.add(value);
        }
    }

    /**
     * Default implementation gets the default value from parameters.
     * Can be override, like in FillWithDateIfEmpty.
     */
    protected String getDefaultValue(DataSetRow row, Map<String, String> parameters, String columnId) {
        return parameters.get(DEFAULT_VALUE_PARAMETER);
    }

}
