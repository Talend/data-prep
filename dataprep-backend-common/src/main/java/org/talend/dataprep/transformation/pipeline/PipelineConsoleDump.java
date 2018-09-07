// ============================================================================
//
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

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.ApplyToColumn;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.ReservoirNode;
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

        builder
                .append("(")
                .append(monitored.getTotalTime())
                .append(" ms - ")
                .append(monitored.getCount())
                .append(" rows - ")
                .append(speed)
                .append(" rows/s) ");
    }

    private void buildApplyToColumn(ApplyToColumn applyToColumn) {
        builder.append(" (applies to ").append(applyToColumn.getColumnNames()).append(")");
    }

    @Override
    public void visitAction(ActionNode actionNode) {
        buildMonitorInformation(actionNode);
        builder
                .append("ACTION")
                .append(" [")
                .append(actionNode.getAction().getName())
                .append("] ")
                .append("(status: ")
                .append(actionNode.getActionContext().getActionStatus())
                .append(")");
        buildApplyToColumn(actionNode);
        builder.append('\n');
        super.visitAction(actionNode);
    }

    @Override
    public void visitCompile(CompileNode compileNode) {
        buildMonitorInformation(compileNode);
        builder
                .append("COMPILE")
                .append(" [")
                .append(compileNode.getAction().getName())
                .append("] ")
                .append("(status: ")
                .append(compileNode.getActionContext().getActionStatus())
                .append(")");
        buildApplyToColumn(compileNode);
        builder.append('\n');
        super.visitCompile(compileNode);
    }

    @Override
    public void visitSource(SourceNode sourceNode) {
        buildMonitorInformation(sourceNode);
        builder.append("SOURCE").append('\n');
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
        builder.append(formatClassName(node));
        if (node instanceof ApplyToColumn) {
            buildApplyToColumn((ApplyToColumn) node);
        }
        builder.append('\n');
        super.visitNode(node);
    }

    private String formatClassName(Node node) {
        return StringUtils.substringAfterLast(node.getClass().getName(), ".");
    }

    @Override
    public void visitCloneLink(CloneLink cloneLink) {
        builder.append("->").append('\n');
        super.visitCloneLink(cloneLink);
    }

    @Override
    public void visitStepNode(StepNode stepNode) {
        builder.append("STEP NODE (").append(stepNode.getStep()).append(")");
        if (stepNode.getParentStepRowMetadata() != null) {
            builder.append(" (*)\n");
        } else {
            builder.append("\n");
        }
        builder.append("{\n");
        stepNode.getEntryNode().accept(this);
        builder.append("}\n");
        super.visitStepNode(stepNode);
    }

    @Override
    public void visitInvalidDetection(InvalidDetectionNode invalidDetectionNode) {
        buildMonitorInformation(invalidDetectionNode);
        builder.append("INVALID DETECTION NODE ");
        buildApplyToColumn(invalidDetectionNode);
        builder.append('\n');
        super.visitInvalidDetection(invalidDetectionNode);
    }

    @Override
    public void visitReservoir(ReservoirNode reservoirNode) {
        final Node wrapped = reservoirNode.getWrapped();

        buildMonitorInformation(reservoirNode);
        builder.append("Reservoir (of ").append(formatClassName(wrapped)).append(")");
        buildApplyToColumn(reservoirNode);
        builder.append("\n{\n");
        wrapped.accept(this);
        builder.append("}\n");
        super.visitReservoir(reservoirNode);
    }
}
