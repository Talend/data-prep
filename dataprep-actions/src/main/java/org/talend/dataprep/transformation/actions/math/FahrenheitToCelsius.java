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

/**
 * Given a column, assumed to be a temperature in Fahrenheit, creates a new one with temperature convertedin Celsius.
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + FahrenheitToCelsius.ACTION_NAME)
public class FahrenheitToCelsius extends AbstractFahrenheitCelsiusConversion {

    protected static final String ACTION_NAME = "fahrenheit_to_celsius";

    @Override
    protected String calculateResult(String columnValue) {
        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);

        BigDecimal result = value.subtract(_32).divide(_1_8, 3, DEFAULT_ROUNDING_MODE);

        return String.valueOf(result);
    }

    @Override
    protected String getColumnNameSuffix() {
        return "in Celsius";
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

}
