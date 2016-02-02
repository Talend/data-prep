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

package org.talend.dataprep.transformation;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.CellAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.DataSetAction;

/**
 * A unit test action: only use to test unexpected action failures.
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + FailedAction.FAILED_ACTION)
public class FailedAction extends ActionMetadata implements ColumnAction, CellAction, DataSetAction {

    public static final String FAILED_ACTION = "testfailedaction";

    @Override
    public String getName() {
        return FAILED_ACTION;
    }

    @Override
    public String getCategory() {
        return "TEST";
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {
        throw new RuntimeException("On purpose unchecked exception");
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        throw new RuntimeException("On purpose unchecked exception");
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        throw new RuntimeException("On purpose unchecked exception");
    }
}
