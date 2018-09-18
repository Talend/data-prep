// ============================================================================
//
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

package org.talend.dataprep.api.dataset.statistics;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Histogram item. It represents the range and its occurrences
 */
public class HistogramRange implements Serializable {

    /**
     * The number of element in the curent range
     */
    @JsonProperty("occurrences")
    private long occurrences = 0;

    /**
     * The range of this part of the histogram
     */
    @JsonProperty("range")
    private Range range = new Range();

    /**
     * Number of element getter
     *
     * @return The number of elements
     */
    public long getOccurrences() {
        return occurrences;
    }

    /**
     * Number of elements setter
     *
     * @param occurrences The new number of occurrences
     */
    public void setOccurrences(long occurrences) {
        this.occurrences = occurrences;
    }

    /**
     * Range getter
     *
     * @return The range
     */
    public Range getRange() {
        return range;
    }

    /**
     * Range setter
     *
     * @param range The new range
     */
    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistogramRange)) {
            return false;
        }
        final HistogramRange that = (HistogramRange) o;
        return occurrences == that.occurrences && range.equals(that.range);
    }

    @Override
    public int hashCode() {
        int result = (int) (occurrences ^ (occurrences >>> 32));
        result = 31 * result + range.hashCode();
        return result;
    }
}
