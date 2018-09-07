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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class CompileNode extends BasicNode implements ApplyToColumn, Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompileNode.class);

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
            if (hashCode != metadata.hashCode()
                    || actionContext.getActionStatus() == ActionContext.ActionStatus.NOT_EXECUTED) { // Metadata changed, force re-compile
                actionContext.setRowMetadata(metadata.clone());
                action.getRowAction().compile(actionContext);
                hashCode = actionContext.getRowMetadata().hashCode();
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
        final List<String> columnNames = new ArrayList<>();
        final String columnId = actionContext.getParameters().get(ImplicitParameters.COLUMN_ID.getKey());
        if (columnId != null) {
            columnNames.add(columnId);
        }
        for (ColumnMetadata column : actionContext.getRowMetadata().getColumns()) {
            final String diffFlagValue = column.getDiffFlagValue();
            if (Flag.DELETE.getValue().equals(diffFlagValue)) {
                columnNames.add(column.getId());
            } else if (Flag.NEW.getValue().equals(diffFlagValue)) {
                columnNames.add(column.getId());
            }
        }
        return columnNames;
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
