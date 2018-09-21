package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.transformation.pipeline.Node;

/**
 * <p>
 * A {@link Node} very similar to {@link BasicNode}. The only difference is this class will not be discarded by
 * optimizations in {@link org.talend.dataprep.transformation.pipeline.Optimizer}.
 * </p>
 * <p>
 * Use this node to indicate a processing step that cannot be removed without impacting pipeline correctness (e.g. in
 * case of branching of pipeline).
 * </p>
 */
public class PassthroughNode extends BasicNode {

    @Override
    public Node copyShallow() {
        return new PassthroughNode();
    }
}
