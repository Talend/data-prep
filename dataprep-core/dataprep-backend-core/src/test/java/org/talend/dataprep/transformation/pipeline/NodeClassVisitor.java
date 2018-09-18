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

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

public class NodeClassVisitor extends Visitor {

    List<Class> traversedClasses = new ArrayList<>();

    @Override
    public void visitAction(ActionNode actionNode) {
        traversedClasses.add(actionNode.getClass());
        super.visitAction(actionNode);
    }

    @Override
    public void visitCompile(CompileNode compileNode) {
        traversedClasses.add(compileNode.getClass());
        super.visitCompile(compileNode);
    }

    @Override
    public void visitSource(SourceNode sourceNode) {
        traversedClasses.add(sourceNode.getClass());
        super.visitSource(sourceNode);
    }

    @Override
    public void visitBasicLink(BasicLink basicLink) {
        traversedClasses.add(basicLink.getClass());
        super.visitBasicLink(basicLink);
    }

    @Override
    public void visitPipeline(Pipeline pipeline) {
        traversedClasses.add(pipeline.getClass());
        super.visitPipeline(pipeline);
    }

    @Override
    public void visitNode(Node node) {
        traversedClasses.add(node.getClass());
        super.visitNode(node);
    }

    @Override
    public void visitCloneLink(CloneLink cloneLink) {
        traversedClasses.add(cloneLink.getClass());
        super.visitCloneLink(cloneLink);
    }

    @Override
    public void visitStepNode(StepNode stepNode) {
        traversedClasses.add(stepNode.getClass());
        super.visitStepNode(stepNode);
    }
}
