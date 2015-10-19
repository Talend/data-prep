package org.talend.dataprep.transformation.api.action.metadata.column;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Change the type of a column <b>This action is not displayed in the UI it's here to ease recording it as a Step It's
 * available from column headers</b>
 */
@Component(TypeChange.ACTION_BEAN_PREFIX + TypeChange.TYPE_CHANGE_ACTION_NAME)
public class TypeChange extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TYPE_CHANGE_ACTION_NAME = "type_change"; //$NON-NLS-1$

    public static final String NEW_TYPE_PARAMETER_KEY = "new_type";

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeChange.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TYPE_CHANGE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMN_METADATA.getDisplayName();
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        LOGGER.debug("TypeChange for columnId {} with parameters {} ", columnId, parameters);
        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
        if (columnMetadata == null) {
            // FIXME exception?
            return;
        }
        final String newType = parameters.get(NEW_TYPE_PARAMETER_KEY);
        if (StringUtils.isNotEmpty(newType)) {
            columnMetadata.setType(newType);
            columnMetadata.setTypeForced(true);
            // erase domain
            columnMetadata.setDomain("");
            columnMetadata.setDomainLabel("");
            columnMetadata.setDomainFrequency(0);
        }
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }
}
