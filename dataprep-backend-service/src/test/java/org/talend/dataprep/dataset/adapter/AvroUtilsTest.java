package org.talend.dataprep.dataset.adapter;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.util.avro.AvroUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

public class AvroUtilsTest {

    @Test
    public void shouldCreateSchemaWithName() {
        // given
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("name").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("id").type(Type.INTEGER).build());
        columnMetadatas.add(column().id(3).name("birth").type(Type.DATE).build());

        // when
        final Schema schema = AvroUtils.toSchema(columnMetadatas);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
    }

    @Test
    public void shouldHandleDuplicatedColumnName() {
        // given
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("name").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("name").type(Type.INTEGER).build());
        columnMetadatas.add(column().id(3).name("name").type(Type.DATE).build());

        // when
        final Schema schema = AvroUtils.toSchema(columnMetadatas);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(3, schema.getFields().size());
        assertNotNull(schema.getField("name"));
        assertNotNull(schema.getField("name_1"));
        assertNotNull(schema.getField("name_2"));
    }

    @Test
    public void shouldEscapeInvalidJavaCharacters() {
        // given
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("#@!abc").type(Type.STRING).build());

        // when
        final Schema schema = AvroUtils.toSchema(columnMetadatas);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(1, schema.getFields().size());
        assertEquals("DP___abc", schema.getFields().get(0).name());
    }

}
