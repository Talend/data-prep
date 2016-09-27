package org.talend.dataprep.transformation.pipeline.node;

import java.util.function.Function;
import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class FilteredNode extends BasicNode {

    private final Function<RowMetadata, Predicate<DataSetRow>> filter;

    private Predicate<DataSetRow> instance;

    private boolean hasMatchedFilter = false;

    private RowMetadata lastRowMetadata;

    public FilteredNode(Function<RowMetadata, Predicate<DataSetRow>> filter) {
        this.filter = filter;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        synchronized (filter) {
            if (instance == null) {
                instance = filter.apply(metadata);
            }
        }
        if (instance.test(row)) {
            hasMatchedFilter = true;
            super.receive(row, metadata);
        } else {
            lastRowMetadata = metadata;
        }
    }
}
