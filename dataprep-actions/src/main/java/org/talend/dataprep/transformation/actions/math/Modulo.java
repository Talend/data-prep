package org.talend.dataprep.transformation.actions.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.transformation.actions.math.Modulo.MODULO_NAME;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + MODULO_NAME)
public class Modulo extends AbstractMathAction {

    protected static final String MODULO_NAME = "modulo_numbers";

    /** Number of digit after the decimal symbol. */
    protected static final String PRECISION = "precision";

    /** Number of the divisor. */
    protected static final String DIVISOR = "divisor";

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Modulo.class);

    /** The default precision. */
    private static final int DEFAULT_PRECISION = 0;

    protected static final RoundingMode ROUND = RoundingMode.HALF_EVEN;

    @Override
    public String getName() {
        return MODULO_NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(PRECISION, INTEGER, "0"));
        parameters.add(new Parameter(DIVISOR, INTEGER, "2"));
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "modulo";
    }

    /**
     *
     * @param parameters for get the precision parameter.
     * @return the precision
     */
    protected int getPrecision(Map<String, String> parameters) {

        int precision = DEFAULT_PRECISION;

        try {
            precision = Integer.parseInt(parameters.get(PRECISION));
        } catch (NumberFormatException e) {
            LOGGER.trace("miss parameter: "+PRECISION+" is empty. Default value: "+DEFAULT_PRECISION+".");
        }

        if (precision < DEFAULT_PRECISION) {
            precision = DEFAULT_PRECISION;
        }

        return precision;
    }


    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);

            actionContext.get(PRECISION, p -> getPrecision(actionContext.getParameters()));

            if (actionContext.getParameters().get(DIVISOR).isEmpty()) {
                LOGGER.warn("miss parameter: "+DIVISOR+" is empty.");
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {

        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        final String divisor = context.getParameters().get(DIVISOR);
        final int precision = context.get(PRECISION);

        if (NumericHelper.isBigDecimal(value)) {
            row.set(columnId,
                    BigDecimalParser
                            .toBigDecimal(value)
                            .remainder(BigDecimalParser.toBigDecimal(divisor))
                            .setScale(precision, ROUND)
                            .abs()
                            .toString());
        }
    }
}
