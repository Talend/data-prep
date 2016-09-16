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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;

import static org.talend.dataprep.transformation.actions.math.Max.MAX_NAME;

/**
 * Calculate Max with a constant or an other column
 */
@Component(AbstractActionMetadata.ACTION_BEAN_PREFIX + MAX_NAME)
public class Max extends AbstractMathOneParameterAction {

    protected static final String MAX_NAME = "max_numbers";

    @Override
    public String getName() {
        return MAX_NAME;
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "max";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        String max = Double.toString(BigDecimalParser.toBigDecimal(columnValue).doubleValue());

        if (StringUtils.isNotBlank(parameter)) {
            max = Double.toString(FastMath.max(BigDecimalParser.toBigDecimal(columnValue).doubleValue(), //
                    BigDecimalParser.toBigDecimal(parameter).doubleValue()));
        }
        return max;
    }
}
