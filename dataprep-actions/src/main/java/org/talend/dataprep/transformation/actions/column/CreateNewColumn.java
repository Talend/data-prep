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
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractGenerateSequenceAction;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * duplicate a column
 */
@Action(CreateNewColumn.ACTION_NAME)
public class CreateNewColumn extends AbstractGenerateSequenceAction {

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

    public static final String SEQUENCE_MODE = "sequence_mode";

    public static final String PARAM_NAME = "paramName";

    /**
     * Name of the new column.
     */
    public static final String NEW_COLUMN_NAME = "create_new_column_name";

    public static final String DEFAULT_NAME_FOR_NEW_COLUMN = "new column";

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

        parameters.add(parameter(locale)
                .setName(NEW_COLUMN_NAME)
                .setType(ParameterType.STRING)
                .setDefaultValue(DEFAULT_NAME_FOR_NEW_COLUMN)
                .setCanBeBlank(false)
                .build(this));

        Parameter constantParameter = Parameter
                .parameter(locale)
                .setName(DEFAULT_VALUE_PARAMETER)
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .build(this);

        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(MODE_PARAMETER)
                        .item(EMPTY_MODE, EMPTY_MODE)
                        .item(CONSTANT_MODE, CONSTANT_MODE, constantParameter)
                        .item(COLUMN_MODE, COLUMN_MODE, parameter(locale).setName(SELECTED_COLUMN_PARAMETER).setType(COLUMN).setDefaultValue(EMPTY).setCanBeBlank(false).build(this))
                        .item(SEQUENCE_MODE, SEQUENCE_MODE,
                                parameter(locale).setName(START_VALUE)
                                        .setType(INTEGER)
                                        .setDefaultValue("1")
                                        .setCanBeBlank(false)
                                        .build(this),
                                parameter(locale).setName(STEP_VALUE)
                                        .setType(INTEGER)
                                        .setDefaultValue("1")
                                        .setCanBeBlank(false)
                                        .build(this))
                        .defaultValue(COLUMN_MODE)
                        .build(this)
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), true)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
        Map<String, String> parameters = actionContext.getParameters();
        if (actionContext.getActionStatus() == OK) {
            checkParameters(actionContext.getParameters(), actionContext.getRowMetadata());
            if (parameters.get(MODE_PARAMETER).equals(SEQUENCE_MODE)) {
                actionContext.get(SEQUENCE, values -> new CalcSequence(parameters));
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final RowMetadata rowMetadata = context.getRowMetadata();
        final Map<String, String> parameters = context.getParameters();

        String newValue = EMPTY;
        switch (parameters.get(MODE_PARAMETER)) {
        case EMPTY_MODE:
            newValue = EMPTY;
            break;
        case CONSTANT_MODE:
            newValue = parameters.get(DEFAULT_VALUE_PARAMETER);
            break;
        case COLUMN_MODE:
            ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            newValue = row.get(selectedColumn.getId());
            break;
        case SEQUENCE_MODE:
            if (row.isDeleted()) {
                return;
            }
            final CalcSequence sequence = context.get(SEQUENCE);
            newValue = sequence.getNextValue();
            break;
        default:
        }

        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    public List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        String columnName = context.getParameters().get(NEW_COLUMN_NAME) != null
                ? context.getParameters().get(NEW_COLUMN_NAME) : DEFAULT_NAME_FOR_NEW_COLUMN;
        ActionsUtils.AdditionalColumn additionalColumn = ActionsUtils.additionalColumn().withName(columnName);
        if (SEQUENCE_MODE.equals(context.getParameters().get(MODE_PARAMETER))) {
            additionalColumn.withType(Type.INTEGER);
        }
        return Collections.singletonList(additionalColumn);
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
                    ExceptionContext.build().put(PARAM_NAME, MODE_PARAMETER));
        }

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DEFAULT_VALUE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put(PARAM_NAME, DEFAULT_VALUE_PARAMETER));
        }
        if (parameters.get(MODE_PARAMETER).equals(COLUMN_MODE) && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put(PARAM_NAME, SELECTED_COLUMN_PARAMETER));
        }
        if (parameters.get(MODE_PARAMETER).equals(SEQUENCE_MODE)
                && (!parameters.containsKey(START_VALUE) || !parameters.containsKey(STEP_VALUE))) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName1", START_VALUE).put("paramName2", STEP_VALUE));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    @Override
    public Set<Behavior> getBehavior(org.talend.dataprep.api.preparation.Action action) {
        final Set<Behavior> behaviors = new HashSet<>(getBehavior());
        if (SEQUENCE_MODE.equals(action.getParameters().get(MODE_PARAMETER))) {
            behaviors.add(Behavior.FORBID_DISTRIBUTED);
        }
        return behaviors;
    }

}
