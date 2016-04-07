//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.ParameterType.*;

import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.CellAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;
import org.talend.dataprep.parameters.Parameter;

/**
 * Replace the content or part of a cell by a value.
 */
@Component(ReplaceOnValue.ACTION_BEAN_PREFIX + ReplaceOnValue.REPLACE_ON_VALUE_ACTION_NAME)
public class ReplaceOnValue extends ActionMetadata implements ColumnAction, CellAction {

    public static final String REGEX_HELPER_KEY = "regex_helper";

    @Autowired
    private ReplaceOnValueHelper regexParametersHelper;

    /** The action name. */
    public static final String REPLACE_ON_VALUE_ACTION_NAME = "replace_on_value"; //$NON-NLS-1$

    /** Value to match. */
    public static final String CELL_VALUE_PARAMETER = "cell_value"; //$NON-NLS-1$

    /** Replace Value. */
    public static final String REPLACE_VALUE_PARAMETER = "replace_value"; //$NON-NLS-1$

    /** Scope Value (replace the entire cell or only the part that matches). */
    public static final String REPLACE_ENTIRE_CELL_PARAMETER = "replace_entire_cell"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return REPLACE_ON_VALUE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(CELL_VALUE_PARAMETER, REGEX, EMPTY));
        parameters.add(new Parameter(REPLACE_VALUE_PARAMETER, STRING, EMPTY));
        parameters.add(new Parameter(REPLACE_ENTIRE_CELL_PARAMETER, BOOLEAN, "false"));
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final Map<String, String> parameters = actionContext.getParameters();
            String rawParam = parameters.get(CELL_VALUE_PARAMETER);

            try {
                actionContext.get(REGEX_HELPER_KEY,(p) -> regexParametersHelper.build(rawParam, false));
            } catch (IllegalArgumentException e) {
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
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
     * @see CellAction#applyOnCell(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {
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
        row.set(context.getColumnId(), newValue);
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
                    final Pattern pattern = replaceEntireCell ? replaceOnValueParameter.getPattern() : Pattern
                            .compile(replaceOnValueParameter.getToken());
                    try {
                        return pattern.matcher(originalValue).replaceAll(replacement);
                    }catch (IndexOutOfBoundsException e){
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
