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

import static org.talend.dataprep.transformation.api.action.metadata.math.Max.MAX_NAME;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Calculate Max with a constant or an other column 
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + MAX_NAME)
public class Max extends AbstractMathOneParameterAction {

    protected static final String MAX_NAME = "max_numbers";

    @Override
    public String getName() {
        return MAX_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    protected String getColumnNameSuffix() {
        return "max";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        String max = Double.toString(NumberUtils.toDouble(columnValue, 0));

        if (StringUtils.isNotBlank(parameter)) {
            max = Double.toString(FastMath.max(NumberUtils.toDouble(columnValue, Double.MIN_VALUE), //
                    NumberUtils.toDouble(parameter, Double.MIN_VALUE)));
        }
        return max;
    }
}
