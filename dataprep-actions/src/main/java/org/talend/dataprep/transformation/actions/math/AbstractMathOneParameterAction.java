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
package org.talend.dataprep.transformation.actions.math;

import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract Action for basic math action with one parameter (constant or an other column)
 */
public abstract class AbstractMathOneParameterAction extends AbstractMathAction implements ColumnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMathOneParameterAction.class);

    private static final String DEFAULT_VALUE_NAN = Integer.toString(Integer.MAX_VALUE);

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter.selectParameter(locale) //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, CONSTANT_MODE, Parameter.parameter(locale).setName(CONSTANT_VALUE)
                        .setType(ParameterType.STRING)
                        .setDefaultValue(StringUtils.EMPTY)
                        .build(this)) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, Parameter.parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                        .setType(ParameterType.COLUMN)
                        .setDefaultValue(StringUtils.EMPTY)
                        .setCanBeBlank(false)
                        .build(this)) //
                .defaultValue(CONSTANT_MODE) //
                .build(this));

        return parameters;
    }

    protected abstract String calculateResult(String columnValue, String parameter);

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        Map<String, String> parameters = context.getParameters();

        String mode = parameters.get(OtherColumnParameters.MODE_PARAMETER);

        String parameterValue;
        switch (mode) {
        case OtherColumnParameters.CONSTANT_MODE:
            parameterValue = parameters.get(CONSTANT_VALUE);
            break;
        case OtherColumnParameters.OTHER_COLUMN_MODE:
            String otherColId = parameters.get(SELECTED_COLUMN_PARAMETER);
            parameterValue = row.get(otherColId);
            break;
        default:
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER, //
                    ExceptionContext.build().put("paramName", OtherColumnParameters.CONSTANT_MODE));
        }

        String result = ERROR_RESULT;

        try {
            if (NumberUtils.isNumber(colValue) && NumberUtils.isNumber(parameterValue)) {
                result = calculateResult(colValue, parameterValue);
            } else {
                if (!NumberUtils.isNumber(parameterValue)) {
                    parameterValue = ExtractNumber.extractNumber(parameterValue, DEFAULT_VALUE_NAN);
                }
                if (!NumberUtils.isNumber(colValue)) {
                    colValue = ExtractNumber.extractNumber(colValue, DEFAULT_VALUE_NAN);
                }
                if (!StringUtils.equals(DEFAULT_VALUE_NAN, parameterValue) //
                        && !StringUtils.equals(DEFAULT_VALUE_NAN, colValue)) {
                    result = calculateResult(colValue, parameterValue);
                }
            }
        } catch (Exception e) {
            // ignore
            LOGGER.debug(e.getMessage(), e);
        }

        row.set(ActionsUtils.getTargetColumnId(context), result);
    }
}
