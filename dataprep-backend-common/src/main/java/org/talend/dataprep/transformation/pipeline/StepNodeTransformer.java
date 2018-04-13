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

package org.talend.dataprep.transformation.pipeline;

import java.util.List;
import java.util.function.Function;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Step;

/**
 * A utility class to transformer a pipeline (with nodes) and replace step-related nodes with
 * {@link org.talend.dataprep.transformation.pipeline.node.StepNode}.
 */
public class StepNodeTransformer {

    private StepNodeTransformer() {
    }

    /**
     * Groups all nodes (accessible from <code>node</code>) into {@link org.talend.dataprep.transformation.pipeline.node.StepNode
     * step nodes} when applicable. Each group node will consume a {@link Step} from <code>steps</code>.
     *
     * @param node The pipeline (as {@link Node}) to transform.
     * @param steps The {@link Step steps} to use when creating group nodes.
     * @param rowMetadataSupplier A function that allows visitor code to associate a row metadata with a step.
     * @return The transformed pipeline, based on copies of the original <code>node</code> (no modification done on the pipeline
     * reachable from <code>node/code>).
     */
    public static Node transform(Node node, List<Step> steps, Function<Step, RowMetadata> rowMetadataSupplier) {
        final StepNodeTransformation visitor = new StepNodeTransformation(steps, rowMetadataSupplier);
        node.accept(visitor);
        return visitor.getTransformedNode();
    }

}
