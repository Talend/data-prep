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
import static org.apache.commons.math3.util.FastMath.cos;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.math.Cos.COS_NAME;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Cos
 */
@Action(COS_NAME)
public class Cos extends AbstractMathNoParameterAction {

    protected static final String COS_NAME = "cos_numbers";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = toBigDecimal(columnValue).doubleValue();

        double result = cos(value);

        return isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    protected String getSuffix(ActionContext context) {
        return "_cos";
    }

    @Override
    public String getName() {
        return COS_NAME;
    }

}
