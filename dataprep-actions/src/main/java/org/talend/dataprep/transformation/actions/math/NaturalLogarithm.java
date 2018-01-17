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

import static java.lang.Double.isNaN;
import static org.apache.commons.math3.util.FastMath.log;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.math.NaturalLogarithm.NATURAL_LOGARITHM_NAME;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Natural Logarithm
 */
@Action(NATURAL_LOGARITHM_NAME)
public class NaturalLogarithm extends AbstractMathNoParameterAction {

    protected static final String NATURAL_LOGARITHM_NAME = "natural_logarithm_numbers";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = toBigDecimal(columnValue).doubleValue();

        double result = log(value);

        return isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    protected String getSuffix(ActionContext context) {
        return "_natural_logarithm";
    }

    @Override
    public String getName() {
        return NATURAL_LOGARITHM_NAME;
    }

}
