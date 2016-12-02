// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.transformation.pipeline.*;

/**
 * <p>
 * This node is dedicated to execution when a preparation is available. This node is used to group together nodes that correspond
 * to a step.
 * </p>
 * <p>
 * This allows code to reuse row metadata contained in step instead of provided one.
 * </p>
 *
 * @see org.talend.dataprep.transformation.pipeline.StepNodeTransformer
 */
public class StepNode extends BasicNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepNode.class);

    private final Step step;

    private final Node entryNode;

    private final Node lastNode;

    public StepNode(Step step, Node entryNode, Node lastNode) {
        this.step = step;
        this.entryNode = entryNode;
        this.lastNode = lastNode;
    }

    public Step getStep() {
        return step;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        Optional<RowMetadata> stepMetadata = Optional.ofNullable(step.getRowMetadata());
        final RowMetadata rowMetadata = stepMetadata.isPresent() ? stepMetadata.get() : metadata;
        if (!stepMetadata.isPresent()) {
            if (Step.ROOT_STEP.getId().equals(step.getId())) {
                LOGGER.warn("Trying to update row metadata on root step.");
            } else {
                step.setRowMetadata(rowMetadata);
            }
        }

        // make sure the last node (ActionNode) link is set to after the StepNode
        if (lastNode.getLink() == null) {
            final RuntimeLink stepLink = getLink().exec();
            lastNode.setLink(new StepLink(stepLink));
        }
        entryNode.exec().receive(row, rowMetadata);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitStepNode(this);
    }

    @Override
    public Node copyShallow() {
        return new StepNode(step, entryNode, lastNode);
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
