package org.talend.dataprep.transformation.actions.math;

import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.talend.dataprep.parameters.ParameterType.INTEGER;

/**
 * Abstract class for conversions from Fahrenheit to Celsius and vice versa.
 */
public abstract class AbstractFahrenheitCelsiusConversion extends  AbstractMathNoParameterAction {

    protected static final BigDecimal _32 = new BigDecimal(32);

    protected static final BigDecimal _1_8 = new BigDecimal(1.8);

    protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_DOWN;

    /** Number of digit after the decimal symbol. */
    protected static final String PRECISION = "precision"; //$NON-NLS-1$

    protected int getPrecision(ActionContext context){
        final String precisionAsString = context.getParameters().get(PRECISION);

        int precision = 2;

        try {
            precision = Integer.parseInt(precisionAsString);
        } catch (Exception e) {
            // Nothing to do, precision cannot be parsed to integer, in this case we keep 0
        }

        if (precision < 0) {
            precision = 0;
        }

        return precision;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(PRECISION, INTEGER, "2"));
        return parameters;
    }

    @Override
    public String getCategory() {
        return "Conversions";
    }

}
