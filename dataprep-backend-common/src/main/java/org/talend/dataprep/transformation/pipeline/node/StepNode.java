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

package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.transformation.pipeline.*;

/**
 * <p>
 * This node is dedicated to execution when a preparation is available. This node is used to group together nodes that
 * correspond to a step.
 * </p>
 * <p>
 * This allows code to reuse row metadata contained in step instead of provided one.
 * </p>
 *
 * @see org.talend.dataprep.transformation.pipeline.StepNodeTransformer
 */
public class StepNode extends BasicNode {

    private final Step step;

    private final Node entryNode;

    private final Node lastNode;

    private RowMetadata lastRowMetadata;

    private RowMetadata stepRowMetadata;

    public StepNode(Step step, RowMetadata stepRowMetadata, Node entryNode, Node lastNode) {
        this.step = step;
        this.stepRowMetadata = stepRowMetadata;
        this.entryNode = entryNode;
        this.lastNode = lastNode;
    }

    public Node getEntryNode() {
        return entryNode;
    }

    public Step getStep() {
        return step;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        RowMetadata processingRowMetadata = metadata;
        if (stepRowMetadata != null) {
            // Step node has associated metadata, use it instead of supplied one.
            processingRowMetadata = stepRowMetadata;
        }

        // make sure the last node (ActionNode) link is set to after the StepNode
        if (lastNode.getLink() == null) {
            final RuntimeLink stepLink = getLink().exec();
            lastNode.setLink(new StepLink(stepLink));
        }
        lastRowMetadata = processingRowMetadata;
        entryNode.exec().receive(row, processingRowMetadata);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitStepNode(this);
    }

    @Override
    public Node copyShallow() {
        return new StepNode(step, stepRowMetadata, entryNode, lastNode);
    }

    /**
     * @return The last row metadata used in {@link #receive(DataSetRow, RowMetadata)}. Might be step's metadata (if
     * any) or supplied metadata.
     */
    public RowMetadata getRowMetadata() {
        return lastRowMetadata;
    }

    private static class StepLink implements Link {

        private final RuntimeLink stepLink;

        private StepLink(RuntimeLink stepLink) {
            this.stepLink = stepLink;
        }

        @Override
        public void accept(Visitor visitor) {
        }

        @Override
        public RuntimeLink exec() {
            return new RuntimeLink() {

                @Override
                public void emit(DataSetRow row, RowMetadata metadata) {
                    stepLink.emit(row, metadata);
                }

                @Override
                public void emit(DataSetRow[] rows, RowMetadata[] metadatas) {
                    stepLink.emit(rows, metadatas);
                }

                @Override
                public void signal(Signal signal) {
                    stepLink.signal(signal);
                }
            };
        }
    }
}
