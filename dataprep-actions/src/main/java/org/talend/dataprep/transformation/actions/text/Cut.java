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
import static org.talend.dataprep.parameters.ParameterType.REGEX;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.STRINGS;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.getColumnCreationParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(Cut.CUT_ACTION_NAME)
public class Cut extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String CUT_ACTION_NAME = "cut"; //$NON-NLS-1$

    protected static final String NEW_COLUMN_SUFFIX = "_cut";

    /**
     * The pattern "where to cut" parameter name
     */
    public static final String PATTERN_PARAMETER = "pattern"; //$NON-NLS-1$

    public static final String REGEX_HELPER_KEY = "regex_helper";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return CUT_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return STRINGS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        parameters.add(parameter(locale).setName(PATTERN_PARAMETER).setType(REGEX).setDefaultValue(EMPTY).build(this));
        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX).withType(STRING));
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
        if (actionContext.getActionStatus() == OK) {
            final Map<String, String> parameters = actionContext.getParameters();
            String rawParam = parameters.get(PATTERN_PARAMETER);

            ReplaceOnValueHelper regexParametersHelper = new ReplaceOnValueHelper();
            try {
                actionContext.get(REGEX_HELPER_KEY, p -> regexParametersHelper.build(rawParam, false));
            } catch (IllegalArgumentException e) {
                actionContext.setActionStatus(CANCELED);
            }
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String toCut = row.get(columnId);
        if (toCut != null) {
            final ReplaceOnValueHelper replaceOnValueParameter = context.get(REGEX_HELPER_KEY);
            String value;
            if (replaceOnValueParameter.matches(toCut)) {
                if (replaceOnValueParameter.getOperator().equals(ReplaceOnValueHelper.REGEX_MODE)) {
                    value = toCut.replaceAll(replaceOnValueParameter.getToken(), ""); //$NON-NLS-1$
                } else {
                    value = toCut.replace(replaceOnValueParameter.getToken(), ""); //$NON-NLS-1$
                }
            } else {
                value = toCut;
            }
            row.set(ActionsUtils.getTargetColumnId(context), value);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
