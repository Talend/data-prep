package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class CompileNode extends BasicNode {

    private final Action action;

    private final ActionContext actionContext;

    private int hashCode = 0;

    public CompileNode(Action action, ActionContext actionContext) {
        this.action = action;
        this.actionContext = actionContext;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        boolean needCompile = actionContext.getActionStatus() == ActionContext.ActionStatus.NOT_EXECUTED;
        if (actionContext.getRowMetadata() == null || hashCode != metadata.hashCode()) {
            actionContext.setRowMetadata(metadata.clone());
            hashCode = metadata.hashCode();
            needCompile = true; // Metadata changed, force re-compile
        }
        if (needCompile) {
            action.getRowAction().compile(actionContext);
            row.setRowMetadata(actionContext.getRowMetadata());
        }
        link.exec().emit(row, actionContext.getRowMetadata());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCompile(this);
    }

    public Action getAction() {
        return action;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }
}
