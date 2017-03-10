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

package org.talend.dataprep.transformation.actions.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.ROW_ID;

import java.util.*;

import org.slf4j.Logger;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.CellAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Replace the content or part of a cell by a value.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ReplaceCellValue.REPLACE_CELL_VALUE_ACTION_NAME)
public class ReplaceCellValue extends AbstractActionMetadata implements CellAction {

    /** For the Serializable interface. */
    private static final long serialVersionUID = 1L;

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(ReplaceCellValue.class);

    /** The action name. */
    static final String REPLACE_CELL_VALUE_ACTION_NAME = "replace_cell_value";

    /** Original value parameter. */
    static final String ORIGINAL_VALUE_PARAMETER = "original_value";

    /** New value parameter name. */
    static final String NEW_VALUE_PARAMETER = "new_value";

    /** Target row ID. */
    private static final String TARGET_ROW_ID_KEY = "targetRowId";

    @Override
    public String getName() {
        return REPLACE_CELL_VALUE_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(ORIGINAL_VALUE_PARAMETER, STRING, EMPTY));
        parameters.add(new Parameter(NEW_VALUE_PARAMETER, STRING, EMPTY));
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {

            final Map<String, String> parameters = actionContext.getParameters();
            // get the target row ID
            try {
                final String targetIdAsString = parameters.get(ROW_ID.getKey());
                if (targetIdAsString == null) {
                    throw new NullPointerException("row ID is null");
                }
                final Long targetRowId = Long.valueOf(targetIdAsString);
                actionContext.get(TARGET_ROW_ID_KEY, p -> targetRowId);
            } catch (NullPointerException | NumberFormatException nfe) {
                LOGGER.info("no row ID specified in parameters {}, action canceled", parameters);
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }

            // make sure the replacement value is set
            if (!actionContext.getParameters().containsKey(NEW_VALUE_PARAMETER)) {
                LOGGER.info("no replacement value specified in parameters {}, action canceled", parameters);
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }

        }

    }

    /**
     * @see CellAction#applyOnCell(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {

        if (!Objects.equals(context.get(TARGET_ROW_ID_KEY), row.getTdpId())) {
            return;
        }

        final String replacement = context.getParameters().get(NEW_VALUE_PARAMETER);
        final String columnId = context.getColumnId();
        final String oldValue = row.get(columnId);
        row.set(columnId, replacement);
        LOGGER.info("{} replaced by {} in row {}, column {}", oldValue, replacement, row.getTdpId(), columnId);

        // all done
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.FORBID_DISTRIBUTED);
    }

}
