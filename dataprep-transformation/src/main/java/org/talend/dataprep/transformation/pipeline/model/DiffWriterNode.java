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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

public class DiffWriterNode extends BasicNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffWriterNode.class);

    private final TransformerWriter writer;

    private long totalTime;

    private int count;

    private boolean startRecords;

    private boolean endMetadata;

    private RowMetadata[] lastMetadatas;

    public DiffWriterNode(final TransformerWriter writer) {
        this.writer = writer;
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
    public void receive(final DataSetRow[] rows, final RowMetadata[] metadatas) {
        final long start = System.currentTimeMillis();
        try {
            // write start if not already started
            if (!startRecords) {
                writer.startObject();
                writer.fieldName("records");
                writer.startArray();
                startRecords = true;
            }

            // write diff
            final DataSetRow initialRow = rows[rows.length - 1];
            for (int i = rows.length - 2; i >= 0; --i) {
                initialRow.diff(rows[i]);
            }
            if (initialRow.shouldWrite()) {
                writer.write(initialRow);
            }

            // save metadata array to write at the end
            lastMetadatas = metadatas;
        } catch (final IOException e) {
            LOGGER.error("Unable to write record.", e);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    @Override
    public void signal(Signal signal) {
        if ((signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) && !endMetadata) {
            final long start = System.currentTimeMillis();
            try {
                writer.endArray(); // <- end records
                writer.fieldName("metadata"); // <- start metadata
                writer.startObject();

                writer.fieldName("columns");
                final RowMetadata initialMetadata = lastMetadatas[lastMetadatas.length - 1];
                for (int i = lastMetadatas.length - 2; i >= 0; --i) {
                    initialMetadata.diff(lastMetadatas[i]);
                }
                // Preview don't need statistics, so wipe them out
                for (final ColumnMetadata column : initialMetadata.getColumns()) {
                    column.getStatistics().setInvalid(0);
                    column.getQuality().setInvalid(0);
                }
                writer.write(initialMetadata);

                writer.endObject();
                writer.endObject(); // <- end data set
                writer.flush();
            } catch (IOException e) {
                LOGGER.error("Unable to end writer.", e);
            } finally {
                totalTime += System.currentTimeMillis() - start;
                endMetadata = true;
            }
        } else {
            LOGGER.debug("Unhandled signal {}.", signal);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public Node copyShallow() {
        return new DiffWriterNode(writer);
    }
}
