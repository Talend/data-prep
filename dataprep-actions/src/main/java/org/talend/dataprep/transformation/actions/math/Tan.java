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

import static org.talend.dataprep.transformation.actions.math.Tan.TAN_NAME;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Tangent
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + TAN_NAME)
public class Tan extends AbstractMathNoParameterAction {

    protected static final String TAN_NAME = "tan_numbers";

    protected static final String TAN_SUFFIX = "_tan";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();
        double result = FastMath.tan(value);
        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<AdditionalColumn> additionalColumns = new ArrayList<>();
        additionalColumns.add(new AdditionalColumn(Type.DOUBLE, context.getColumnName() + TAN_SUFFIX));
        return additionalColumns;
    }

    @Override
    public String getName() {
        return TAN_NAME;
    }

}
