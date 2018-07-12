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

import static java.util.Optional.ofNullable;

import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;
import org.talend.dataprep.transformation.pipeline.node.TypeDetectionNode;

public abstract class Visitor {

    protected void doNodeVisit(Node node) {
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
        ofNullable(pipeline.getNode()).ifPresent(n -> n.accept(this));
    }

    public void visitStepNode(StepNode stepNode) {
        doNodeVisit(stepNode);
    }

    public void visitNode(Node node) {
        doNodeVisit(node);
    }

    public void visitTypeDetection(TypeDetectionNode typeDetectionNode) {
        doNodeVisit(typeDetectionNode);
    }

    public void visitInvalidDetection(InvalidDetectionNode invalidDetectionNode) {
        doNodeVisit(invalidDetectionNode);
    }

    public void visitCloneLink(CloneLink cloneLink) {
        final Node[] nodes = cloneLink.getNodes();
        for (Node node : nodes) {
            node.accept(this);
        }
    }
}
