package org.talend.dataprep.transformation.actions.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.column.DeleteColumn;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteColumn.class);

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
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        String columnId;
        ColumnMetadata column;
        Quality quality;
        List<ColumnMetadata> columns = row.getRowMetadata().getColumns();

        for (int i = columns.size() - 1; i >= 0; i--) {
            column = columns.get(i);
            columnId = column.getId();
            if (context.getParameters().get(ACTION_PARAMETER).equals(KEEP)) {
                if (context.getRowMetadata().getById(columnId).getStatistics().getDataFrequencies().size() > 1) {
                    continue; //parameter to keep blanks is at true and the size of the datafrequencies is higher than 1 so we don't delete this column
                }
            }
            quality = column.getQuality();
            if (quality.getValid() + quality.getInvalid() == 0) {
                LOGGER.debug("DeleteColumn for columnId {}", columnId);
                context.getRowMetadata().deleteColumnById(columnId);
                row.deleteColumnById(columnId);
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_DELETE_COLUMNS);
    }

}
