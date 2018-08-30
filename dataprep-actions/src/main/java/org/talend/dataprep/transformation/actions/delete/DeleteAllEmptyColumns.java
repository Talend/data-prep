package org.talend.dataprep.transformation.actions.delete;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

/**
 * Delete columns when they are empty.
 */
@Action(DeleteAllEmptyColumns.DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME)
public class DeleteAllEmptyColumns extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action name.
     */
    static final String DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME = "delete_all_empty_columns";

    static final String ACTION_PARAMETER = "action_on_columns_with_blank";

    static final String DELETE = "delete";

    static final String KEEP = "keep";

    private static final String COLUMNS_TO_DELETE = "column_to_delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllEmptyColumns.class);

    /**
     * Case KEEP : test on the the DataFrequencies because empty and non-printing character are not the same.
     *
     * @param columnMetadata
     * @param parameter
     */
    private static boolean isColumnToDelete(ColumnMetadata columnMetadata, String parameter) {
        // test if all cells are empty or blanck
        if (columnMetadata.getQuality().getEmpty() != columnMetadata.getStatistics().getCount()) {
            return false;
        }
        // here all cells are all empty or blanck
        if (DELETE.equals(parameter)) {
            return true;
        }
        // manage the KEEP option :
        // test if statistics contains more than one pattern so all cells are not empty :
        if (columnMetadata.getStatistics().getDataFrequencies().size() > 1) {
            return false;
        }
        // test if the only present pattern is the empty string
        if (columnMetadata.getStatistics().getDataFrequencies().size() == 1) {
            return columnMetadata.getStatistics().getDataFrequencies().get(0).getData().isEmpty();
        }
        return false;
    }

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_COLUMNS_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter //
                .selectParameter(locale) //
                .name(ACTION_PARAMETER) //
                .item(DELETE, DELETE) //
                .item(KEEP, KEEP) //
                .defaultValue(DELETE) //
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
        final List<ColumnMetadata> columns = actionContext.getRowMetadata().getColumns();

        // find the empty columns
        Set<String> columnsToDelete = new HashSet<>();
        for (ColumnMetadata column : columns) {
            if (isColumnToDelete(column, actionContext.getParameters().get(ACTION_PARAMETER))) {
                columnsToDelete.add(column.getId());
            }
        }

        if (columnsToDelete.isEmpty()) {
            actionContext.setActionStatus(ActionContext.ActionStatus.DONE);
            return;
        }

        // delete the empty columns on row metadata
        columnsToDelete.forEach(columnId -> {
            LOGGER.debug("DeleteColumn for columnId {}", columnId);
            actionContext.getRowMetadata().deleteColumnById(columnId);
        });
        actionContext.get(COLUMNS_TO_DELETE, p -> columnsToDelete);
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext actionContext) {
        // delete related values
        final Set<String> columnsToDelete = actionContext.get(COLUMNS_TO_DELETE);
        for (String columnId : columnsToDelete) {
            row.deleteColumnById(columnId);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_DELETE_COLUMNS, Behavior.NEED_STATISTICS_QUALITY, //
                Behavior.NEED_STATISTICS_FREQUENCY);
    }
}
