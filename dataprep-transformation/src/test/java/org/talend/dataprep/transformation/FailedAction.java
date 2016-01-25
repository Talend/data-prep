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
