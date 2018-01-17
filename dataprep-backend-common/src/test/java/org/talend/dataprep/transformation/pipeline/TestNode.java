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
import java.util.Arrays;
import java.util.List;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

public class TestNode extends BasicNode {
    private List<DataSetRow> receivedRows = new ArrayList<>();
    private List<RowMetadata> receivedMetadata = new ArrayList<>();
    private List<Signal> receivedSignals = new ArrayList<>();

    @Override
    public RuntimeNode exec() {
        return this;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        this.receivedRows.add(row);
        this.receivedMetadata.add(metadata);
        super.receive(row, metadata);
    }

    @Override
    public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
        this.receivedRows.addAll(Arrays.asList(rows));
        this.receivedMetadata.addAll(Arrays.asList(metadatas));
        super.receive(rows, metadatas);
    }

    @Override
    public void signal(Signal signal) {
        this.receivedSignals.add(signal);
        super.signal(signal);
    }

    public List<DataSetRow> getReceivedRows() {
        return receivedRows;
    }

    public List<RowMetadata> getReceivedMetadata() {
        return receivedMetadata;
    }

    public List<Signal> getReceivedSignals() {
        return receivedSignals;
    }

    @Override
    public Node copyShallow() {
        return new TestNode();
    }
}
