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

package org.talend.dataprep.transformation.actions.clear;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.REGEX;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Clear cell when value is matching.
 */

@Action(ClearMatching.ACTION_NAME)
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
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        if (this.type == BOOLEAN) {
            parameters.add(selectParameter(locale) //
                    .name(VALUE_PARAMETER) //
                    .item(TRUE.toString()) //
                    .item(FALSE.toString()) //
                    .build(this));
        } else {
            parameters.add(parameter(locale).setName(VALUE_PARAMETER)
                    .setType(REGEX)
                    .setDefaultValue(EMPTY)
                    .setCanBeBlank(false)
                    .build(this));
        }

        return parameters;
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
