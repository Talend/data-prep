package org.talend.dataprep.transformation.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.transformation.pipeline.node.StepNode;

/**
 * Purpose of this class is to retrieve all the {@link Node} instances reachable from a given {@link Node}.
 * @see StepNode#getColumnNames()
 */
public class Flattener extends Visitor {

    private final List<Node> nodes = new ArrayList<>();

    @Override
    protected void doNodeVisit(Node node) {
        nodes.add(node);
        super.doNodeVisit(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
