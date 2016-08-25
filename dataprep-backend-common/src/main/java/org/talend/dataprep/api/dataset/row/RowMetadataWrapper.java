package org.talend.dataprep.api.dataset.row;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;

public class RowMetadataWrapper extends RowMetadata {

    private transient Schema schema;

    public RowMetadataWrapper(Schema schema) {
        this.schema = schema;
        for (Schema.Field field : schema.getFields()) {
            addColumn(RowMetadataUtils.toColumnMetadata(field));
        }
    }

    @Override
    public void setColumns(List<ColumnMetadata> columnMetadata) {
        schema = RowMetadataUtils.toSchema(columnMetadata);
    }

    @Override
    public ColumnMetadata addColumn(ColumnMetadata columnMetadata) {
        super.addColumn(columnMetadata);
        schema = RowMetadataUtils.toSchema(this);
        return columnMetadata;
    }

    @Override
    public ColumnMetadata deleteColumnById(String id) {
        final ColumnMetadata columnMetadata = super.deleteColumnById(id);
        schema = RowMetadataUtils.toSchema(this);
        return columnMetadata;
    }

    @Override
    public int size() {
        return schema.getFields().size();
    }

    @Override
    public ColumnMetadata getById(String wantedId) {
        final List<Schema.Field> fields = schema.getFields();
        for (Schema.Field field : fields) {
            if (wantedId.equalsIgnoreCase(field.name().substring(RowMetadataUtils.DATAPREP_FIELD_PREFIX.length()))) {
                return RowMetadataUtils.toColumnMetadata(field);
            }
        }
        return null;
    }

    @Override
    public void update(@Nonnull String columnId, @Nonnull ColumnMetadata column) {
        super.update(columnId, column);
        schema = RowMetadataUtils.toSchema(this);
    }

    @Override
    public String insertAfter(@Nonnull String columnId, @Nonnull ColumnMetadata column) {
        final String newId = super.insertAfter(columnId, column);
        schema = RowMetadataUtils.toSchema(this);
        return newId;
    }

    @Override
    public RowMetadata clone() {
        return new RowMetadataWrapper(schema);
    }

    @Override
    public Schema toSchema() {
        return schema;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
