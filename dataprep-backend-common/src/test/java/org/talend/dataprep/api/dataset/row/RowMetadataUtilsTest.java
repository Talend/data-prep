// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.dataset.row;

import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the org.talend.dataprep.api.dataset.row.RowMetadataUtils class.
 *
 * @see RowMetadataUtils
 */
public class RowMetadataUtilsTest {

    @Test
    @Ignore("No explicit fix - see TDP-7705")
    public void shouldCreateSchemaWithName() {

        // given
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("name").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("id").type(Type.INTEGER).build());
        columnMetadatas.add(column().id(3).name("birth").type(Type.DATE).build());

        // when
        final Schema schema = RowMetadataUtils.toSchema(columnMetadatas);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
    }

}
