//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.talend.dataprep.transformation.api.action.metadata.math.Tan.TAN_NAME;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Create a new column with Tangent
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + TAN_NAME)
public class Tan extends AbstractMathNoParameterAction {

    protected static final String TAN_NAME = "tan_numbers";

    @Override
    protected String calculateResult(String columnValue) {
        double value = NumberUtils.toDouble(columnValue);

        double result = FastMath.tan(value);

        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    @Override
    protected String getColumnNameSuffix() {
        return "tan";
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public String getName() {
        return TAN_NAME;
    }

}
