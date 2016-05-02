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

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Abstract Action for basic math action without parameter
 */
public abstract class AbstractMathNoParameterAction extends AbstractMathAction implements ColumnAction {

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();
        return parameters;
    }

    protected abstract String calculateResult(String columnValue);

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        String result = ERROR_RESULT;

        if (NumberUtils.isNumber(colValue)) {
            result = calculateResult(colValue);
        }

        String newColumnId = context.column("result");
        row.set(newColumnId, result);
    }
}
