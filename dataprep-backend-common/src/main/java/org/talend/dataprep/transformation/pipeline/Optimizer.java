package org.talend.dataprep.transformation.pipeline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.ApplyToColumn;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.FilterNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.ReactiveTypeDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.ReservoirNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

/**
 * <p>
 * This class performs clean up and optimizations in nodes to be found in {@link Pipeline}.
 * </p>
 * <h3>BasicNode removal</h3>
 * <p>
 * {@link BasicNode} are used as fillers for no-op and can be removed.
 * </p>
 * <h3>Node that applies on 0 columns</h3>
 * <p>
 * Any node that implements {@link ApplyToColumn} that returns empty list for {@link ApplyToColumn#getColumnNames()} is
 * also removed from pipeline because it won't perform any operation.
 * </p>
 * <h3>Useless type detection</h3>
 * <p>
 * If a type detection node is located in a place where no modification was done before, the type detection is ignored,
 * since input metadata already provides enough type information.
 * </p>
 */
public class Optimizer extends Visitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Optimizer.class);

    private static final List<Class> allowedNodesForUnknownColumns =
            Arrays.asList(StepNode.class, ActionNode.class, CompileNode.class);

    private final Set<String> currentlyModifiedColumns = new HashSet<>();

    private NodeBuilder builder = NodeBuilder.source();

    private Function<Node[], Link> lastLink;

    private int nonSourceNodeKept = 0;

    public Pipeline getOptimized() {
        return new Pipeline(builder.build());
    }

    private void discard(Node node, String reason) {
        LOGGER.debug("Discard node '{}': {}", node, reason);
    }

    private void keep(Node node) {
        if (!(node instanceof SourceNode)) {
            nonSourceNodeKept++;
        }
        LOGGER.debug("Keep node '{}'.", node);
        if (!node.getClass().equals(FilterNode.class)) {
            builder.to(lastLink, node.copyShallow());
        } else {
            builder.to(lastLink, node);
        }
    }

    private void handleApplyToColumn(Node node) {
        if (!(node instanceof ApplyToColumn)) {
            keep(node);
            return;
        }

        final ApplyToColumn applyToColumn = (ApplyToColumn) node;
        if (applyToColumn.getColumnNames().isEmpty()) {
            if (nonSourceNodeKept == 0 && !allowedNodesForUnknownColumns.contains(node.getClass())) {
                discard(node,
                        "Applies to 0 columns (and only source node(s) met so far, and not an allowed empty column node).");
            } else {
                keep(node);
            }
        } else {
            if (node.getClass().equals(ActionNode.class) || node.getClass().equals(StepNode.class)) {
                currentlyModifiedColumns.addAll(applyToColumn.getColumnNames());
                keep(node);
            } else if (node.getClass().equals(ReactiveTypeDetectionNode.class)) {
                if (applyToColumn.getColumnNames().stream().anyMatch(currentlyModifiedColumns::contains)) {
                    keep(node);
                    currentlyModifiedColumns.clear();
                } else if (currentlyModifiedColumns.isEmpty()) {
                    discard(node, "No modified column. No need to detect types.");
                } else {
                    discard(node, "Targeted columns are not modified.");
                }
            } else {
                keep(node);
            }
        }
    }

    @Override
    public void visitAction(ActionNode actionNode) {
        handleApplyToColumn(actionNode);
        super.visitAction(actionNode);
    }

    @Override
    public void visitCompile(CompileNode compileNode) {
        handleApplyToColumn(compileNode);
        super.visitCompile(compileNode);
    }

    @Override
    public void visitBasicLink(BasicLink basicLink) {
        lastLink = nodes -> new BasicLink(nodes[0]);
        super.visitBasicLink(basicLink);
    }

    @Override
    public void visitStepNode(StepNode stepNode) {
        handleApplyToColumn(stepNode);
        super.visitStepNode(stepNode);
    }

    @Override
    public void visitNode(Node node) {
        if (node.getClass().equals(BasicNode.class)) {
            discard(node, "Remove no-op node.");
        } else if (node instanceof ApplyToColumn) {
            handleApplyToColumn(node);
        } else {
            keep(node);
        }
        super.visitNode(node);
    }

    @Override
    public void visitTypeDetection(ReactiveTypeDetectionNode typeDetectionNode) {
        handleApplyToColumn(typeDetectionNode);
        super.visitTypeDetection(typeDetectionNode);
    }

    @Override
    public void visitInvalidDetection(InvalidDetectionNode invalidDetectionNode) {
        handleApplyToColumn(invalidDetectionNode);
        super.visitInvalidDetection(invalidDetectionNode);
    }

    @Override
    public void visitCloneLink(CloneLink cloneLink) {
        lastLink = CloneLink::new;
        super.visitCloneLink(cloneLink);
    }

    @Override
    public void visitReservoir(ReservoirNode reservoirNode) {
        if (reservoirNode.getWrapped() instanceof BasicNode) {
            // TODO Should be equals use (there seems to be an issue with type detection here).
            discard(reservoirNode, "No need for reservoir over a no-op node.");
        } else {
            keep(reservoirNode);
        }
        super.visitReservoir(reservoirNode);
    }
}
