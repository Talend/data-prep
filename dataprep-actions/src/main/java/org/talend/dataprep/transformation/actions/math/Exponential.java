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

import static org.talend.dataprep.transformation.actions.math.Exponential.EXPONENTIAL_NAME;

/**
 * Create a new column with Exponential
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + EXPONENTIAL_NAME)
public class Exponential extends AbstractMathNoParameterAction {

    protected static final String EXPONENTIAL_NAME = "exponential_numbers";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();

        return Double.toString(FastMath.exp(value));
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "exponential";
    }

    @Override
    public String getName() {
        return EXPONENTIAL_NAME;
    }

}
