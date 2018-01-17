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

package org.talend.dataprep.transformation.actions.common;

import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

class DataSetRowActionImpl implements DataSetRowAction {

    private final DataSetRowAction rowAction;

    private final DataSetRowAction compile;

    DataSetRowActionImpl(DataSetRowAction rowAction, DataSetRowAction compile) {
        this.rowAction = rowAction;
        this.compile = compile;
    }

    @Override
    public DataSetRow apply(DataSetRow dataSetRow, ActionContext actionContext) {
        return rowAction.apply(dataSetRow, actionContext);
    }

    @Override
    public void compile(ActionContext actionContext) {
        compile.compile(actionContext);
    }
}
