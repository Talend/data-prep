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

package org.talend.dataprep.transformation.api.action;

import java.io.Serializable;
import java.util.function.BiFunction;

import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@FunctionalInterface
public interface DataSetRowAction extends BiFunction<DataSetRow, ActionContext, DataSetRow>, Serializable {

    default void compile(ActionContext actionContext) {
        // Do nothing by default
        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
    }
}
