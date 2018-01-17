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
import org.talend.dataprep.transformation.pipeline.link.BasicLink;

public class TestLink extends BasicLink {
    private List<DataSetRow> emittedRows = new ArrayList<>();
    private List<RowMetadata> emittedMetadata = new ArrayList<>();
    private List<Signal> emittedSignals = new ArrayList<>();

    public TestLink(final Node target) {
        super(target);
    }

    @Override
    public RuntimeLink exec() {
        return this;
    }

    @Override
    public void emit(DataSetRow row, RowMetadata metadata) {
        this.emittedRows.add(row);
        this.emittedMetadata.add(metadata);
        super.emit(row, metadata);
    }

    @Override
    public void emit(DataSetRow[] rows, RowMetadata[] metadatas) {
        this.emittedRows.addAll(Arrays.asList(rows));
        this.emittedMetadata.addAll(Arrays.asList(metadatas));
        super.emit(rows, metadatas);
    }

    @Override
    public void signal(Signal signal) {
        this.emittedSignals.add(signal);
    }

    public List<DataSetRow> getEmittedRows() {
        return emittedRows;
    }

    public List<RowMetadata> getEmittedMetadata() {
        return emittedMetadata;
    }

    public List<Signal> getEmittedSignals() {
        return emittedSignals;
    }
}
