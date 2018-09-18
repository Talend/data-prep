// ============================================================================
//
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * Abstract Action for basic math action without parameter
 */
public abstract class AbstractMathNoParameterAction extends AbstractMathAction implements ColumnAction {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NumericOperations.class);

    protected abstract String calculateResult(String columnValue, ActionContext context);

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        String result = ERROR_RESULT;

        if (NumericHelper.isBigDecimal(colValue)) {
            try {
                result = calculateResult(colValue, context);
            } catch (ArithmeticException | NumberFormatException | NullPointerException e) {
                LOGGER.debug("Unable to calculate action on {} due to the following exception {}.", colValue, e);
            } catch (Exception e) {
                LOGGER.debug("Unable to calculate action on {} due to an unknown exception {}.", colValue, e);
            }
        }

        row.set(ActionsUtils.getTargetColumnId(context), result);
    }

}
