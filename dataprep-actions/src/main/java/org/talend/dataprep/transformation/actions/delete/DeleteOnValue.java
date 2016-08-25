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

package org.talend.dataprep.transformation.actions.delete;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.NUMERIC;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.ParameterType.REGEX;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Delete row on a given value.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DeleteOnValue.DELETE_ON_VALUE_ACTION_NAME)
public class DeleteOnValue extends AbstractDelete {

    /**
     * The action name.
     */
    public static final String DELETE_ON_VALUE_ACTION_NAME = "delete_on_value"; //$NON-NLS-1$

    /**
     * Name of the parameter needed.
     */
    public static final String VALUE_PARAMETER = "value"; //$NON-NLS-1$

    @Override
    public String getName() {
        return DELETE_ON_VALUE_ACTION_NAME;
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(VALUE_PARAMETER, REGEX, EMPTY));
        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType())) || NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final Map<String, String> parameters = actionContext.getParameters();
            final ReplaceOnValueHelper regexParametersHelper = new ReplaceOnValueHelper();
            actionContext.get("replaceOnValue", p -> regexParametersHelper.build(parameters.get(VALUE_PARAMETER), true));
        }
    }

    /**
     * @see AbstractDelete#toDelete(DataSetRow, String, ActionContext)
     */
    @Override
    public boolean toDelete(DataSetRow dataSetRow, String columnId, ActionContext context) {
        try {
            final ReplaceOnValueHelper helper = context.get("replaceOnValue");
            return helper.matches(dataSetRow.get(columnId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
