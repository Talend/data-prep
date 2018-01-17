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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.format.BaseFormatTest;

public abstract class AbstractTransformerWriterTest extends BaseFormatTest {

    protected AbstractTransformerWriter writer;

    /**
     * <a href="https://jira.talendforge.org/browse/TDP-3188>TDP-3188</a>
     * 
     */
    @Test(expected = IllegalStateException.class)
    public void should_only_write_values_in_columns_order_TDP_3188() throws Exception {

        final Map<String, String> values = new HashMap<>();
        values.put("key", "value");
        DataSetRow row = new DataSetRow(new RowMetadata(), values);

        writer.write(row);
    }

}