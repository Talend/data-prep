package org.talend.dataprep.util.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.entity.ContentType;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.api.type.Type.STRING;

public class AvroUtils {

    /*
     * Avro mime types as specified at <a
     * href="http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport>http://avro.apache.org/docs/current/spec.html#HTTP+as+Transport</a>
     * and discussed at <a
     * href="https://issues.apache.org/jira/browse/AVRO-488>https://issues.apache.org/jira/browse/AVRO-488</a>
     */

    /** Avro binary  mime type as defined in spec but illegal. */
    private static final String AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID = "avro/binary";

    /** Preferred avro binary content-type. */
    public static final String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE = "application/x-avro-binary";

    public static final ContentType AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID =
            ContentType.create(AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE);

    /** Correct binary Avro mime type if ever registered. */
    private static final String AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-binary";

    /** Avro JSON mime type as defined in spec but illegal. */
    private static final String AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID = "avro/json";

    /** Preferred avro JSON mime type. */
    // TODO : should we add the charset?
    public static final String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE = "application/x-avro-json";

    public static final ContentType AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID =
            ContentType.create(AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE);

    /** Correct JSON Avro mime type if ever registered. */
    private static final String AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_REGISTERED = "application/avro-json";

    private static final String DATAPREP_FIELD_PREFIX = "DP_";

    private static final String DP_COLUMN_ID = "_dp_column_id";

    private static final String DP_COLUMN_NAME = "_dp_column_name";

    private static final String DP_COLUMN_TYPE = "_dp_column_type";

    private AvroUtils() {
    }

    /** Create a reader for a stream of binary encoded Avro records. */
    public static AvroReader readBinaryStream(InputStream rawContent, Schema schema) throws IOException {
        return new AvroReader(rawContent, schema, true);
    }

    /** Create a reader for a stream of JSON encoded Avro records. */
    public static AvroReader readJsonStream(InputStream rawContent, Schema schema) throws IOException {
        return new AvroReader(rawContent, schema, false);
    }

    /**
     *  Convert an {@link Schema Avro Schema} in a dataprep {@link RowMetadata} using any dataprep specific properties.
     */
    public static RowMetadata toRowMetadata(Schema schema) {
        RowMetadata rowMetadata = new RowMetadata();

        final List<ColumnMetadata> columns = schema.getFields() //
                .stream() //
                .map(AvroUtils::toColumnMetadata) //
                .collect(Collectors.toList());
        rowMetadata.setColumns(columns);
        return rowMetadata;
    }

    /** Convert a dataprep {@link RowMetadata} to an {@link Schema Avro Schema} adding dataprep specific properties. */
    public static Schema toSchema(RowMetadata rowMetadata) {
        final String name = "dataprep" + System.currentTimeMillis();
        return toSchema(rowMetadata, name);
    }

    /**
     * Convert a dataprep {@link RowMetadata} to a named {@link Schema Avro Schema} adding dataprep specific properties.
     */
    public static Schema toSchema(RowMetadata rowMetadata, String name) {
        final List<Schema.Field> fields = rowMetadata.getColumns().stream() //
                .map(new ColumnToAvroField()) //
                .collect(Collectors.toList());

        final Schema schema = Schema.createRecord( //
                name, //
                "a dataprep preparation", //
                "org.talend.dataprep", //
                false //
        );

        schema.setFields(fields);
        return schema;
    }

    /** Build a converter to streamingly convert {@link GenericRecord} in {@link DataSetRow}. */
    public static Function<GenericRecord, DataSetRow> getToDataSetRowConverter(RowMetadata dataSetMetadata) {
        return new AvroToDatasetRow(dataSetMetadata);
    }

    /** Build a converter to streamingly convert {@link DataSetRow} in {@link GenericRecord}. */
    public static Function<DataSetRow, GenericRecord> getToGenericRecordConverter(Schema schema) {
        return dsr -> toGenericRecord(schema, dsr);
    }

    private static ColumnMetadata toColumnMetadata(Schema.Field field) {
        if (field.getProp(DP_COLUMN_ID) == null) {
            return column() //
                    .type(STRING) //
                    .id(field.pos()) //
                    .name(field.name()) //
                    .build();
        }
        return column() //
                .type(Type.get(field.getProp(DP_COLUMN_TYPE))) //
                .computedId(field.getProp(DP_COLUMN_ID)) //
                .name(field.getProp(DP_COLUMN_NAME)) //
                .build();
    }

    private static GenericRecord toGenericRecord(Schema schema, DataSetRow dsr) {
        GenericData.Record record = new GenericData.Record(schema);
        final Iterator<Object> iterator = dsr.order().values().values().iterator();
        for (int j = 0; j < schema.getFields().size() && iterator.hasNext(); j++) {
            record.put(j, iterator.next());
        }
        return record;
    }

