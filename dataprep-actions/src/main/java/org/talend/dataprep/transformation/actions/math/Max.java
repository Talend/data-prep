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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.math3.util.FastMath.max;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.math.Max.MAX_NAME;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Calculate Max with a constant or an other column
 */
@Action(MAX_NAME)
public class Max extends AbstractMathOneParameterAction {

    protected static final String MAX_NAME = "max_numbers";

    @Override
    public String getName() {
        return MAX_NAME;
    }

    @Override
    protected String getSuffix(ActionContext context) {
        return "_max";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        String max = Double.toString(toBigDecimal(columnValue).doubleValue());

        if (isNotBlank(parameter)) {
            max = Double.toString(max(toBigDecimal(columnValue).doubleValue(), //
                    toBigDecimal(parameter).doubleValue()));
        }
        return max;
    }
}
