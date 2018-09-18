package org.talend.dataprep.transformation.pipeline.link;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.*;

public class BasicLink implements Link, RuntimeLink {

    private final Node target;

    public BasicLink(Node target) {
        this.target = target;
    }

    @Override
    public void emit(DataSetRow row, RowMetadata metadata) {
        target.exec().receive(row, metadata);
    }

    @Override
    public void emit(DataSetRow[] rows, RowMetadata[] metadatas) {
        target.exec().receive(rows, metadatas);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitBasicLink(this);
    }

    @Override
    public RuntimeLink exec() {
        return this;
    }

    @Override
    public void signal(Signal signal) {
        target.exec().signal(signal);
    }

    @Override
    public Node getTarget() {
        return target;
    }
}
