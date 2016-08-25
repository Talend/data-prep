package org.talend.dataprep.transformation.pipeline.node;

import java.util.function.BiPredicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * Node that filter input using a provided predicate.
 * If the predicate returns true, it is emited to the next node.
 */
public class FilterNode extends BasicNode {

    private final BiPredicate<DataSetRow, RowMetadata>[] filters;

    @SafeVarargs
    public FilterNode(final BiPredicate<DataSetRow, RowMetadata>... filters) {
        this.filters = filters;
    }

    @Override
    public void receive(final DataSetRow row, final RowMetadata metadata) {
        if (filters != null && filters[0].test(row, metadata)) {
            super.receive(row, metadata);
        }
    }

    @Override
    public void receive(final DataSetRow[] rows, final RowMetadata[] metadatas) {
        if (test(rows, metadatas)) {
            super.receive(rows, metadatas);
        }
    }

    private boolean test(DataSetRow[] rows, RowMetadata[] metadatas) {
        if (filters == null) {
            return true;
        }
        // Expect row.length == metadatas.length (otherwise it's a node API use issue).
        for (int i = 0; i < rows.length && i < filters.length; i++) {
            if (!filters[i].test(rows[i], metadatas[i])) {
                return false;
            }
        }
        return true;
    }
}
