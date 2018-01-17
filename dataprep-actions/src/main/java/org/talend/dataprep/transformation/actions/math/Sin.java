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

import static org.talend.dataprep.transformation.actions.math.Sin.SIN_NAME;

import org.apache.commons.math3.util.FastMath;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Sin
 */
@Action(SIN_NAME)
public class Sin extends AbstractMathNoParameterAction {

    static final String SIN_NAME = "sin_numbers";

    private static final String SIN_SUFFIX = "_sin";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();
        double result = FastMath.sin(value);
        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    protected String getSuffix(ActionContext context) {
        return SIN_SUFFIX;
    }

    @Override
    public String getName() {
        return SIN_NAME;
    }

}
