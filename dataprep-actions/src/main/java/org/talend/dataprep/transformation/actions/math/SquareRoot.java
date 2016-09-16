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

import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import static org.talend.dataprep.transformation.actions.math.SquareRoot.SQRT_NAME;

/**
 * Create a new column with square root value
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + SQRT_NAME)
public class SquareRoot extends AbstractMathNoParameterAction {

    protected static final String SQRT_NAME = "square_root_numbers";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();

        return value < 0 ? ERROR_RESULT : Double.toString(FastMath.sqrt(value));
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "square_root";
    }

    @Override
    public String getName() {
        return SQRT_NAME;
    }

}
