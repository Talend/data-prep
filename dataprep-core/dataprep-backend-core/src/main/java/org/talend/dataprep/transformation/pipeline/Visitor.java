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

import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

public abstract class Visitor {

    private void doNodeVisit(Node node) {
        if (node != null && node.getLink() != null) {
            node.getLink().accept(this);
        }
    }

    public void visitAction(ActionNode actionNode) {
        doNodeVisit(actionNode);
    }

    public void visitCompile(CompileNode compileNode) {
        doNodeVisit(compileNode);
    }

    public void visitSource(SourceNode sourceNode) {
        doNodeVisit(sourceNode);
    }

    public void visitBasicLink(BasicLink basicLink) {
        basicLink.getTarget().accept(this);
    }

    public void visitPipeline(Pipeline pipeline) {
        pipeline.getNode().accept(this);
    }

    public void visitStepNode(StepNode stepNode) {
        doNodeVisit(stepNode);
    }

    public void visitNode(Node node) {
        doNodeVisit(node);
    }

    public void visitCloneLink(CloneLink cloneLink) {
        final Node[] nodes = cloneLink.getNodes();
        for (Node node : nodes) {
            node.accept(this);
        }
    }
}
