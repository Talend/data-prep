package org.talend.dataprep.transformation.pipeline.node;

import java.util.*;
import java.util.stream.IntStream;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Link;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;

/**
 * Node that zip multiple stream.
 * It waits to receive all the inputs entry, and emit an array of all the inputs to the output
 */
public class ZipLink extends BasicLink {

    private final int numberOfSource;

    private final Stack[] stacks;

    private final List<Signal> signalStack;

    private ZipLink(final Node[] sources, final Node target) {
        super(target);

        this.numberOfSource = sources.length;
        this.signalStack = new ArrayList<>(this.numberOfSource);
        this.stacks = IntStream.range(0, this.numberOfSource).mapToObj(n -> new Stack()).toArray(Stack[]::new);

        // attach to each source a zipper that will notify to this ZipNode which source and values it receive
        IntStream.range(0, this.numberOfSource).forEach(index -> {
            final Node source = sources[index];
            final Link zipper = new Zipper(this, target, index);
            source.setLink(zipper);
        });
    }

    @Override
    public void signal(final Signal signal) {
        signalStack.add(signal);

        if (signalStack.size() == numberOfSource) {
            super.signal(signal);
            signalStack.clear();
        }
    }

    private void emit(final DataSetRow row, final RowMetadata metadata, final int index) {
        stacks[index].push(row.clone(), metadata);

        if (allStacksHaveNext()) {
            final DataSetRow[] rows = popAllStacksNextRow();
            final RowMetadata[] metadatas = popAllStacksNextMetadata();
            super.emit(rows, metadatas);
        }
    }

    public static ZipLink zip(final Node[] sources, final Node target) {
        return new ZipLink(sources, target);
    }

    private RowMetadata[] popAllStacksNextMetadata() {
        return Arrays.stream(stacks).map(Stack::popMetadata).toArray(RowMetadata[]::new);
    }

    private DataSetRow[] popAllStacksNextRow() {
        return Arrays.stream(stacks).map(Stack::popRow).toArray(DataSetRow[]::new);
    }

    private boolean allStacksHaveNext() {
        return Arrays.stream(stacks).allMatch((Stack::hasNext));
    }

    public class Zipper extends BasicLink {

        private final ZipLink proxy;

        private final int index;

        Zipper(final ZipLink proxy, final Node target, final int index) {
            super(target);
            this.proxy = proxy;
            this.index = index;
        }

        @Override
        public void emit(final DataSetRow row, final RowMetadata metadata) {
            proxy.emit(row, metadata, index);
        }

        @Override
        public void signal(Signal signal) {
            proxy.signal(signal);
        }
    }

    private class Stack {

        private final Deque<DataSetRow> rowStack = new ArrayDeque<>();

        private final Deque<RowMetadata> metadataStack = new ArrayDeque<>();

        public boolean hasNext() {
            return !this.rowStack.isEmpty();
        }

        void push(final DataSetRow row, final RowMetadata metadata) {
            rowStack.addFirst(row);
            metadataStack.addFirst(metadata);
        }

        DataSetRow popRow() {
            return rowStack.pollLast();
        }

        RowMetadata popMetadata() {
            return metadataStack.pollLast();
        }
    }
}
