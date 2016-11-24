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

package org.talend.dataprep.transformation.actions.clear;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Clear cell when value is matching.
 */

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ClearMatching.ACTION_NAME)
public class ClearMatching extends AbstractClear implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_matching"; //$NON-NLS-1$

    public static final String VALUE_PARAMETER = "matching_value"; //$NON-NLS-1$

    private final Type type;

    public ClearMatching() {
        this(Type.STRING);
    }

    public ClearMatching(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        if (this.type == Type.BOOLEAN) {
            parameters.add(SelectParameter.Builder.builder() //
                    .name(VALUE_PARAMETER) //
                    .item(Boolean.TRUE.toString()) //
                    .item(Boolean.FALSE.toString()) //
                    .build());
        } else {
            parameters.add(new Parameter(VALUE_PARAMETER, ParameterType.REGEX, //
                    StringUtils.EMPTY, false, false, StringUtils.EMPTY));
        }

        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null || !acceptField(column)) {
            return this;
        }
        return new ClearMatching(Type.valueOf(column.getType().toUpperCase()));
    }

    @Override
    public boolean toClear(DataSetRow dataSetRow, String columnId, ActionContext actionContext) {
        final Map<String, String> parameters = actionContext.getParameters();
        final RowMetadata rowMetadata = actionContext.getRowMetadata();
        final ColumnMetadata columnMetadata = rowMetadata.getById(columnId);
        final String value = dataSetRow.get(columnId);
        final String equalsValue = parameters.get(VALUE_PARAMETER);

        if (Type.get(columnMetadata.getType()) == Type.BOOLEAN) { // for boolean we can accept True equalsIgnoreCase true
            return StringUtils.equalsIgnoreCase(value, equalsValue);
        } else {
            ReplaceOnValueHelper replaceOnValueHelper = new ReplaceOnValueHelper().build(equalsValue, true);
            return replaceOnValueHelper.matches(value);
        }
    }

}
