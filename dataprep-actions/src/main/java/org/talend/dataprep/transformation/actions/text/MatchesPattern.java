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
import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.REGEX;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import javax.annotation.Nonnull;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(MatchesPattern.MATCHES_PATTERN_ACTION_NAME)
public class MatchesPattern extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String MATCHES_PATTERN_ACTION_NAME = "matches_pattern"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_matching"; //$NON-NLS-1$

    /**
     * The pattern shown to the user as a list. An item in this list is the value 'other', which allow the user to
     * manually enter his pattern.
     */
    public static final String PATTERN_PARAMETER = "proposed_pattern"; //$NON-NLS-1$

    public static final String CUSTOM = "custom";

    public static final String REGEX_HELPER_KEY = "regex_helper";

    /**
     * The pattern manually specified by the user. Should be used only if PATTERN_PARAMETER value is 'other'.
     */
    protected static final String MANUAL_PATTERN_PARAMETER = "manual_pattern"; //$NON-NLS-1$

    @Override
    public String getName() {
        return MATCHES_PATTERN_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        // @formatter:off
		parameters.add(selectParameter(locale)
				.name(PATTERN_PARAMETER)
				.item("[a-z]+", "[a-z]+")
				.item("[A-Z]+", "[A-Z]+")
				.item("[0-9]+", "[0-9]+")
				.item("[a-zA-Z]+", "[a-zA-Z]+")
				.item("[a-zA-Z0-9]+", "[a-zA-Z0-9]+")
				.item(CUSTOM, CUSTOM, parameter(locale).setName(MANUAL_PATTERN_PARAMETER).setType(REGEX).setDefaultValue(EMPTY).build(this))
				.defaultValue("[a-zA-Z]+")
				.build(this ));
		// @formatter:on
        return parameters;
    }

    /**
     * @param parameters the action parameters.
     * @return the pattern to use according to the given parameters.
     */
    private ReplaceOnValueHelper getPattern(Map<String, String> parameters) {
        if (CUSTOM.equals(parameters.get(PATTERN_PARAMETER))) {
            final String jsonString = parameters.get(MANUAL_PATTERN_PARAMETER);
            final ReplaceOnValueHelper regexParametersHelper = new ReplaceOnValueHelper();
            return regexParametersHelper.build(jsonString, true);
        } else {
            return new ReplaceOnValueHelper(parameters.get(PATTERN_PARAMETER), ReplaceOnValueHelper.REGEX_MODE);
        }
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            try {
                context.get(REGEX_HELPER_KEY, p -> getPattern(context.getParameters()));
            } catch (IllegalArgumentException e) {
                context.setActionStatus(CANCELED);
            }
        }
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(
                ActionsUtils.additionalColumn().withName(context.getColumnName() + APPENDIX).withType(Type.BOOLEAN));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        final String newValue = toStringTrueFalse(computeNewValue(value, context));
        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    /**
     * Computes if a given string matches or not given pattern.
     *
     * @param value the value to test
     * @param actionContext context expected to contain the compiled pattern to match the value against.
     * @return true if 'value' matches 'pattern', false if not or if 'pattern' is not a valid pattern or is null or
     * empty
     */
    protected boolean computeNewValue(String value, ActionContext actionContext) {
        // There are direct calls to this method from unit tests, normally such checks are done during transformation.
        if (actionContext.getActionStatus() != ActionContext.ActionStatus.OK) {
            return false;
        }
        final ReplaceOnValueHelper replaceOnValueParameter = actionContext.get(REGEX_HELPER_KEY);

        return replaceOnValueParameter.matches(value);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
