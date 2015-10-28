package org.talend.dataprep.transformation.api.action.metadata.bool;

import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Negate a boolean.
 *
 * @see Negate
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + Negate.NEGATE_ACTION_NAME)
public class Negate extends ActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String NEGATE_ACTION_NAME = "negate"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return NEGATE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.BOOLEAN.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.BOOLEAN.equals(Type.get(column.getType()));
    }


    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);
        if (isBoolean(value)) {
            final Boolean boolValue = Boolean.valueOf(value);
            row.set(columnId, WordUtils.capitalizeFully("" + !boolValue)); //$NON-NLS-1$
        }
    }

    private boolean isBoolean(final String value) {
        return value != null &&
                ("true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim())); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
