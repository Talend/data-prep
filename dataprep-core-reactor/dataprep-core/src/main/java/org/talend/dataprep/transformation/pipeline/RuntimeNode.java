package org.talend.dataprep.transformation.pipeline;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * A node is a processing unit inside the transformation pipeline.
 */
public interface RuntimeNode {

    /**
     * Called by an incoming {@link RuntimeLink} when new row is submitted to the pipeline.
     *
     * @param row A {@link DataSetRow row} to be processed by this node.
     * @param metadata The {@link RowMetadata row metadata} to be used when processing the <code>row</code>.
     */
    void receive(DataSetRow row, RowMetadata metadata);

    /**
     * Called by an incoming {@link RuntimeLink} when new group of row is submitted to the pipeline.
     *
     * @param rows An array of {@link DataSetRow row} to be processed by this node.
     * @param metadatas The array of {@link RowMetadata row metadata} to be used when processing the <code>row</code>.
     */
    void receive(DataSetRow[] rows, RowMetadata[] metadatas);

    /**
     * Sends a {@link Signal event} to the node. Signals are data-independent events to indicate external events (such
     * as end of the stream). Node implementations are responsible of the signal propagation using the
     * {@link RuntimeLink link} .
     *
     * @param signal A {@link Signal signal} to be sent to the pipeline.
     * @see Node#setLink(Link)
     */
    void signal(Signal signal);

}
