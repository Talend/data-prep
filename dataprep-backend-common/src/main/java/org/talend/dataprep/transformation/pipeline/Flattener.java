package org.talend.dataprep.transformation.pipeline;

import java.util.ArrayList;
import java.util.List;

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
