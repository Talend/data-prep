package org.talend.dataprep.api.dataset.statistics.date;

import org.junit.Test;
import org.talend.dataprep.api.dataset.statistics.Histogram;
import org.talend.dataprep.api.dataset.statistics.HistogramRange;
import org.talend.dataprep.date.DateManipulator;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static java.time.Month.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StreamDateHistogramStatisticsTest {
    @Test
    public void should_create_day_histogram() {
        //given
        final StreamDateHistogramStatistics stats = new StreamDateHistogramStatistics();
        stats.setNumberOfBins(4);

        stats.add(LocalDateTime.of(2015, JANUARY, 5, 5, 6, 7));
        stats.add(LocalDateTime.of(2015, JANUARY, 6, 0, 0, 0));
        stats.add(LocalDateTime.of(2015, JANUARY, 7, 12, 58, 0));
        stats.add(LocalDateTime.of(2015, JANUARY, 6, 2, 4, 45));
        stats.add(LocalDateTime.of(2015, JANUARY, 5, 3, 45, 2));
        stats.add(LocalDateTime.of(2015, JANUARY, 5, 9, 8, 3));

        //when
        final Histogram histogram = stats.getHistogram();

        //then
        assertThat(((DateHistogram) histogram).getPace(), is(DateManipulator.Pace.DAY));
        assertThat(histogram.getItems().size(), is(3));

        HistogramRange histoRange = histogram.getItems().get(0);
        assertThat((long) histoRange.getRange().getMin(), is(1420416000000L));
        assertThat((long) histoRange.getRange().getMax(), is(1420502400000L));
        assertThat(histoRange.getOccurrences(), is(3L));

        histoRange = histogram.getItems().get(1);
        assertThat((long) histoRange.getRange().getMin(), is(1420502400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1420588800000L));
        assertThat(histoRange.getOccurrences(), is(2L));

        histoRange = histogram.getItems().get(2);
        assertThat((long) histoRange.getRange().getMin(), is(1420588800000L));
        assertThat((long) histoRange.getRange().getMax(), is(1420675200000L));
        assertThat(histoRange.getOccurrences(), is(1L));
    }

    @Test
    public void should_create_month_histogram() {
        //given
        final StreamDateHistogramStatistics stats = new StreamDateHistogramStatistics();
        stats.setNumberOfBins(5);

        stats.add(LocalDateTime.of(2015, JANUARY, 5, 2, 4));
        stats.add(LocalDateTime.of(2015, JANUARY, 6, 4, 12));
        stats.add(LocalDateTime.of(2015, MARCH, 15, 16, 5));
        stats.add(LocalDateTime.of(2015, APRIL, 1, 10, 2));
        stats.add(LocalDateTime.of(2015, MARCH, 25, 14, 14));
        stats.add(LocalDateTime.of(2015, JANUARY, 5, 2, 25));
        stats.add(LocalDateTime.of(2015, JANUARY, 18, 4, 4));

        //when
        final Histogram histogram = stats.getHistogram();

        //then
        assertThat(((DateHistogram) histogram).getPace(), is(DateManipulator.Pace.MONTH));
        assertThat(histogram.getItems().size(), is(4));

        HistogramRange histoRange = histogram.getItems().get(0);
        assertThat((long) histoRange.getRange().getMin(), is(1420070400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1422748800000L));
        assertThat(histoRange.getOccurrences(), is(4L));

        histoRange = histogram.getItems().get(1);
        assertThat((long) histoRange.getRange().getMin(), is(1422748800000L));
        assertThat((long) histoRange.getRange().getMax(), is(1425168000000L));
        assertThat(histoRange.getOccurrences(), is(0L));

        histoRange = histogram.getItems().get(2);
        assertThat((long) histoRange.getRange().getMin(), is(1425168000000L));
        assertThat((long) histoRange.getRange().getMax(), is(1427846400000L));
        assertThat(histoRange.getOccurrences(), is(2L));

        histoRange = histogram.getItems().get(3);
        assertThat((long) histoRange.getRange().getMin(), is(1427846400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1430438400000L));
        assertThat(histoRange.getOccurrences(), is(1L));
    }

    @Test
    public void should_create_year_histogram() {
        //given
        final StreamDateHistogramStatistics stats = new StreamDateHistogramStatistics();
        stats.setNumberOfBins(4);

        stats.add(LocalDateTime.of(2013, JANUARY, 5, 8, 8));
        stats.add(LocalDateTime.of(2015, JANUARY, 6, 9, 9));
        stats.add(LocalDateTime.of(2015, MARCH, 15, 0, 0));
        stats.add(LocalDateTime.of(2013, APRIL, 1, 12, 12));
        stats.add(LocalDateTime.of(2015, MARCH, 25, 4, 10));
        stats.add(LocalDateTime.of(2015, JANUARY, 5, 7, 9));

        //when
        final Histogram histogram = stats.getHistogram();

        //then
        assertThat(((DateHistogram) histogram).getPace(), is(DateManipulator.Pace.YEAR));
        assertThat(histogram.getItems().size(), is(3));

        HistogramRange histoRange = histogram.getItems().get(0);
        assertThat((long) histoRange.getRange().getMin(), is(1356998400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1388534400000L));
        assertThat(histoRange.getOccurrences(), is(2L));

        histoRange = histogram.getItems().get(1);
        assertThat((long) histoRange.getRange().getMin(), is(1388534400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1420070400000L));
        assertThat(histoRange.getOccurrences(), is(0L));

        histoRange = histogram.getItems().get(2);
        assertThat((long) histoRange.getRange().getMin(), is(1420070400000L));
        assertThat((long) histoRange.getRange().getMax(), is(1451606400000L));
        assertThat(histoRange.getOccurrences(), is(4L));
    }

    @Test
    public void should_scale_to_geologic_time() {
        //given
        final StreamDateHistogramStatistics stats = new StreamDateHistogramStatistics();
        stats.setNumberOfBins(4);

        stats.add(LocalDateTime.of(-1_000_000, JANUARY, 5, 8, 8));
        stats.add(LocalDateTime.of(100, JANUARY, 6, 9, 9));
        stats.add(LocalDateTime.of(1000, MARCH, 15, 0, 0));
        stats.add(LocalDateTime.of(1500, APRIL, 1, 12, 12));
        stats.add(LocalDateTime.of(2000, MARCH, 25, 4, 10));
        stats.add(LocalDateTime.of(1_000_000, JANUARY, 5, 7, 9));

        //when
        final Histogram histogram = stats.getHistogram();

        //then
        // Assert that it scales, e.g. the pace should be for this example 10 thousands years
        // Assert that we have 3 bins
        // Assert also that there is no limit about pace, it should continue to scale
        //TODO implementation + assertion
    }
}
