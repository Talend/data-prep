//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the ColumnMetadata class.
 * 
 * @see ColumnMetadata
 */
public class ColumnMetadataTest {

    @Test
    public void should_format_id() {
        int id = 3;
        String expectedId = "0003";
        ColumnMetadata metadata = ColumnMetadata.Builder.column().id(id).name("name").type(Type.STRING).build();
        assertEquals(expectedId, metadata.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_allow_empty_Type() {
        ColumnMetadata.Builder.column().type(null).build();
    }

    @Test
    public void should_set_empty_statistics() {
        ColumnMetadata column = ColumnMetadata.Builder.column().id(42).type(Type.STRING).build();
        column.setStatistics(null);
        assertEquals(new Statistics(), column.getStatistics());
    }

}
