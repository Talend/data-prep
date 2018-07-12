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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Flattener;
import org.talend.dataprep.transformation.pipeline.Link;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.RuntimeLink;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;

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
public class StepNode extends BasicNode implements ApplyToColumn {

    private final String step;

    private final Node entryNode;

    private final Node lastNode;

    private RowMetadata parentStepRowMetadata;

    public StepNode(String step, RowMetadata parentStepRowMetadata, Node entryNode, Node lastNode) {
        this.step = step;
        this.parentStepRowMetadata = parentStepRowMetadata;
        this.entryNode = entryNode;
        this.lastNode = lastNode;
    }

    public Node getEntryNode() {
        return entryNode;
    }

    public String getStep() {
        return step;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        RowMetadata processingRowMetadata = metadata;
        if (parentStepRowMetadata != null) {
            // Step node has associated metadata, use it instead of supplied one.
            processingRowMetadata = parentStepRowMetadata;
        }

        // make sure the last node (ActionNode) link is set to after the StepNode
        if (lastNode.getLink() == null) {
            final RuntimeLink stepLink = getLink().exec();
            lastNode.setLink(new StepLink(stepLink));
        }
        entryNode.exec().receive(row, processingRowMetadata);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitStepNode(this);
    }

    @Override
    public Node copyShallow() {
        return new StepNode(step, parentStepRowMetadata, entryNode, lastNode);
    }

    @Override
    public List<String> getColumnNames() {
        final Flattener flattener = new Flattener();
        entryNode.accept(flattener);
        final Set<String> columnNames = new HashSet<>();
        for (Node node : flattener.getNodes()) {
            if (node instanceof ApplyToColumn) {
                columnNames.addAll(((ApplyToColumn) node).getColumnNames());
            }
        }
        return new ArrayList<>(columnNames);
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
