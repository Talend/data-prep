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

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;

import java.util.List;

public class RowMetadataUtils {

    private RowMetadataUtils() {
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
