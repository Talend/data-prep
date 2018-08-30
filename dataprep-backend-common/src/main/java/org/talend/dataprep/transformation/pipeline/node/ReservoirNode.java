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

public class ReservoirNode extends BasicNode implements Monitored, ApplyToColumn {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservoirNode.class);

    private final ReplayProcessor<DataSetRow> processor;

    private final BlockingSink<DataSetRow> sink;

    private long count;

    private long totalTime;

    private Node wrapped;

    /**
     * @param wrapped
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

    public static Node convertToReservoir(Node node) {
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
        return convertToReservoir(wrapped.copyShallow());
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

    public Node getWrapped() {
        return wrapped;
    }
}
