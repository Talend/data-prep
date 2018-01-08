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

package org.talend.dataprep.transformation.actions.text;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;
import static org.talend.dataprep.parameters.ParameterType.REGEX;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Pattern;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Replace the content or part of a cell by a value.
 */
@Action(ReplaceOnValue.REPLACE_ON_VALUE_ACTION_NAME)
public class ReplaceOnValue extends AbstractActionMetadata implements ColumnAction {

    public static final String REGEX_HELPER_KEY = "regex_helper";

    /** The action name. */
    public static final String REPLACE_ON_VALUE_ACTION_NAME = "replace_on_value";

    /** Value to match. */
    public static final String CELL_VALUE_PARAMETER = "cell_value";

    /** Replace Value. */
    public static final String REPLACE_VALUE_PARAMETER = "replace_value";

    /** Scope Value (replace the entire cell or only the part that matches). */
    public static final String REPLACE_ENTIRE_CELL_PARAMETER = "replace_entire_cell";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return REPLACE_ON_VALUE_ACTION_NAME;
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
                parameter(locale).setName(CELL_VALUE_PARAMETER).setType(REGEX).setDefaultValue(EMPTY).build(this));
        parameters.add(parameter(locale).setName(REPLACE_VALUE_PARAMETER).setType(ParameterType.STRING).setDefaultValue(EMPTY).build(this));
        parameters.add(parameter(locale).setName(REPLACE_ENTIRE_CELL_PARAMETER)
                .setType(BOOLEAN)
                .setDefaultValue(false)
                .build(this));
        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + "_replace").withType(STRING));
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
            String rawParam = parameters.get(CELL_VALUE_PARAMETER);

            try {
                final ReplaceOnValueHelper regexParametersHelper = new ReplaceOnValueHelper();
                actionContext.get(REGEX_HELPER_KEY, p -> regexParametersHelper.build(rawParam, false));
            } catch (IllegalArgumentException e) {
                actionContext.setActionStatus(CANCELED);
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        apply(row, context);
    }

    /**
     * Apply the action.
     *
     * @param row the row where to apply the action.
     * @param context the action context.
     */
    private void apply(DataSetRow row, ActionContext context) {
        final String value = row.get(context.getColumnId());

        // defensive programming against null pointer exception
        if (value == null) {
            return;
        }

        final String newValue = computeNewValue(context, value);
        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    /**
     * Compute the new action based on the current action context.
     *
     * @param context the action context.
     * @param originalValue the original value.
     * @return the new value to set based on the parameters within the action context.
     */
    protected String computeNewValue(ActionContext context, String originalValue) {
        if (originalValue == null) {
            return null;
        }

        // There are direct calls to this method from unit tests, normally such checks are done during transformation.
        if (context.getActionStatus() != ActionContext.ActionStatus.OK) {
            return originalValue;
        }

        final Map<String, String> parameters = context.getParameters();

        String replacement = parameters.get(REPLACE_VALUE_PARAMETER);
        boolean replaceEntireCell = Boolean.valueOf(parameters.get(REPLACE_ENTIRE_CELL_PARAMETER));

        try {
            final ReplaceOnValueHelper replaceOnValueParameter = context.get(REGEX_HELPER_KEY);

            boolean matches = replaceOnValueParameter.matches(originalValue);

            if (matches) {
                if (replaceOnValueParameter.getOperator().equals(ReplaceOnValueHelper.REGEX_MODE)) {
                    final Pattern pattern = replaceEntireCell ? replaceOnValueParameter.getPattern()
                            : Pattern.compile(replaceOnValueParameter.getToken());
                    try {
                        return pattern.matcher(originalValue).replaceAll(replacement);
                    } catch (IndexOutOfBoundsException e) {
                        // catch to fix TDP_1227 PB#1
                        return originalValue;
                    }
                } else if (replaceEntireCell) {
                    return replacement;
                } else {
                    return originalValue.replace(replaceOnValueParameter.getToken(), replacement);
                }
            } else {
                return originalValue;
            }
        } catch (InvalidParameterException e) {
            return originalValue;
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
