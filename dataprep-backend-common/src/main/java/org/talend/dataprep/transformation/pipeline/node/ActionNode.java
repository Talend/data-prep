package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class ActionNode extends BasicNode implements Monitored {

    private final Action action;

    private final ActionContext actionContext;

    private long totalTime;

    private int count;

    public ActionNode(Action action, ActionContext actionContext) {
        this.action = action;
        this.actionContext = actionContext;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final DataSetRow actionRow;
        final long start = System.currentTimeMillis();
        try {
            switch (actionContext.getActionStatus()) {
            case NOT_EXECUTED:
            case OK:
                actionRow = action.getRowAction().apply(row, actionContext);
                break;
            case DONE:
            case CANCELED:
            default:
                actionRow = row;
                break;
            }
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
        row.setRowMetadata(actionContext.getRowMetadata());
        link.exec().emit(actionRow, actionContext.getRowMetadata());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAction(this);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    public Action getAction() {
        return action;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }
}
