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

package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class ActionNode extends BasicNode implements Monitored {

    private final RunnableAction action;

    private final ActionContext actionContext;

    private long totalTime;

    private int count;

    public ActionNode(RunnableAction action, ActionContext actionContext) {
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
        if (link != null) {
            link.exec().emit(actionRow, actionContext.getRowMetadata());
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAction(this);
    }

    @Override
    public Node copyShallow() {
        return new ActionNode(action, actionContext);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    public RunnableAction getAction() {
        return action;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }
}
