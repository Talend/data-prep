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

package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Clear cell when value is equals.
 */
@Component(ClearEquals.ACTION_BEAN_PREFIX + ClearEquals.ACTION_NAME)
public class ClearEquals extends AbstractClear implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_equals"; //$NON-NLS-1$

    public static final String VALUE_PARAMETER = "equals_value"; //$NON-NLS-1$

    @Autowired
    private ReplaceOnValueHelper regexParametersHelper;

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        Parameter constantParameter = new Parameter(VALUE_PARAMETER, ParameterType.REGEX, //
                StringUtils.EMPTY, false, false);

        parameters.add(constantParameter);

        return parameters;
    }

    public boolean toClear(ColumnMetadata colMetadata, String value, ActionContext context) {
        Map<String, String> parameters = context.getParameters();
        String equalsValue = parameters.get(VALUE_PARAMETER);

        switch (Type.get(colMetadata.getType())) {
        case BOOLEAN:
            // for boolean we can accept True equals true
            ReplaceOnValueHelper replaceOnValueHelper = regexParametersHelper.build(equalsValue, false);
            return StringUtils.equalsIgnoreCase(value, replaceOnValueHelper.getToken());
        default:
            replaceOnValueHelper = regexParametersHelper.build(equalsValue, true);
            return replaceOnValueHelper.matches(value);
        }
    }

}
