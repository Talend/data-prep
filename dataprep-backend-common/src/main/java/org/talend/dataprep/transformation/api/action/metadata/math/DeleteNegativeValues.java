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

import static org.talend.dataprep.api.type.Type.NUMERIC;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.delete.AbstractDelete;

/**
 * Delete row on a given value.
 */
@Component(DeleteNegativeValues.ACTION_BEAN_PREFIX + DeleteNegativeValues.ACTION_NAME)
public class DeleteNegativeValues extends AbstractDelete {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "delete_negative_values"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    /**
     * @see AbstractDelete#toDelete(ColumnMetadata, Map, String)
     */
    @Override
    public boolean toDelete(ColumnMetadata colMetadata, Map<String, String> parsedParameters, String value) {
        if (value == null) {
            return false;
        }
        try {
            BigDecimal bd = BigDecimalParser.toBigDecimal(value.trim());
            return bd.compareTo(BigDecimal.ZERO) < 0;
        }
        catch (NumberFormatException exc){
            return false;
        }
    }

}
