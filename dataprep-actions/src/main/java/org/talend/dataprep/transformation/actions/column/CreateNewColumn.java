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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * duplicate a column
 */
@Action(CreateNewColumn.ACTION_NAME)
public class CreateNewColumn extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "create_new_column"; //$NON-NLS-1$

    public static final String DEFAULT_VALUE_PARAMETER = "default_value"; //$NON-NLS-1$

    /**
     * Mode: tells if fill value is taken from another column or is a constant
     */
    public static final String MODE_PARAMETER = "mode_new_column"; //$NON-NLS-1$

    /**
     * The selected column id.
     */
    public static final String SELECTED_COLUMN_PARAMETER = "selected_column"; //$NON-NLS-1$

    /**
     * Constant to represents mode where we fill with a constant.
     */
    public static final String EMPTY_MODE = "empty_mode";

    public static final String CONSTANT_MODE = "a_constant_mode";

    public static final String COLUMN_MODE = "other_column_mode";

    /**
     * Name of the new column.
     */
    public static final String NEW_COLUMN_NAME = "create_new_column_name";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMN_METADATA.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<String> getActionScope() {
        return Arrays.asList(COLUMN_METADATA.getDisplayName(), HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(parameter(locale).setName(NEW_COLUMN_NAME)
                .setType(ParameterType.STRING)
                .setDefaultValue("New column")
                .setCanBeBlank(false)
                .build(this));

        Parameter constantParameter = Parameter.parameter(locale).setName(DEFAULT_VALUE_PARAMETER)
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .build(this);

        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(MODE_PARAMETER)
                        .item(EMPTY_MODE, EMPTY_MODE)
                        .item(CONSTANT_MODE, CONSTANT_MODE, constantParameter)
                        .item(COLUMN_MODE, COLUMN_MODE, parameter(locale).setName(SELECTED_COLUMN_PARAMETER).setType(COLUMN).setDefaultValue(EMPTY).setCanBeBlank(false).build(this))
                        .defaultValue(COLUMN_MODE)
                        .build(this)
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            checkParameters(context.getParameters(), context.getRowMetadata());
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final RowMetadata rowMetadata = context.getRowMetadata();
        final Map<String, String> parameters = context.getParameters();

        String newValue = "";
        switch (parameters.get(MODE_PARAMETER)) {
        case EMPTY_MODE:
            newValue = "";
            break;
        case CONSTANT_MODE:
            newValue = parameters.get(DEFAULT_VALUE_PARAMETER);
            break;
        case COLUMN_MODE:
            ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            newValue = row.get(selectedColumn.getId());
            break;
        default:
        }

        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    public List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        String columnName = context.getParameters().get(NEW_COLUMN_NAME);
        return Collections.singletonList(ActionsUtils.additionalColumn().withName(columnName));
    }

    /**
     * Check that the selected column parameter is correct in case we concatenate with another column: defined in the
     * parameters and there's a matching column. If the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param rowMetadata the row where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (!parameters.containsKey(MODE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", MODE_PARAMETER));
        }

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DEFAULT_VALUE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", DEFAULT_VALUE_PARAMETER));
        }
        if (parameters.get(MODE_PARAMETER).equals(COLUMN_MODE) && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", SELECTED_COLUMN_PARAMETER));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
