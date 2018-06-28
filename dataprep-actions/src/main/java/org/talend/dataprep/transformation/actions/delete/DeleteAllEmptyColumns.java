package org.talend.dataprep.transformation.actions.delete;

import org.apache.commons.lang.StringUtils;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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

    protected static final String ACTION_PARAMETER = "action_on_columns_with_blank";

    protected static final String DELETE = "delete";

    protected static final String KEEP = "keep";

    private static final String COLUMNS_TO_DELETE = "column_to_delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllEmptyColumns.class);

    /**
     * This method will be call only at the first call of the apply.
     * Case KEEP : test if the DataFrequencies is higher than 1
     * because " " and "" are not the same.
     * It test also if the first cell is a non-printing character
     * in case of the column is full of the same non-printing
     * character.
     * The the default test is to test by the quality if all the
     * cells are empty.
     * character
     * @param columnMetadata
     * @param parameter
     * @param row
     */
    private static boolean isColumnToDelete(ColumnMetadata columnMetadata, String parameter, DataSetRow row) {
        if (KEEP.equals(parameter)) {
            if (columnMetadata.getStatistics().getDataFrequencies().size() > 1 || //
                    StringUtils.isNotEmpty(row.get(columnMetadata.getId()))) {
                return false;
            }
        }
        return columnMetadata.getQuality().getValid() + columnMetadata.getQuality().getInvalid() == 0;
    }

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter
                .selectParameter(locale)
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
        actionContext.get(COLUMNS_TO_DELETE, p -> new HashSet());
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        final List<ColumnMetadata> columns = context.getRowMetadata().getColumns();
        final Set<String> columnsToDelete = context.get(COLUMNS_TO_DELETE);

        if (columnsToDelete.isEmpty()) {
            for (ColumnMetadata column : columns) {
                if (isColumnToDelete(column, context.getParameters().get(ACTION_PARAMETER), row)) {
                    columnsToDelete.add(column.getId());
                }
            }
            if (columnsToDelete.isEmpty()) {
                context.setActionStatus(ActionContext.ActionStatus.DONE);
            }
            context.get(COLUMNS_TO_DELETE, p -> columnsToDelete);
        }
        columnsToDelete.forEach(columnId -> {
            LOGGER.debug("DeleteColumn for columnId {}", columnId);
            row.deleteColumnById(columnId);
            context.getRowMetadata().deleteColumnById(columnId);
        });
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_DELETE_COLUMNS, Behavior.NEED_STATISTICS_QUALITY, //
                Behavior.NEED_STATISTICS_FREQUENCY);
    }
}
