package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.transformation.pipeline.Node;

public class PassthroughNode extends BasicNode {

    @Override
    public Node copyShallow() {
        return new PassthroughNode();
    }
}
