package org.talend.dataprep.transformation.pipeline.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;

abstract class ColumnFilteredNode extends BasicNode {

    RowMetadata rowMetadata;

    List<ColumnMetadata> filteredColumns;

    Set<String> filteredColumnNames;

    private final Predicate<? super ColumnMetadata> filter;

    ColumnFilteredNode(Predicate<? super ColumnMetadata> filter) {
        this.filter = filter;
    }

    void performColumnFilter(DataSetRow row, RowMetadata metadata) {
        final boolean needRefresh = rowMetadata == null || !metadata.equals(rowMetadata);
        List<ColumnMetadata> columns = metadata.getColumns();
        if (!columns.isEmpty()) {
            if (filteredColumns == null || needRefresh) {
                filteredColumns = columns.stream().filter(filter).collect(Collectors.toList());
                filteredColumnNames = filteredColumns.stream().map(ColumnMetadata::getId).collect(Collectors.toSet());
            }
        } else {
            // No column in row metadata, guess all type, starting from string columns.
            ColumnMetadata.Builder builder = ColumnMetadata.Builder.column().type(Type.STRING);
            final int rowSize = row.toArray(DataSetRow.SKIP_TDP_ID).length;
            columns = new ArrayList<>(rowSize + 1);
            for (int i = 0; i < rowSize; i++) {
                final ColumnMetadata newColumn = builder.build();
                metadata.addColumn(newColumn);
                columns.add(newColumn);
            }
            filteredColumns = columns;
            filteredColumnNames = columns.stream().map(ColumnMetadata::getId).collect(Collectors.toSet());
        }
        rowMetadata = metadata;
    }
}
