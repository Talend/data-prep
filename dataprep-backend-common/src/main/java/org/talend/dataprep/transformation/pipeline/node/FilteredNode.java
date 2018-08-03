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
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;

public class FilteredNode extends BasicNode {

    private final Function<RowMetadata, Predicate<DataSetRow>> filter;

    private transient Predicate<DataSetRow> instance;

    private RowMetadata lastMetadata;

    public FilteredNode(Function<RowMetadata, Predicate<DataSetRow>> filter) {
        this.filter = filter;
    }

    private boolean hasMatched;

    private DataSetRow lastRow;

    private long totalCount = 0;

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        synchronized (filter) {
            if (instance == null) {
                instance = filter.apply(metadata);
            }
        }
        totalCount++;
        metadata.setSampleNbRows(totalCount);
        if (instance.test(row)) {
            hasMatched = true;
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
}
