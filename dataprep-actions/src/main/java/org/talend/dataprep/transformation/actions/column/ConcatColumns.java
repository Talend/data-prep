package org.talend.dataprep.transformation.actions.column;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.MultiColumnsAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.STRING;

@Action(ConcatColumns.CONCAT_COLUMNS_ACTION_NAME)
public class ConcatColumns extends AbstractActionMetadata implements MultiColumnsAction {

    /**
     * The action name.
     */
    public static final String CONCAT_COLUMNS_ACTION_NAME = "concat_columns";

    private static final String NEW_COLUMN_SUFFIX = "_concatened";


    @Override
    public String getName() {
        return CONCAT_COLUMNS_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX)
                .withType(STRING).withCopyMetadataFromId(context.getColumnId()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
    }

    @Override
    public void applyOnMultiColumns(DataSetRow row, ActionContext context) {
        final String[] columnsId = extractColumnsId(context.getColumnId());
        StringBuilder result = new StringBuilder();

        for (String aColumnsId : columnsId) {
            result.append(row.get(aColumnsId));
        }

        row.set(ActionsUtils.getTargetColumnId(context), result.toString());
    }

    private String[] extractColumnsId(String columnId) {
        return columnId.split(",");
    }

}
