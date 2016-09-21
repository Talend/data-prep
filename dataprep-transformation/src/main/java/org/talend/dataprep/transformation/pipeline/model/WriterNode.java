package org.talend.dataprep.transformation.pipeline.model;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.transformation.api.transformer.ConfiguredCacheWriter;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.RuntimeNode;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

public class WriterNode extends BasicNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterNode.class);

    private final TransformerWriter writer;

    private final ConfiguredCacheWriter metadataCacheWriter;

    private final ContentCacheKey metadataKey;

    private RowMetadata lastRowMetadata;

    private boolean startRecords = false;

    private long totalTime;

    private int count;

    /** True if the writer is stopped. */
    private AtomicBoolean isStopped = new AtomicBoolean(false);

    /**
     * Constructor.
     * 
     * @param writer the transformer writer.
     * @param metadataCacheWriter the metadata cache writer.
     * @param metadataKey the transformation metadata cache key to use.
     */
    public WriterNode(final TransformerWriter writer, final ConfiguredCacheWriter metadataCacheWriter, final ContentCacheKey metadataKey) {
        this.writer = writer;
        this.metadataCacheWriter = metadataCacheWriter;
        this.metadataKey = metadataKey;
    }

    /**
     * Synchronized method not to clash with the signal method.
     * 
     * @see WriterNode#signal(Signal)
     * @see RuntimeNode#receive(DataSetRow, RowMetadata)
     */
    @Override
    public synchronized void receive(DataSetRow row, RowMetadata metadata) {

        // do not write this row if the writer is stopped
        if (isStopped.get()) {
            LOGGER.debug("already stopped, let's skip this row");
            return;
        }

        final long start = System.currentTimeMillis();
        try {
            if (!startRecords) {
                writer.startObject();
                writer.fieldName("records");
                writer.startArray();
                startRecords = true;
            }
            lastRowMetadata = metadata;
            if (row.shouldWrite()) {
                row.setRowMetadata(metadata);
                writer.write(row);
                super.receive(row, metadata);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to write record.", e);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    /**
     * Synchronized method not to clash with the receive method.
     * 
     * @see WriterNode#receive(DataSetRow, RowMetadata)
     * @see RuntimeNode#signal(Signal)
     */
    @Override
    public synchronized void signal(Signal signal) {

        LOGGER.debug("receive {}", signal);

        if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) {

            if (isStopped.get()) {
                LOGGER.debug("cannot process {} because WriterNode is already stopped", signal);
                super.signal(signal);
                return;
            }

            // set this writer to stopped
            this.isStopped.set(true);

            final long start = System.currentTimeMillis();
            try {
                writer.endArray(); // <- end records
                writer.fieldName("metadata"); // <- start metadata
                writer.startObject();

                writer.fieldName("columns");
                writer.write(lastRowMetadata);

                writer.endObject();
                writer.endObject(); // <- end data set
                writer.flush();
            } catch (IOException e) {
                LOGGER.error("Unable to end writer.", e);
            } finally {
                totalTime += System.currentTimeMillis() - start;
            }

            // Cache computed metadata for later reuse
            try {
                metadataCacheWriter.write(metadataKey, lastRowMetadata);
                writer.flush();
            } catch (IOException e) {
                LOGGER.error("Unable to cache metadata for preparation #{} @ step #{}", metadataKey.getKey());
                LOGGER.debug("Unable to cache metadata due to exception.", e);
            }

        } else {
            LOGGER.debug("Unhandled signal {}.", signal);
        }

        super.signal(signal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    public TransformerWriter getWriter() {
        return writer;
    }
}
