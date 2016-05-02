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

import static org.talend.dataprep.transformation.api.action.metadata.math.Cos.COS_NAME;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Create a new column with Cos
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + COS_NAME)
public class Cos extends AbstractMathNoParameterAction {

    protected static final String COS_NAME = "cos_numbers";

    @Override
    protected String calculateResult(String columnValue) {
        double value = NumberUtils.toDouble(columnValue);

        double result = FastMath.cos(value);

        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    @Override
    protected String getColumnNameSuffix() {
        return "cos";
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public String getName() {
        return COS_NAME;
    }

}
