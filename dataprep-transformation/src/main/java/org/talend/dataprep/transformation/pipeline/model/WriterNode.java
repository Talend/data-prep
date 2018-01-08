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

package org.talend.dataprep.transformation.pipeline.model;

import static org.talend.dataprep.transformation.pipeline.Signal.END_OF_STREAM;

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
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.RuntimeNode;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

public class WriterNode extends BasicNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterNode.class);

    private final TransformerWriter writer;

    private final ConfiguredCacheWriter metadataCacheWriter;

    private final ContentCacheKey metadataKey;

    /** Fall back raw metadata when no row (hence row metadata) is received. */
    private RowMetadata fallBackRowMetadata;

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
     * @param fallBackRowMetadata fallback raw metadata to be able to write an empty content even if no row/rowMetadata id
     * received.
     */
    public WriterNode(final TransformerWriter writer, final ConfiguredCacheWriter metadataCacheWriter,
            final ContentCacheKey metadataKey, RowMetadata fallBackRowMetadata) {
        this.writer = writer;
        this.metadataCacheWriter = metadataCacheWriter;
        this.metadataKey = metadataKey;
        this.fallBackRowMetadata = fallBackRowMetadata;
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
            LOGGER.debug("already finished or canceled, let's skip this row");
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

        switch (signal) {
        case END_OF_STREAM:
            endOfStream();
            break;
        case CANCEL:
            cancel();
            break;
        default:
            LOGGER.debug("Unhandled signal {}.", signal);
        }

        super.signal(signal);
    }

    /**
     * Deal with the cancel signal.
     */
    private void cancel() {
        // just set stopped flag to true so that the writer is not used anymore
        this.isStopped.set(true);
    }

    /**
     * Deal with end of stream signal.
     */
    private void endOfStream() {
        if (isStopped.get()) {
            LOGGER.debug("cannot process {} because WriterNode is already finished or canceled", END_OF_STREAM);
            return;
        }

        // set this writer to stopped
        this.isStopped.set(true);

        final long start = System.currentTimeMillis();

        try {
            // no row received, let's switch to the fallback row metadata
            if (!startRecords) {
                writer.startObject();
                writer.fieldName("records");
                writer.startArray();
                lastRowMetadata = fallBackRowMetadata;
            }

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
        } catch (IOException e) {
            LOGGER.error("Unable to cache metadata for preparation #{} @ step #{}", metadataKey.getKey());
            LOGGER.debug("Unable to cache metadata due to exception.", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                LOGGER.error("unable to close writer", e);
            }
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public Node copyShallow() {
        return new WriterNode(writer, metadataCacheWriter, metadataKey, fallBackRowMetadata);
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
