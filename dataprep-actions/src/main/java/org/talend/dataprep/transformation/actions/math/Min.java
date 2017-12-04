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

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.math3.util.FastMath.min;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.math.Min.MIN_NAME;

import java.util.List;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Calculate Min with a constant or an other column
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + MIN_NAME)
public class Min extends AbstractMathOneParameterAction {

    protected static final String MIN_NAME = "min_numbers";

    @Override
    public String getName() {
        return MIN_NAME;
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(new AdditionalColumn(Type.DOUBLE, context.getColumnName() + "_min"));
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        String min = Double.toString(toBigDecimal(columnValue).doubleValue());

        if (isNotBlank(parameter)) {
            min = Double.toString(min(toBigDecimal(columnValue).doubleValue(), //
                    toBigDecimal(parameter).doubleValue()));
        }
        return min;
    }
}
