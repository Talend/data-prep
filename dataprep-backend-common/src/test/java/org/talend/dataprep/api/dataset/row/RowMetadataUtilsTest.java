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

import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the org.talend.dataprep.api.dataset.row.RowMetadataUtils class.
 *
 * @see RowMetadataUtils
 */
public class RowMetadataUtilsTest {

    @Test
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

    @Test
    public void shouldHandleDuplicatedColumnName() {
        // given
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("name").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("name").type(Type.INTEGER).build());
        columnMetadatas.add(column().id(3).name("name").type(Type.DATE).build());

        // when
        final Schema schema = RowMetadataUtils.toSchema(columnMetadatas);

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
        final Schema schema = RowMetadataUtils.toSchema(columnMetadatas);

        // then
        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertEquals(1, schema.getFields().size());
        assertEquals("DP___abc", schema.getFields().get(0).name());
    }

    @Test
    public void shouldGetMostUsedDatePattern() {
        // given
        final ColumnMetadata columnMetadata = column().id(1).name("date").type(Type.DATE).build();
        final List<PatternFrequency> patternFrequencies = columnMetadata.getStatistics().getPatternFrequencies();
        patternFrequencies.add(new PatternFrequency("MM-dd-YYYY", 2));
        patternFrequencies.add(new PatternFrequency("dd-YYYY", 4));

        // when
        final String mostUsedDatePattern = RowMetadataUtils.getMostUsedDatePattern(columnMetadata);

        // then
        assertEquals("dd-YYYY", mostUsedDatePattern);
    }

    @Test
    public void shouldGetMostUsedDatePattern_stringColumn() {
        // given
        final ColumnMetadata columnMetadata = column().id(1).name("date").type(Type.STRING).build();

        // when
        final String mostUsedDatePattern = RowMetadataUtils.getMostUsedDatePattern(columnMetadata);

        // then
        assertNull(mostUsedDatePattern);
    }

    @Test
    public void shouldGetMostUsedDatePattern_noPattern() {
        // given
        final ColumnMetadata columnMetadata = column().id(1).name("date").type(Type.DATE).build();

        // when
        final String mostUsedDatePattern = RowMetadataUtils.getMostUsedDatePattern(columnMetadata);

        // then
        assertNull(mostUsedDatePattern);
    }


}
