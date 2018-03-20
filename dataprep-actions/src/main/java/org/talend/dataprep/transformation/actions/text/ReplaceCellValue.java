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

package org.talend.dataprep.transformation.actions.text;

import static java.lang.Long.valueOf;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.of;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.ROW_ID;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.slf4j.Logger;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.CellAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Replace the content or part of a cell by a value.
 */
@Action(ReplaceCellValue.REPLACE_CELL_VALUE_ACTION_NAME)
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

    protected static final String NEW_COLUMN_SUFFIX = "_replace";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return REPLACE_CELL_VALUE_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        parameters.add(
                parameter(locale).setName(ORIGINAL_VALUE_PARAMETER).setType(STRING).setDefaultValue(EMPTY).build(this));
        parameters.add(
                parameter(locale).setName(NEW_VALUE_PARAMETER).setType(STRING).setDefaultValue(EMPTY).build(this));
        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX));
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
        if (actionContext.getActionStatus() == OK) {

            final Map<String, String> parameters = actionContext.getParameters();
            // get the target row ID
            try {
                final String targetIdAsString = parameters.get(ROW_ID.getKey());
                if (targetIdAsString == null) {
                    throw new NullPointerException("row ID is null");
                }
                final Long targetRowId = valueOf(targetIdAsString);
                actionContext.get(TARGET_ROW_ID_KEY, p -> targetRowId);
            } catch (NullPointerException | NumberFormatException nfe) {
                LOGGER.info("no row ID specified in parameters {}, action canceled", parameters);
                actionContext.setActionStatus(CANCELED);
            }

            // make sure the replacement value is set
            if (!actionContext.getParameters().containsKey(NEW_VALUE_PARAMETER)) {
                LOGGER.info("no replacement value specified in parameters {}, action canceled", parameters);
                actionContext.setActionStatus(CANCELED);
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
        row.set(ActionsUtils.getTargetColumnId(context), replacement);
        LOGGER.debug("{} replaced by {} in row {}, column {}", oldValue, replacement, row.getTdpId(), columnId);

        // all done
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return of(Behavior.FORBID_DISTRIBUTED, Behavior.VALUES_COLUMN);
    }

}
