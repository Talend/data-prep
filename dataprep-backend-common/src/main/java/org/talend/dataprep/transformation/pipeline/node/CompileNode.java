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

import java.util.Collections;
import java.util.List;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class CompileNode extends BasicNode implements ApplyToColumn, Monitored {

    private final RunnableAction action;

    private final ActionContext actionContext;

    private int hashCode = 0;

    private long totalTime;

    private long count;

    public CompileNode(RunnableAction action, ActionContext actionContext) {
        this.action = action;
        this.actionContext = actionContext;
        if (actionContext != null) {
            hashCode = actionContext.getRowMetadata().hashCode();
        }
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            if (hashCode != metadata.hashCode()) { // Metadata changed, force re-compile
                actionContext.setRowMetadata(metadata.clone());
                action.getRowAction().compile(actionContext);
            }
            row.setRowMetadata(actionContext.getRowMetadata());
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
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

    @Override
    public List<String> getColumnNames() {
        return Collections.singletonList(actionContext.getParameters().get(ImplicitParameters.COLUMN_ID.getKey()));
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }
}
