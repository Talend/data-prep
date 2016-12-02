//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.pipeline;

import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

public class PipelineConsoleDump extends Visitor {

    private final StringBuilder builder;

    public PipelineConsoleDump(StringBuilder builder) {
        this.builder = builder;
    }

    private void buildMonitorInformation(Monitored monitored) {
        final long totalTime = monitored.getTotalTime();
        final long count = monitored.getCount();
        double speed = totalTime > 0 ? Math.round(((double) count * 1000) / totalTime) : Double.POSITIVE_INFINITY;

        builder.append("(").append(monitored.getTotalTime()).append(" ms - ").append(monitored.getCount()).append(" rows - ")
                .append(speed).append(" rows/s) ");
    }

    @Override
    public void visitAction(ActionNode actionNode) {
        buildMonitorInformation(actionNode);
        builder.append("ACTION").append(" [").append(actionNode.getAction().getName()).append("] ").append("(status: ")
                .append(actionNode.getActionContext().getActionStatus()).append(")").append('\n');
        super.visitAction(actionNode);
    }

    @Override
    public void visitCompile(CompileNode compileNode) {
        builder.append("COMPILE").append(" [").append(compileNode.getAction().getName()).append("] ").append("(status: ")
                .append(compileNode.getActionContext().getActionStatus()).append(")").append('\n');
        super.visitCompile(compileNode);
    }

    @Override
    public void visitSource(SourceNode sourceNode) {
        builder.append("-> SOURCE").append('\n');
        super.visitSource(sourceNode);
    }

    @Override
    public void visitBasicLink(BasicLink basicLink) {
        builder.append("-> ");
        super.visitBasicLink(basicLink);
    }

    @Override
    public void visitPipeline(Pipeline pipeline) {
        builder.append("PIPELINE {").append('\n');
        super.visitPipeline(pipeline);
        builder.append('\n').append('}').append('\n');
    }

    @Override
    public void visitNode(Node node) {
        if (node instanceof Monitored) {
            buildMonitorInformation((Monitored) node);
        }
        builder.append("UNKNOWN NODE (").append(node.getClass().getName()).append(")").append('\n');
        super.visitNode(node);
    }

    @Override
    public void visitCloneLink(CloneLink cloneLink) {
        builder.append("->").append('\n');
        super.visitCloneLink(cloneLink);
    }

    @Override
    public void visitStepNode(StepNode stepNode) {
        builder.append("STEP NODE (").append(stepNode.getStep().toString()).append(")\n");
        super.visitStepNode(stepNode);
    }
}
