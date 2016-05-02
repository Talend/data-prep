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

import static org.talend.dataprep.transformation.api.action.metadata.math.Negate.NEGATE_NAME;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Create a new column with negate value
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + NEGATE_NAME)
public class Negate extends AbstractMathNoParameterAction {

    protected static final String NEGATE_NAME = "negate_numbers";

    @Override
    protected String calculateResult(String columnValue) {
        return Double.toString( - NumberUtils.toDouble( columnValue ));
    }

    @Override
    protected String getColumnNameSuffix() {
        return "negate";
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public String getName() {
        return NEGATE_NAME;
    }


}
