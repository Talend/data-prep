// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Deletes a column from a dataset. This action is available from column headers</b>
 */
@Action(DeleteColumn.DELETE_COLUMN_ACTION_NAME)
public class DeleteColumn extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String DELETE_COLUMN_ACTION_NAME = "delete_column"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteColumn.class);

    @Override
    public String getName() {
        return DELETE_COLUMN_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(COLUMN_METADATA.getDisplayName());
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        LOGGER.debug("DeleteColumn for columnId {}", columnId);
        context.getRowMetadata().deleteColumnById(columnId);
        row.deleteColumnById(columnId);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_DELETE_COLUMNS);
    }

}
