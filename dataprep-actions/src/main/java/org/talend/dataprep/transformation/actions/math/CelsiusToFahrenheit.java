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

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Given a column, assumed to be a temperature in Celsius, creates a new one with temperature convertedin Fahrenheit.
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + CelsiusToFahrenheit.ACTION_NAME)
public class CelsiusToFahrenheit extends AbstractMathNoParameterAction {

    protected static final String ACTION_NAME = "celsius_to_fahrenheit";

    private static final BigDecimal _32 = new BigDecimal(32);

    private static final BigDecimal _1_8 = new BigDecimal(1.8);

    @Override
    protected String calculateResult(String columnValue) {
        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);

        BigDecimal result = value.multiply(_1_8).add(_32).setScale(2, BigDecimal.ROUND_HALF_DOWN);

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

    @Override
    public String getCategory() {
        return "Conversions";
    }

}
