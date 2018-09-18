// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.talend.dataprep.api.type.Type.NUMERIC;

import java.math.BigDecimal;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.delete.AbstractDelete;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * Delete row on a given value.
 */
@Action(DeleteNegativeValues.ACTION_NAME)
public class DeleteNegativeValues extends AbstractDelete {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "delete_negative_values"; //$NON-NLS-1$

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    /**
     * @see AbstractDelete#toDelete(DataSetRow, String, ActionContext)
     */
    @Override
    public boolean toDelete(DataSetRow dataSetRow, String columnId, ActionContext context) {
        final String value = dataSetRow.get(columnId);
        if (value == null) {
            return false;
        }
        if (!NumericHelper.isBigDecimal(value)) {
            return false;
        }
        BigDecimal bd = BigDecimalParser.toBigDecimal(value.trim());
        return bd.compareTo(BigDecimal.ZERO) < 0;
    }

}
