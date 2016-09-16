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
package org.talend.dataprep.transformation.actions.math;

import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.math.BigDecimal;

/**
 * Given a column, assumed to be a temperature in Celsius, creates a new one with temperature convertedin Fahrenheit.
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + CelsiusToFahrenheit.ACTION_NAME)
public class CelsiusToFahrenheit extends AbstractFahrenheitCelsiusConversion {

    protected static final String ACTION_NAME = "celsius_to_fahrenheit";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);

        BigDecimal result = value.multiply(_1_8).add(_32).setScale(getPrecision(context), DEFAULT_ROUNDING_MODE);

        return String.valueOf(result);
    }

    @Override
    protected String getColumnNameSuffix() {
        return "in Fahrenheit";
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

}
