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
import static org.apache.commons.math3.util.FastMath.max;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.math.Max.MAX_NAME;

import java.util.List;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Calculate Max with a constant or an other column
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + MAX_NAME)
public class Max extends AbstractMathOneParameterAction {

    protected static final String MAX_NAME = "max_numbers";

    @Override
    public String getName() {
        return MAX_NAME;
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(new AdditionalColumn(Type.DOUBLE, context.getColumnName() + "_max"));
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
