package org.talend.dataprep.util.avro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

public class AvroUtilsTest {

    @Test
    public void shouldCreateSchemaWithName() {
        // given
        List<ColumnMetadata> columnsMetadata = new ArrayList<>();
        columnsMetadata.add(column().id(1).name("name").type(Type.STRING).build());
        columnsMetadata.add(column().id(2).name("id").type(Type.INTEGER).build());
        columnsMetadata.add(column().id(3).name("birth").type(Type.DATE).build());
        RowMetadata rowMetadata = new RowMetadata(columnsMetadata);

        // when
        final Schema schema = AvroUtils.toSchema(rowMetadata);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
    }

    @Test
    public void shouldHandleDuplicatedColumnName() {
        // given
        List<ColumnMetadata> columnsMetadata = new ArrayList<>();
        columnsMetadata.add(column().id(1).name("name").type(Type.STRING).build());
        columnsMetadata.add(column().id(2).name("name").type(Type.INTEGER).build());
        columnsMetadata.add(column().id(3).name("name").type(Type.DATE).build());
        RowMetadata rowMetadata = new RowMetadata(columnsMetadata);

        // when
        final Schema schema = AvroUtils.toSchema(rowMetadata);

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
        ColumnMetadata columnMetadata = column().id(1).name("#@!abc$").type(Type.STRING).build();
        RowMetadata rowMetadata = new RowMetadata(Collections.singletonList(columnMetadata));

        // when
        Schema schema = AvroUtils.toSchema(rowMetadata);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(1, schema.getFields().size());
        assertEquals("DP___abc_", schema.getFields().get(0).name());
    }

    @Test
    public void shouldEscapeInvalidJavaCharacters_ifFirstIsValidItIsKept() {
        // given
        ColumnMetadata columnMetadata = column().id(1).name("1234").type(Type.STRING).build();
        RowMetadata rowMetadata = new RowMetadata(Collections.singletonList(columnMetadata));

        // when
        Schema schema = AvroUtils.toSchema(rowMetadata);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(1, schema.getFields().size());
        assertEquals("DP_1234", schema.getFields().get(0).name());
    }

    @Test
    public void shouldEscapeInvalidJavaCharacters_emptyIsNumbered() {
        // given
        ColumnMetadata columnMetadata = column().id(1).name("").type(Type.STRING).build();
        RowMetadata rowMetadata = new RowMetadata(Collections.singletonList(columnMetadata));

        // when
        Schema schema = AvroUtils.toSchema(rowMetadata);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(1, schema.getFields().size());
        assertEquals("DP_0001", schema.getFields().get(0).name());
    }

    @Test
    public void toDataSetRowConverter_shouldHandleDuplicate() {
        RowMetadata rowMetadata = new RowMetadata();
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("city").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("city").type(Type.STRING).build());
        columnMetadatas.add(column().id(3).name("city").type(Type.STRING).build());
        rowMetadata.setColumns(columnMetadatas);

        DataSetRow inputRow = new DataSetRow(rowMetadata);
        inputRow.set("0001", "value 1");
        inputRow.set("0002", "value 2");
        inputRow.set("0003", "value 3");
        inputRow.setTdpId(1L);

        Function<DataSetRow, GenericRecord> toAvro =
                AvroUtils.buildToGenericRecordConverter(AvroUtils.toSchema(rowMetadata));
        Function<GenericRecord, DataSetRow> converter = AvroUtils.buildToDataSetRowConverter(rowMetadata);

        DataSetRow dataSetRow = converter.apply(toAvro.apply(inputRow));

        assertEquals(inputRow, dataSetRow);
    }

    @Test
    public void toDataSetRowConverter_shouldHandleInvalidNames() {
        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata
                .setColumns(Collections.singletonList(column().id(2).name("date-of-birth").type(Type.STRING).build()));

        RowMetadata rowMetadataConverted = AvroUtils.toRowMetadata(AvroUtils.toSchema(rowMetadata));

        assertEquals(rowMetadata, rowMetadataConverted);
    }

}
