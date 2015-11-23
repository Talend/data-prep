package org.talend.dataprep.api.dataset.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.talend.dataquality.statistics.numeric.histogram.Range;

public class StreamHistogramStatisticsTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNegativeOrZeroNumberOfBins() {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNumberOfBinsNotPowerOfTwo() {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(3);
    }

    @Test
    public void shouldBConsistentWhenNoValueAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(32);
        // expected
        assertEquals(0, histogram.getNumberOfValues());
        assertEquals(0, histogram.getMean(), 0);
        assertEquals(0, histogram.getMin(), 0);
        assertEquals(0, histogram.getMax(), 0);
        assertEquals(32, histogram.getNumberOfBins());
    }

    @Test
    public void shouldBeConsistentWhenOneValueAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(16);

        // when
        histogram.add(1);
        // expected
        assertEquals(1, histogram.getNumberOfValues());
        assertEquals(1, histogram.getMean(), 0);
        assertEquals(1, histogram.getMin(), 0);
        assertEquals(1, histogram.getMax(), 0);
        assertEquals(16, histogram.getNumberOfBins());
        assertEquals(1, histogram.getHistogram().size());

        ArrayList<Range> ranges = new ArrayList<>(histogram.getHistogram().keySet());
        Range min = ranges.get(0);
        assertEquals(1, min.getLower(), 0);
        assertEquals(1, min.getUpper(), 0);

    }

    @Test
    public void shouldBeConsistentWhenAHundredValuesFromOneToOneHundredAreAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(16);
        // when
        for (int i = 1; i <= 100; i++) {
            histogram.add(i);
        }
        // expected
        assertEquals(100, histogram.getNumberOfValues());
        assertEquals(50.5, histogram.getMean(), 0);
        assertEquals(1, histogram.getMin(), 0);
        assertEquals(100, histogram.getMax(), 0);
        assertEquals(16, histogram.getNumberOfBins());
        // assertEquals(histogram.getHistogram().size(), 16);

        Collection<Long> counts = histogram.getHistogram().values();
        long sum = 0;
        for (Long l : counts) {
            sum += l;
        }
        assertEquals(sum, histogram.getNumberOfValues());
    }

    @Test
    public void shouldBeConsistentWhenAHundredValuesFromOneHundredToOneAreAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(32);
        for (int i = 100; i >= 1; i--) {
            histogram.add(i);
        }
        // expected
        assertEquals(100, histogram.getNumberOfValues());
        assertEquals(50.5, histogram.getMean(), 0);
        assertEquals(1, histogram.getMin(), 0);
        assertEquals(100, histogram.getMax(), 0);
        assertEquals(32, histogram.getNumberOfBins());
        // assertEquals(histogram.getHistogram().size(), 32);

        Collection<Long> counts = histogram.getHistogram().values();
        long sum = 0;
        for (Long l : counts) {
            sum += l;
        }
        assertEquals(sum, histogram.getNumberOfValues());
    }

    @Test
    public void shouldBeConsistentWhenAHundredValuesUnorderedFromOneHundredToOneAreAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(2);
        int[] array = { 48, 49, 50, 51, 32, 33, 34, 35, 96, 97, 98, 100, 15, 16, 17, 18, 91, 90, 92, 93, 94, 95, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 11, 12, 13, 14, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 36, 37, 38, 39, 40, 41, 42,
                43, 44, 45, 46, 47, 52, 53, 54, 55, 56, 57, 58, 59, 19, 99 };
        for (int i : array) {
            histogram.add(i);
        }
        // expected
        assertEquals(100, histogram.getNumberOfValues());
        assertEquals(50.5, histogram.getMean(), 0);
        assertEquals(1, histogram.getMin(), 0);
        assertEquals(100, histogram.getMax(), 0);
        assertEquals(2, histogram.getNumberOfBins());

        Collection<Long> counts = histogram.getHistogram().values();
        long sum = 0;
        for (Long l : counts) {
            sum += l;
        }
        assertEquals(sum, histogram.getNumberOfValues());
    }

    @Test
    public void shouldHaveConsistentRangesWhenAThousandValuesAreAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(32);
        for (int i = 1000; i >= 1; i--) {
            histogram.add(i);
        }
        // expected
        Range previousRange = null;
        for (Range range : histogram.getHistogram().keySet()) {
            if (previousRange != null) {
                assertEquals(previousRange.getUpper(), range.getLower(), 0);
            }
            previousRange = range;
        }
    }

    @Test
    public void shouldHaveSameMinWhenMinIsAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(4);
        histogram.add(1);
        double min = histogram.getMin();
        // when
        histogram.add(1);

        // expected
        assertEquals(min, histogram.getMin(), 0);
    }

    @Test
    public void shouldHaveSameMaxWhenMaxIsAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(4);
        histogram.add(1);
        double max = histogram.getMin();
        // when
        histogram.add(1);

        // expected
        assertEquals(max, histogram.getMin(), 0);
    }

    @Test
    public void shouldHaveMinMaxAndMeanWhenAValueIsAdded() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(4);
        histogram.add(10);

        // expected
        assertEquals(10, histogram.getMin(), 0);
        assertEquals(10, histogram.getMax(), 0);
        assertEquals(10, histogram.getMean(), 0);
    }

    @Test
    public void shouldHaveRightBoundaries() throws Exception {
        // given
        final StreamHistogramStatistics histogram = new StreamHistogramStatistics();
        histogram.setNumberOfBins(2);
        histogram.add(2);
        histogram.add(3);
        histogram.add(0);
        histogram.add(4);

        // expected
        ArrayList<Range> ranges = new ArrayList<>(histogram.getHistogram().keySet());
        Range min = ranges.get(0);
        Range max = ranges.get(ranges.size() - 1);

        assertTrue(min.compareTo(new Range(0, 4)) == 0);
        assertTrue(max.compareTo(new Range(4, 8)) == 0);
    }

}
