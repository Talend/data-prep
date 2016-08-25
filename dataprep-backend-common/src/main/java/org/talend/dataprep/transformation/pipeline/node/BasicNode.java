package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.*;

public class BasicNode implements Node, RuntimeNode {

    Link link;

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        if (link != null) {
            link.exec().emit(row, metadata);
        }
    }

    @Override
    public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
        if (link != null) {
            link.exec().emit(rows, metadatas);
        }
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public void setLink(Link link) {
        this.link = link;
    }

    @Override
    public void signal(Signal signal) {
        if (link != null) {
            link.exec().signal(signal);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public RuntimeNode exec() {
        return this;
    }
}
