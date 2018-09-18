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

package org.talend.dataprep.api.dataset.statistics.date;

import java.util.ArrayList;
import java.util.List;

import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;
import org.talend.dataprep.date.DateManipulator;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DateHistogram implements Histogram {

    private static final long serialVersionUID = 1L;

    public static final String TYPE = "date";

    private final List<HistogramRange> items = new ArrayList<>();

    private DateManipulator.Pace pace;

    /**
     * The minimum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    @JsonIgnore
    private long minUTCEpochMilliseconds;

    /**
     * The maximum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    @JsonIgnore
    private long maxUTCEpochMilliseconds;

    @Override
    public List<HistogramRange> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DateHistogram))
            return false;

        DateHistogram that = (DateHistogram) o;

        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    public void setPace(DateManipulator.Pace pace) {
        this.pace = pace;
    }

    public DateManipulator.Pace getPace() {
        return pace;
    }

    /**
     *
     * @param minUTCEpochMilliseconds the minimum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    public void setMinUTCEpochMilliseconds(long minUTCEpochMilliseconds) {
        this.minUTCEpochMilliseconds = minUTCEpochMilliseconds;
    }

    /**
     *
     * @return the minimum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    public long getMinUTCEpochMilliseconds() {
        return minUTCEpochMilliseconds;
    }

    /**
     *
     * @param maxUTCEpochMilliseconds the maximum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    public void setMaxUTCEpochMilliseconds(long maxUTCEpochMilliseconds) {
        this.maxUTCEpochMilliseconds = maxUTCEpochMilliseconds;
    }

    /**
     *
     * @return the maximum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    public long getMaxUTCEpochMilliseconds() {
        return maxUTCEpochMilliseconds;
    }

}
