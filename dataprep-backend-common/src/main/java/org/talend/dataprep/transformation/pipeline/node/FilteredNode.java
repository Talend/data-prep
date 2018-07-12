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

import java.util.function.Function;
import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;

public class FilteredNode extends BasicNode implements Monitored {

    private final Function<RowMetadata, Predicate<DataSetRow>> filter;

    private long totalTime;

    private long count;

    private transient Predicate<DataSetRow> instance;

    private RowMetadata lastMetadata;

    private boolean hasMatched;

    private DataSetRow lastRow;

    public FilteredNode(Function<RowMetadata, Predicate<DataSetRow>> filter) {
        this.filter = filter;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            synchronized (filter) {
                if (instance == null) {
                    instance = filter.apply(metadata);
                }
            }
            hasMatched = instance.test(row);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }

        metadata.setSampleNbRows(count);

        if (hasMatched) {
            super.receive(row, metadata);
        } else {
            lastRow = row;
            lastMetadata = metadata;
        }
    }

    @Override
    public void signal(Signal signal) {
        switch (signal) {
        case END_OF_STREAM:
        case STOP:
        case CANCEL:
            if (!hasMatched && lastRow != null) {
                lastRow.setDeleted(true);
                super.receive(lastRow, lastMetadata);
            }
            break;
        }
        super.signal(signal);
    }

    @Override
    public Node copyShallow() {
        return new FilteredNode(filter);
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
