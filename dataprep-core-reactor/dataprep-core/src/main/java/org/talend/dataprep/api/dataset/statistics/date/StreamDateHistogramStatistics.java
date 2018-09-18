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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;
import org.talend.dataprep.api.dataset.statistics.Range;
import org.talend.dataprep.date.DateManipulator;

/**
 * Date statistics. It calculates the occurrences for each {@link DateManipulator.Pace}.
 * A pace is removed if it no longer fit the max bins number rule.
 * The final pace is defined at the end since it is based on the min and max value.
 */
public class StreamDateHistogramStatistics {

    /**
     * The default bin number which is 16.
     */
    private static final int DEFAULT_BIN_NUMBER = 16;

    /**
     * The maximum number of buckets.
     */
    private int numberOfBins = DEFAULT_BIN_NUMBER;

    /**
     * The bins per pace. For each pace, a bins that match the date is created.
     */
    private final Map<DateManipulator.Pace, Map<Range, Long>> bins = new HashMap<>();

    /**
     * The minimum limit value.
     */
    private LocalDateTime min;

    /**
     * The maximum limit value.
     */
    private LocalDateTime max;

    /**
     * Constructor.
     * It initialize the bins for each {@link DateManipulator.Pace}.
     */
    public StreamDateHistogramStatistics() {
        Arrays.stream(DateManipulator.Pace.values()).forEach(pace -> bins.put(pace, new HashMap<>()));
    }

    /**
     * Add the specified value in each pace's bins.
     *
     * @param date the value to add to this histogram.
     */
    public void add(final LocalDateTime date) {
        try (Stream<DateManipulator.Pace> stream = Arrays.stream(DateManipulator.Pace.values())) {
            stream.forEach(pace -> add(pace, date));
        }
        refreshLimits(date);
    }

    /**
     * Add a date in a specific pace's bin.
     * When the number of bin in this pace exceed the max number of bins, the bins are erased. So if the pace's bin
     * does not exist, it is not updated with the new value.
     *
     * @param pace The pace where to add the date.
     * @param date The date to add.
     */
    private void add(final DateManipulator.Pace pace, final LocalDateTime date) {
        final Map<Range, Long> paceBins = bins.get(pace);

        // pace has been removed, we skip the add on this pace
        if (paceBins == null) {
            return;
        }

        final LocalDateTime startDate = DateManipulator.getSuitableStartingDate(date, pace);
        final LocalDateTime endDate = DateManipulator.getNext(startDate, pace);

        final long startTimestamp = DateManipulator.getUTCEpochMilliseconds(startDate);
        final long endTimestamp = DateManipulator.getUTCEpochMilliseconds(endDate);

        final Range range = new Range(startTimestamp, endTimestamp);
        final Long nbInBin = paceBins.get(range);
        paceBins.put(range, (nbInBin != null ? nbInBin : 0L) + 1);

        // the bins exceed maximum number of bins, we remove this pace
        if (paceBins.size() > numberOfBins) {
            bins.remove(pace);
        }
    }

    /**
     * Refresh the min/max limits date depending on the provided date.
     *
     * @param date The date to take into account.
     */
    private void refreshLimits(final LocalDateTime date) {
        if (min == null || date.isBefore(min)) {
            min = date;
        }
        if (max == null || date.isAfter(max)) {
            max = date;
        }
    }

    /**
     * Get histograms
     *
     * @return the histogram
     * Note that the returned ranges are in pattern of [Min, Min+Pace[ - [Min+Pace, Min+Pace*2[ - ...[Max-binSize,Max[.
     */
    public Histogram getHistogram() {
        final DateHistogram histogram = new DateHistogram();
        if (min == null) {
            return histogram;
        }

        final DateManipulator.Pace pace = DateManipulator.getSuitablePace(min, max, numberOfBins);
        final Map<Range, Long> paceBin = bins.get(pace);
        if (paceBin == null) {
            return histogram;
        }

        histogram.setPace(pace);
        LocalDateTime nextRangeStart = DateManipulator.getSuitableStartingDate(min, pace);

        while (max.isAfter(nextRangeStart) || max.equals(nextRangeStart)) {
            final LocalDateTime rangeStart = nextRangeStart;
            final LocalDateTime rangeEnd = DateManipulator.getNext(nextRangeStart, pace);

            final long rangeStartTimestamp = DateManipulator.getUTCEpochMilliseconds(rangeStart);
            final long rangeEndTimestamp = DateManipulator.getUTCEpochMilliseconds(rangeEnd);

            final Range range = new Range(rangeStartTimestamp, rangeEndTimestamp);
            final Long rangeValue = paceBin.get(range);

            final HistogramRange dateRange = new HistogramRange();
            dateRange.setRange(range);
            dateRange.setOccurrences(rangeValue != null ? rangeValue : 0L);
            histogram.getItems().add(dateRange);

            nextRangeStart = rangeEnd;
        }
        // set min and max
        histogram.setMinUTCEpochMilliseconds(minUTCEpochMilliseconds());
        histogram.setMaxUTCEpochMilliseconds(maxUTCEpochMilliseconds());

        return histogram;
    }

    /**
     * Set number of bins in histogram. Number must be a positive integer and a power of 2.
     *
     * @param numberOfBins the number of regulars of this histogram. Value must be a positive integer.
     */
    public void setNumberOfBins(int numberOfBins) {
        if (numberOfBins <= 0) {
            throw new IllegalArgumentException("The number of bin must be a positive integer");
        }
        this.numberOfBins = numberOfBins;
    }

    /**
     *
     * @return the minimum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    private long minUTCEpochMilliseconds() {
        return DateManipulator.getUTCEpochMilliseconds(min);
    }

    /**
     *
     * @return the maximum date added to this histogram (in milliseconds since EPOCH in UTC)
     */
    private long maxUTCEpochMilliseconds() {
        return DateManipulator.getUTCEpochMilliseconds(max);
    }
}
