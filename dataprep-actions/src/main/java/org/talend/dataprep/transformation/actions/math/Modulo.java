package org.talend.dataprep.transformation.actions.math;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;

import java.util.Map;

import static org.talend.dataprep.transformation.actions.math.Modulo.MODULO_NAME;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + MODULO_NAME)
public class Modulo extends AbstractMathOneParameterAction {

    protected static final String MODULO_NAME = "modulo_numbers";

    @Override
    public String getName() {
        return MODULO_NAME;
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "modulo";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        String mod = Double.toString(BigDecimalParser.toBigDecimal(columnValue).doubleValue());

        if (StringUtils.isNotBlank(parameter)) {
            mod = Double.toString();
        }
        return mod;
    }
}
