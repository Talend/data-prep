// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.column;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.category.ActionScope;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.MultiColumnsAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.STRING;

@Action(ConcatColumns.CONCAT_COLUMNS_ACTION_NAME)
public class ConcatColumns extends AbstractActionMetadata implements MultiColumnsAction {

    /**
     * The action name.
     */
    public static final String CONCAT_COLUMNS_ACTION_NAME = "concat_columns";

    private static final String NEW_COLUMN_NAME = "concatenate_columns";

    private ScopeCategory scope;

    public ConcatColumns() {
        this(ScopeCategory.MULTI_COLUMNS);
    }

    public ConcatColumns(ScopeCategory scope) {
        this.scope = scope;
    }

    @Override
    public String getName() {
        return CONCAT_COLUMNS_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        if (ScopeCategory.MULTI_COLUMNS.equals(this.scope)) {
            return singletonList(ActionScope.MULTI_COLUMNS.getDisplayName());
        }
        return emptyList();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(NEW_COLUMN_NAME)
                .withType(STRING).withCopyMetadataFromId(getMetadataFromFirstColumn(context.getColumnId())));
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
        String result = columnId.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("\"", "");
        return result.split(",");
    }

    private String getMetadataFromFirstColumn(String columnId) {
        String[] columnsId = extractColumnsId(columnId);
        return columnsId[0];
    }

    @Override
    public ActionDefinition adapt(ScopeCategory scope) {
        return new ConcatColumns(scope);
    }

}
