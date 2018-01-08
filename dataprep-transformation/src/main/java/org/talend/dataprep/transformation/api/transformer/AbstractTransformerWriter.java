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

package org.talend.dataprep.transformation.api.transformer;

import java.io.IOException;

import org.talend.dataprep.api.dataset.row.DataSetRow;

public abstract class AbstractTransformerWriter implements TransformerWriter {

    protected abstract au.com.bytecode.opencsv.CSVWriter getRecordsWriter();

    @Override
    public void write(DataSetRow row) throws IOException {
        if (!row.values().isEmpty() && row.getRowMetadata().getColumns().isEmpty()) {
            throw new IllegalStateException(
                    " If a dataset row has some values it should at least have columns just before writing the result of a non json transformation.");
        }
        // values need to be written in the same order as the columns
        getRecordsWriter().writeNext(row.order().toArray(DataSetRow.SKIP_TDP_ID));
    }

}
