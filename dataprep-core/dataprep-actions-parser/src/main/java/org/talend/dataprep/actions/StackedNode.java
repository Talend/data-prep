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

package org.talend.dataprep.actions;

import java.util.ArrayDeque;
import java.util.Deque;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

class StackedNode extends BasicNode implements Monitored {

    /** For the serialization interface. */
    private static final long serialVersionUID = 1L;

    private transient Deque<DataSetRow> stack;

    private long totalTime;

    private long count;

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            if (!row.isDeleted()) {
                getStack().push(row);
            }
            super.receive(row, metadata);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    private Deque<DataSetRow> getStack() {
        if (stack == null) {
            stack = new ArrayDeque<>();
        }
        return stack;
    }

    DataSetRow pop() {
        final Deque<DataSetRow> dataSetRows = getStack();
        return dataSetRows.isEmpty() ? null : dataSetRows.pop();
    }

    @Override
    public Node copyShallow() {
        return new StackedNode();
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