    /**
     * Converter from {@link ColumnMetadata} to {@link Schema.Field}. It handles a stream of column metadata as a single
     * schema to avoid name duplication and normalize name to avro conventions.
     */
    private static class ColumnToAvroField implements Function<ColumnMetadata, Schema.Field> {

        final Map<String, Integer> uniqueSuffixes = new HashMap<>();

        @Override
        public Schema.Field apply(ColumnMetadata column) {
            String fieldName = column.getName();
            // normalize to Avro conventions
            fieldName = StringUtils.isEmpty(fieldName) ?
                    DATAPREP_FIELD_PREFIX + column.getId() :
                    toAvroFieldName(fieldName);

            // handle duplicates
            final Integer suffix = uniqueSuffixes.get(fieldName);
            if (suffix != null) {
                // Modify column name
                uniqueSuffixes.put(fieldName, suffix + 1);
                fieldName = fieldName + '_' + suffix;
            } else {
                // Don't modify column name
                uniqueSuffixes.put(fieldName, 1);
            }

            final Schema type = SchemaBuilder.builder().unionOf().nullBuilder().endNull().and().stringType().endUnion();

            final Schema.Field field = new Schema.Field(fieldName, type, EMPTY, ((Object) null));
            field.addProp(DP_COLUMN_ID, column.getId());
            field.addProp(DP_COLUMN_NAME, column.getName());
            field.addProp(DP_COLUMN_TYPE, column.getType());

            return field;
        }

        private static String toAvroFieldName(String dataprepColumnName) {
            final char[] chars = dataprepColumnName.toCharArray();
            final StringBuilder columnName = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                final char currentChar = chars[i];
                if (i == 0) {
                    if (!Character.isLetter(currentChar)) {
                        columnName.append(DATAPREP_FIELD_PREFIX);
                    } else if (!Character.isJavaIdentifierPart(currentChar)) {
                        columnName.append('_');
                    } else {
                        columnName.append(currentChar);
                    }
                }
                if (i > 0) {
                    if (!Character.isJavaIdentifierPart(currentChar)) {
                        columnName.append('_');
                    } else {
                        columnName.append(currentChar);
                    }
                }
            }
            return columnName.toString();
        }
    }

    /**
     * Converter from Avro generic records to DataSetRows.
     */
    private static class AvroToDatasetRow implements Function<GenericRecord, DataSetRow> {

        private final RowMetadata rowMetadata;

        private List<Pair<String, String>> dpToAvroId;

        private long rowId = 1;

        AvroToDatasetRow(RowMetadata rowMetadata) {
            this.rowMetadata = rowMetadata;
            List<ColumnMetadata> columns = rowMetadata.getColumns();
            dpToAvroId = columns.stream() //
                    .map(new ColumnToAvroField()) //
                    .map(f -> new ImmutablePair<>(f.getProp(DP_COLUMN_ID), f.name()))
                    .collect(Collectors.toList());
        }

        @Override
        public DataSetRow apply(GenericRecord nextRecord) {
            DataSetRow dataSetRow = new DataSetRow(rowMetadata);
            for (Pair<String, String> dpToAvroIds : dpToAvroId) {
                dataSetRow.set(dpToAvroIds.getKey(), toStringValue(nextRecord, dpToAvroIds.getValue()));
            }
            dataSetRow.setTdpId(rowId++);
            return dataSetRow;
        }

        /**
         * From Avro value to dataprep string.
         *
         * @param currentRecord avro record
         * @param fieldName
         * @return row value
         */
        private static String toStringValue(GenericRecord currentRecord, String fieldName) {
            final Schema fieldSchema = currentRecord.getSchema().getField(fieldName).schema();
            Object recordFieldValue = currentRecord.get(fieldName);
            return convertAvroFieldToString(fieldSchema, recordFieldValue);
        }

        private static String convertAvroFieldToString(Schema fieldSchema, Object recordFieldValue) {
            final String result;
            switch (fieldSchema.getType()) {
            case BYTES:
                result = new String(((ByteBuffer) recordFieldValue).array());
                break;
            case UNION:
                String unionValue = EMPTY;
                Iterator<Schema> iterator = fieldSchema.getTypes().iterator();
                while (EMPTY.equals(unionValue) && iterator.hasNext()) {
                    Schema schema = iterator.next();
                    unionValue = convertAvroFieldToString(schema, recordFieldValue);
                }
                result = unionValue;
                break;
            case STRING:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
            case ENUM:
                result = String.valueOf(recordFieldValue);
                break;
            case NULL:
                result = EMPTY;
                break;
            default: // RECORD, ARRAY, MAP, FIXED
                result = "Data Preparation cannot interpret this value";
                break;
            }
            return result;
        }
    }
}
