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

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

/**
 * Unit test for the org.talend.dataprep.api.dataset.row.RowMetadataUtils class.
 *
 * @see RowMetadataUtils
 */
public class RowMetadataUtilsTest {

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
