package org.talend.dataprep.transformation.actions.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Abstract class for conversions from Fahrenheit to Celsius and vice versa.
 */
public abstract class AbstractFahrenheitCelsiusConversion extends  AbstractMathNoParameterAction {

    protected static final BigDecimal _32 = new BigDecimal(32);

    protected static final BigDecimal _1_8 = new BigDecimal(1.8);

    protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_DOWN;

    @Override
    public String getCategory() {
        return "Conversions";
    }

}
