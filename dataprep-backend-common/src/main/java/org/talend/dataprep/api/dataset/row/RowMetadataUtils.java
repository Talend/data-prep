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

package org.talend.dataprep.api.dataset.row;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.api.type.Type.STRING;

public class RowMetadataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowMetadataUtils.class);

    private static final String DATAPREP_FIELD_PREFIX = "DP_";

    private static final String DP_COLUMN_ID = "_dp_column_id";

    private static final String DP_COLUMN_NAME = "_dp_column_name";

    private static final String DP_COLUMN_TYPE = "_dp_column_type";

    private RowMetadataUtils() {
    }

    public static Schema toSchema(RowMetadata rowMetadata) {
        return toSchema(rowMetadata.getColumns());
    }

    public static Schema toSchema(List<ColumnMetadata> columns) {
        final String name = "dataprep" + System.currentTimeMillis();
        final Map<String, Integer> uniqueSuffixes = new HashMap<>();
        final List<Schema.Field> fields = columns.stream() //
                .sorted(Comparator.comparingInt(c -> Integer.parseInt(c.getId()))) //
                .peek(columnMetadata -> {
                    final Integer suffix = uniqueSuffixes.get(columnMetadata.getName());
                    if (suffix != null) {
                        // Modify column name
                        uniqueSuffixes.put(columnMetadata.getName(), suffix + 1);
                        columnMetadata.setName(columnMetadata.getName() + '_' + suffix);
                    } else {
                        // Don't modify column name
                        uniqueSuffixes.put(columnMetadata.getName(), 1);
                    }
                }) //
                .map(RowMetadataUtils::toField) //
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

    private static Schema.Field toField(ColumnMetadata column) {
        final String name = StringUtils.isEmpty(column.getName()) ?
                DATAPREP_FIELD_PREFIX + column.getId() :
                toAvroFieldName(column);
        final Schema type = SchemaBuilder.builder().unionOf().nullBuilder().endNull().and().stringType().endUnion();
        final Schema.Field field = new Schema.Field(name, type, StringUtils.EMPTY, null);
        field.addProp(DP_COLUMN_ID, column.getId());
        field.addProp(DP_COLUMN_NAME, column.getName());
        field.addProp(DP_COLUMN_TYPE, column.getType());

        return field;
    }

    private static String toAvroFieldName(ColumnMetadata column) {
        final char[] chars = column.getName().toCharArray();
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

    /**
     * In case of a date column, return the most used pattern.
     *
     * @param column the column to inspect.
     * @return the most used pattern or null if there's none.
     */
    public static String getMostUsedDatePattern(ColumnMetadata column) {
        // only filter out non date columns
        if (Type.get(column.getType()) != Type.DATE) {
            return null;
        }
        final List<PatternFrequency> patternFrequencies = column.getStatistics().getPatternFrequencies();
        if (!patternFrequencies.isEmpty()) {
            patternFrequencies.sort((p1, p2) -> Long.compare(p2.getOccurrences(), p1.getOccurrences()));
            return patternFrequencies.get(0).getPattern();
        }
        return null;
    }

    private static Optional<ColumnMetadata> getColumnMetadata(Schema.Field field) {
        if (field.getProp(DP_COLUMN_ID) == null) {
            return Optional.of(column() //
                    .type(STRING) //
                    .id(field.pos()) //
                    .name(field.name()) //
                    .build());
        }
        return Optional.of(column() //
                .type(Type.get(field.getProp(DP_COLUMN_TYPE))) //
                .computedId(field.getProp(DP_COLUMN_ID)) //
                .name(field.getProp(DP_COLUMN_NAME)) //
                .build() //
        );
    }

    public static RowMetadata toRowMetadata(Schema schema) {
        RowMetadata rowMetadata = new RowMetadata();

        final List<ColumnMetadata> columns = schema.getFields() //
                .stream() //
                .map(RowMetadataUtils::getColumnMetadata) //
                .filter(Optional::isPresent) //
                .map(Optional::get) //
                .collect(Collectors.toList());
        rowMetadata.setColumns(columns);

        return rowMetadata;
    }

    public static DataSetRow toDataSetRow(Record record) {
        return toDataSetRow(record.getIndexedRecord(), record.getMetadata());
    }

    public static DataSetRow toDataSetRow(IndexedRecord indexedRecord, Metadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null.");
        }
        final List<Schema.Field> fields = indexedRecord.getSchema().getFields();

        final List<ColumnMetadata> newColumns = metadata.columns.stream() //
                .map(c -> ColumnMetadata.Builder.column().copy(c).build()) //
                .collect(Collectors.toList());

        DataSetRow row = new DataSetRow(new RowMetadata(newColumns));
        for (Schema.Field field : fields) {
            final String dpColumnId = field.getProp(DP_COLUMN_ID);
            if (dpColumnId != null) {
                row = row.set(dpColumnId, String.valueOf(indexedRecord.get(field.pos())));
            }
        }
        row.setTdpId(metadata.getRowId());
        return row;
    }

    public static Record toRecord(DataSetRow row) {
        final Schema schema = toSchema(row.getRowMetadata());
        final IndexedRecord record = new GenericData.Record(schema);

        final Map<String, Object> recordValues = row.values();
        for (Schema.Field field : schema.getFields()) {
            final String columnId = field.getProp(DP_COLUMN_ID);
            if (columnId != null) {
                if (recordValues.containsKey(columnId)) {
                    record.put(field.pos(), recordValues.get(columnId));
                } else {
                    LOGGER.warn("Unable to set column '{}'.", columnId);
                }
            }
        }

        final Metadata metadata = new Metadata(row.getTdpId(), row.getRowMetadata().getColumns());
        return new Record(record, metadata);
    }

    public static class Record {

        private final IndexedRecord indexedRecord;

        private final Metadata metadata;

        public Record(IndexedRecord indexedRecord, Metadata metadata) {
            this.indexedRecord = indexedRecord;
            this.metadata = metadata;
        }

        public IndexedRecord getIndexedRecord() {
            return indexedRecord;
        }

        public Metadata getMetadata() {
            return metadata;
        }
    }

    public static class Metadata implements Serializable {

        private final Long rowId;

        private final List<ColumnMetadata> columns;

        public Metadata(Long rowId, List<ColumnMetadata> columns) {
            this.rowId = rowId;
            this.columns = columns == null ? Collections.emptyList() : columns;
        }

        public Long getRowId() {
            return rowId;
        }

        public List<ColumnMetadata> getColumns() {
            return columns;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metadata metadata = (Metadata) o;
            return Objects.equals(rowId, metadata.rowId) &&
                    Objects.equals(columns, metadata.columns);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowId, columns);
        }
    }

}
