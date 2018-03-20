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

    @Override
    public Node copyShallow() {
        return new BasicNode();
    }

}
