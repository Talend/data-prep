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

import org.apache.commons.lang.math.NumberUtils;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract Action for basic math action without parameter
 */
public abstract class AbstractMathNoParameterAction extends AbstractMathAction implements ColumnAction {

    protected abstract String calculateResult(String columnValue, ActionContext context);

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        String result = ERROR_RESULT;

        if (NumberUtils.isNumber(colValue)) {
            result = calculateResult(colValue, context);
        }

        String newColumnId = context.column("result");
        row.set(newColumnId, result);
    }

}
