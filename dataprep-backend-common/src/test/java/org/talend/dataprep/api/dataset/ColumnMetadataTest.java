//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;

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

    @Test
    public void should_be_compatible() {
        int id = 1;
        ColumnMetadata metadata1 = ColumnMetadata.Builder.column().id(id).name("name").type(Type.STRING).build();
        ColumnMetadata metadata2 = ColumnMetadata.Builder.column().id(id).name("name").type(Type.STRING).build();
        assertTrue(metadata1.compatible(metadata1));
        assertTrue(metadata1.compatible(metadata2));
        assertTrue(metadata2.compatible(metadata1));
    }

    @Test
    public void should_be_compatible_no_case_sensitive() {
        int id = 1;
        ColumnMetadata metadata1 = ColumnMetadata.Builder.column().id(id).name("name").type(Type.STRING).build();
        ColumnMetadata metadata2 = ColumnMetadata.Builder.column().id(id).name("Name").type(Type.STRING).build();
        assertTrue(metadata1.compatible(metadata1));
        assertTrue(metadata1.compatible(metadata2));
        assertTrue(metadata2.compatible(metadata1));
    }

    @Test
    public void should_be_incompatible() {
        int id = 1;
        ColumnMetadata metadata1 = ColumnMetadata.Builder.column().id(id).name("name").type(Type.STRING).build();
        ColumnMetadata metadata2 = ColumnMetadata.Builder.column().id(id).name("name").type(Type.INTEGER).build();
        ColumnMetadata metadata3 = ColumnMetadata.Builder.column().id(id).name("name2").type(Type.STRING).build();
        assertFalse(metadata1.compatible(metadata2));
        assertFalse(metadata2.compatible(metadata1));
        assertFalse(metadata3.compatible(metadata1));
        assertFalse(metadata3.compatible(metadata2));
    }


}
