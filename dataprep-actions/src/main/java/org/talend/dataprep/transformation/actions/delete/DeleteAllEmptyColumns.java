package org.talend.dataprep.transformation.actions.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;

/**
 * Delete columns when they are empty.
 */
@Action(DeleteAllEmptyColumns.DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME)
public class DeleteAllEmptyColumns extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action name.
     */
    public static final String DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME = "delete_all_empty_columns";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllEmptyColumns.class);

    protected static final String ACTION_PARAMETER = "action_on_columns_with_blank";

    protected static final String DELETE = "delete";

    protected static final String KEEP = "keep";

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter.selectParameter(locale)
                .name(ACTION_PARAMETER)
                .item(DELETE, DELETE)
                .item(KEEP, KEEP)
                .defaultValue(DELETE)
                .build(this));

        return parameters;
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(COLUMN_METADATA.getDisplayName());
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        List<ColumnMetadata> columns = actionContext.getRowMetadata().getColumns();
        List<String> columnsToDelete = new ArrayList<>();

        for (ColumnMetadata column : columns) {
            if (isColumnToDelete(column, actionContext.getParameters().get(ACTION_PARAMETER))) {
                columnsToDelete.add(column.getId());
            }
        }

        columnsToDelete.forEach(columnId -> {
            actionContext.getRowMetadata().deleteColumnById(columnId);
            LOGGER.debug("DeleteColumn for columnId {}", columnId);
        });
        actionContext.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        //nothing to do here
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_DELETE_COLUMNS);
    }

    private boolean isColumnToDelete(ColumnMetadata columnMetadata, String ActionParameter) {
        switch (ActionParameter) {
            case KEEP:
                if (columnMetadata.getStatistics().getDataFrequencies().size() > 1) {
                    return false;
                }
            default:
                return columnMetadata.getQuality().getValid() + columnMetadata.getQuality().getInvalid() == 0;
        }
    }
}
