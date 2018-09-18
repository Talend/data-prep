// ============================================================================
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

package org.talend.dataprep.transformation.actions.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public class ApplyDataSetRowAction implements DataSetRowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyDataSetRowAction.class);

    private final ActionDefinition metadata;

    private final Map<String, String> parameters;

    private final ScopeCategory scope;

    public ApplyDataSetRowAction(ActionDefinition metadata, Map<String, String> parameters, ScopeCategory scope) {
        this.metadata = metadata;
        this.parameters = parameters;
        this.scope = scope;
    }

    @Override
    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
        return handleRow(metadata, parameters, scope, dataSetRow, context);
    }

    private DataSetRow handleRow(final ActionDefinition metadata, //
            final Map<String, String> parameters, //
            final ScopeCategory scope, //
            final DataSetRow row, //
            final ActionContext context) {
        try {
            final DataSetRow actionRow;
            final boolean implicitFilter = metadata.implicitFilter();
            if (implicitFilter && !context.getFilter().test(row)) {
                // Return non-modifiable row since it didn't pass the filter (but metadata might be modified).
                actionRow = row.unmodifiable();
            } else {
                actionRow = row;
            }
            // Select the correct method to call depending on scope.
            switch (scope) {
            case CELL:
                ((CellAction) metadata).applyOnCell(actionRow, context);
                break;
            case LINE:
                ((RowAction) metadata).applyOnLine(actionRow, context);
                break;
            case COLUMN:
                ((ColumnAction) metadata).applyOnColumn(actionRow, context);
                break;
            case DATASET:
                ((DataSetAction) metadata).applyOnDataSet(actionRow, context);
                break;
            default:
                LOGGER.warn("Is there a new action scope ??? {}", scope);
                break;
            }
            // For following actions, returns the row as modifiable to allow further modifications.
            return actionRow.modifiable();
        } catch (Throwable e) {
            LOGGER.error("Unable to use action '{}' (parameters: {}) due to unexpected error.", metadata.getName(),
                    parameters, e);
            context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            return row.modifiable();
        }
    }
}
