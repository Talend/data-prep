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

package org.talend.dataprep.transformation.pipeline.builder;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Link;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.FilteredSourceNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.ZipLink;

public class NodeBuilder {

    private final Node sourceNode;

    private State state;

    /**
     * Constructor from an initial node as source
     */
    private NodeBuilder(Node sourceNode) {
        this.sourceNode = sourceNode;
        state = new NodeState(sourceNode);
    }

    /**
     * Set a SourceNode as initial node.
     * This SourceNode is a BasicNode but logged as source
     */
    public static NodeBuilder source() {
        return new NodeBuilder(new SourceNode());
    }

    /**
     * Create a new builder from a provided source node
     */
    public static NodeBuilder from(Node node) {
        return new NodeBuilder(node);
    }

    /**
     * Create a new builder from a filter node as a source
     */
    public static NodeBuilder filteredSource(Predicate<DataSetRow> filter) {
        return new NodeBuilder(new FilteredSourceNode(filter));
    }

    /**
     * Append a new node at the end, with a basic link
     */
    public NodeBuilder to(final Node node) {
        final Function<Node[], Link> linkFunction = n -> new BasicLink(n[0]);
        return this.to(linkFunction, node);
    }

    /**
     * Append a new node at the end, with a custom link
     *
     * @param linkFunction A function that create the link from the previous nodes
     * @param node The new node to append
     */
    public NodeBuilder to(final Function<Node[], Link> linkFunction, final Node node) {
        try {
            state = state.next(linkFunction);
            state = state.next(node);
        } catch (IllegalStateException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION,
                    new Exception("Each to() must be followed by node().", e));
        }
        return this;
    }

    /**
     * Create a multi pipeline.
     * The previous node will dispatch its input to all the provided nodes via a CloneLink
     */
    public NodeBuilder dispatchTo(final Node... nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION,
                    new IllegalArgumentException("Each dispatchTo() must be followed by nodes()."));
        }

        state = state.next(CloneLink::new);
        state = state.next(nodes);
        return this;
    }

    /**
     * Create a multi pipeline and zip them
     */
    public NodeBuilder zipTo(final Node target) {
        if (target == null) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION,
                    new IllegalArgumentException("Each zipTo() must be followed by nodes()."));
        }

        final Node[] sources = state.getNodes();
        ZipLink.zip(sources, target);
        state = new NodeState(target);

        return this;
    }

    /**
     * Build the node pipeline
     *
     * @return The first node
     */
    public Node build() {
        return sourceNode;
    }

    private interface State {

        State next(Function<Node[], Link> link);

        State next(Node... node);

        Node[] getNodes();
    }

    private static class LinkState implements State {

        private final Node[] previousNodes;

        private final Function<Node[], Link> linkFunction;

        private LinkState(Node[] previousNodes, Function<Node[], Link> linkFunction) {
            this.previousNodes = previousNodes;
            this.linkFunction = linkFunction;
        }

        @Override
        public State next(Function<Node[], Link> link) {
            throw new IllegalStateException();
        }

        @Override
        public State next(Node... nodes) {
            try {
                previousNodes[0].setLink(linkFunction.apply(nodes));
            } catch (UnsupportedOperationException e) {
                throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION,
                        new Exception("Unable to specify a new output after a terminal node.", e));
            }

            return new NodeState(nodes);
        }

        @Override
        public Node[] getNodes() {
            return previousNodes;
        }
    }

    private static class NodeState implements State {

        private final Node[] nodes;

        private NodeState(Node... nodes) {
            // get last node of a sub-pipeline if entry is not a simple node
            this.nodes = Arrays.stream(nodes).map(nextNode -> {
                Node last = nextNode;
                while (last.getLink() != null && last.getLink().getTarget() != null) {
                    last = last.getLink().getTarget();
                }
                return last;
            }).toArray(Node[]::new);
        }

        @Override
        public State next(Function<Node[], Link> link) {
            return new LinkState(nodes, link);
        }

        @Override
        public State next(Node... node) {
            throw new IllegalStateException();
        }

        @Override
        public Node[] getNodes() {
            return nodes;
        }
    }
}
