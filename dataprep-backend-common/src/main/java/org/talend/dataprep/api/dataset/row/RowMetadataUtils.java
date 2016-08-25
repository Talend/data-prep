// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;

import com.fasterxml.jackson.databind.ObjectMapper;

// FHU TODO Add test for schema conversion
public class RowMetadataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowMetadataUtils.class);

    public static final String DATAPREP_FIELD_PREFIX = "DP_";

    private RowMetadataUtils() {
    }

    public static ColumnMetadata toColumnMetadata(Schema.Field field) {
        return new ColumnMetadataWrapper(field);
    }

    public static RowMetadata toRowMetadata(Schema schema) {
        return new RowMetadataWrapper(schema);
    }

    public static Schema toSchema(RowMetadata rowMetadata) {
        return toSchema(rowMetadata.getColumns());
    }

    public static Schema toSchema(List<ColumnMetadata> columns) {
        List<Schema.Field> fields = columns.stream() //
                .map(RowMetadataUtils::toField) //
                .collect(Collectors.toList());
        return Schema.createRecord(fields);
    }

    public static Schema.Field toField(ColumnMetadata column) {
        final String name = column.getName() == null ? DATAPREP_FIELD_PREFIX + column.getId() : toAvroFieldName(column);
        final Schema.Field field = new Schema.Field(name, Schema.create(Schema.Type.STRING),
                StringUtils.EMPTY, null);
        try {
            final StringWriter writer = new StringWriter();
            new ObjectMapper().writerWithType(ColumnMetadata.class).writeValue(writer, column);
            field.addProp("_dp_column", writer.toString());
        } catch (IOException e) {
            LOGGER.error("Unable to convert to field.", e);
        }
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
                }
                columnName.append(currentChar);
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
}
