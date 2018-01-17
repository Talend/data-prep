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

package org.talend.dataprep.api.filter;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public abstract class FilterServiceTest {

    protected DataSetRow datasetRowFromValues;

    protected DataSetRow row;

    protected RowMetadata rowMetadata;

    @Before
    public void init() {
        datasetRowFromValues = new DataSetRow(new HashMap<>());

        final ColumnMetadata firstColumn = new ColumnMetadata();
        firstColumn.setId("0001");
        final ColumnMetadata secondColumn = new ColumnMetadata();
        secondColumn.setId("0002");
        rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Arrays.asList(firstColumn, secondColumn));
        row = new DataSetRow(rowMetadata);
    }

}
