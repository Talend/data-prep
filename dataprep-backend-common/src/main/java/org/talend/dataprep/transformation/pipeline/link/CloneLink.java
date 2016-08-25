package org.talend.dataprep.transformation.pipeline.link;

import java.util.Arrays;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.*;

public class CloneLink implements Link, RuntimeLink {

    private final Node[] nodes;

    private RowMetadata[] clonedMetadata;

    private RowMetadata[][] multiClonedMetadata;

    public CloneLink(Node... nodes) {
        this.nodes = nodes;
    }

    @Override
    public void emit(DataSetRow row, RowMetadata metadata) {
        initClonedMetadata(metadata);
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i].exec().receive(row.clone(), clonedMetadata[i]);
        }
    }

    @Override
    public void emit(DataSetRow[] rows, RowMetadata[] metadatas) {
        initClonedMetadata(metadatas);

        for (int i = 0; i < nodes.length; ++i) {
            final DataSetRow[] clonedRows = Arrays.stream(rows).map(DataSetRow::clone).toArray(DataSetRow[]::new);
            nodes[i].exec().receive(clonedRows, multiClonedMetadata[i]);
        }
    }

    @Override
    public void signal(Signal signal) {
        for (Node node : nodes) {
            node.exec().signal(signal);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitCloneLink(this);
    }

    @Override
    public RuntimeLink exec() {
        return this;
    }

    public Node[] getNodes() {
        return nodes;
    }

    /**
     * Clone the metadata to send the same on each branch.
     * RowMetadata are designed to be the same object over a branche in a pipeline.
     * Some nodes base their implementation on that.
     * Example 1 : the StatisticsNode configure the columns based on the first row metadata and update them on stop.
     * Example 2 : the WriterNode write the metadata of the last row.
     * For every node to work together, this metadata should be the same over a branch in the pipeline
     */
    private void initClonedMetadata(final RowMetadata metadata) {
        if (clonedMetadata != null) {
            return;
        }

        clonedMetadata = Arrays.stream(nodes).map((node) -> metadata.clone()).toArray(RowMetadata[]::new);
    }

    /**
     * Clone the metadata to send the same on each branch.
     * RowMetadata are designed to be the same object over a branche in a pipeline.
     * Some nodes base their implementation on that.
     * Example 1 : the StatisticsNode configure the columns based on the first row metadata and update them on stop.
     * Example 2 : the WriterNode write the metadata of the last row.
     * For every node to work together, this metadata should be the same over a branch in the pipeline
     */
    private void initClonedMetadata(RowMetadata[] multiMetadata) {
        if (multiClonedMetadata != null) {
            return;
        }

        multiClonedMetadata = Arrays.stream(nodes)
                .map((node) -> Arrays.stream(multiMetadata).map(RowMetadata::clone).toArray(RowMetadata[]::new))
                .toArray(RowMetadata[][]::new);
    }
}
