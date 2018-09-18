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

import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;

public class FilteredSourceNode extends SourceNode {

    private final Predicate<DataSetRow> filter;

    public FilteredSourceNode(Predicate<DataSetRow> filter) {
        this.filter = filter;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        if (filter.test(row)) {
            super.receive(row, metadata);
        }
    }

    @Override
    public Node copyShallow() {
        return new FilteredSourceNode(filter);
    }
}
