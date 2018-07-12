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
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class SourceNode extends BasicNode implements Monitored {

    private long totalTime;

    private long count;

    private RowMetadata lastRowMetadata;

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            super.receive(row, metadata);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    @Override
    public void signal(Signal signal) {
        final long start = System.currentTimeMillis();
        try {
            if (lastRowMetadata != null) {
                lastRowMetadata.setSampleNbRows(count);
            }
            super.signal(signal);
        } finally {
            totalTime += System.currentTimeMillis() - start;
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSource(this);
    }

    @Override
    public Node copyShallow() {
        SourceNode result = new SourceNode();
        result.lastRowMetadata = lastRowMetadata;
        result.count = count;
        return result;
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
