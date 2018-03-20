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
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class CompileNode extends BasicNode {

    private final RunnableAction action;

    private final ActionContext actionContext;

    private int hashCode = 0;

    public CompileNode(RunnableAction action, ActionContext actionContext) {
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
        }
        row.setRowMetadata(actionContext.getRowMetadata());
        link.exec().emit(row, actionContext.getRowMetadata());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCompile(this);
    }

    @Override
    public Node copyShallow() {
        return new CompileNode(action, actionContext);
    }

    public RunnableAction getAction() {
        return action;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }
}
