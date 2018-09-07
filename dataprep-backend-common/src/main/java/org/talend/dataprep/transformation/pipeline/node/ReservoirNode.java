package org.talend.dataprep.transformation.pipeline.node;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.ReplayProcessor;

/**
 * <p>
 * A {@link Node} implementation that stores all processed rows till
 * {@link org.talend.dataprep.transformation.pipeline.RuntimeNode#signal(Signal)} is called.
 * </p>
 * <p>
 * This is especially useful when a node needs to process all records first then resend oll processed rows to following
 * nodes.
 * </p>
 */
public class ReservoirNode extends BasicNode implements Monitored, ApplyToColumn {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservoirNode.class);

    private final ReplayProcessor<DataSetRow> processor;

    private final BlockingSink<DataSetRow> sink;

    private final Node wrapped;

    private long count;

    private long totalTime;

    /**
     * Create a reservoir around the given node.
     *
     * @param wrapped The {@link Node} to create a reservoir for.
     * @see #convertToReservoir(Node)
     */
    private ReservoirNode(Node wrapped) {
        this.wrapped = wrapped.copyShallow();
        this.processor = ReplayProcessor.create(30000, false);
        this.sink = processor.connectSink();
        processor.subscribe(row -> {
            wrapped.exec().receive(row, row.getRowMetadata());
            count++;
        });
    }

    /**
     * Creates a reservoir for the given node. Reservoir node stores all rows received in
     * {@link #receive(DataSetRow, RowMetadata)} and <b>only</b> send rows to next nodes when {@link #signal(Signal)} is
     * called.
     * 
     * @param node The {@link Node} to create a reservoir around.
     * @return A {@link ReservoirNode} for the given node.
     */
    public static Node convertToReservoir(Node node) {
        if (node instanceof ReactiveTypeDetectionNode) {
            return node;
        }
        return new ReservoirNode(node);
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        sink.emit(row.clone());
    }

    @Override
    public void signal(Signal signal) {
        try {
            switch (signal) {
            case END_OF_STREAM:
            case STOP:
            case CANCEL:
            default:
                final long start = System.currentTimeMillis();
                processor.onComplete();
                processor.subscribe(row -> {
                    LOGGER.trace("forward row: {}", row);
                    link.exec().emit(row, row.getRowMetadata());
                });
                totalTime += System.currentTimeMillis() - start;
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to forward rows to next node(s).", e);
        }
        super.signal(signal);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public Node copyShallow() {
        return convertToReservoir(wrapped);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitReservoir(this);
    }

    @Override
    public List<String> getColumnNames() {
        if (wrapped instanceof ApplyToColumn) {
            return ((ApplyToColumn) wrapped).getColumnNames();
        }
        return Collections.emptyList();
    }

    /**
     * @return The {@link Node} wrapped by this reservoir.
     */
    public Node getWrapped() {
        return wrapped;
    }
}
