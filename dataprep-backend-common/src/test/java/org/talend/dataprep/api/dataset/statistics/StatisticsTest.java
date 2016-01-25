package org.talend.dataprep.api.dataset.statistics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class StatisticsTest {

    Statistics statistics;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        statistics = mapper.readerFor(Statistics.class).readValue(StatisticsTest.class.getResourceAsStream("statistics.json"));
    }

    @Test
    public void testEquals() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final Statistics other = mapper.readerFor(Statistics.class).readValue(StatisticsTest.class.getResourceAsStream("statistics.json"));
        assertEquals(true, statistics.equals(other));
    }

    @Test
    public void testHashCode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final Statistics other = mapper.readerFor(Statistics.class).readValue(StatisticsTest.class.getResourceAsStream("statistics.json"));
        assertEquals(statistics.hashCode(), other.hashCode());
    }

    @Test
    public void testGetCount() throws Exception {
        assertEquals(65, statistics.getCount());
    }

    @Test
    public void testGetValid() throws Exception {
        assertEquals(65, statistics.getValid());
    }

    @Test
    public void testGetInvalid() throws Exception {
        assertEquals(0, statistics.getInvalid());
    }

    @Test
    public void testGetEmpty() throws Exception {
        assertEquals(0, statistics.getEmpty());
    }

    @Test
    public void testGetMax() throws Exception {
        assertEquals(Double.NaN, statistics.getMax(), 0);
    }

    @Test
    public void testGetMin() throws Exception {
        assertEquals(Double.NaN, statistics.getMin(), 0);
    }

    @Test
    public void testGetMean() throws Exception {
        assertEquals(Double.NaN, statistics.getMean(), 0);
    }

    @Test
    public void testGetVariance() throws Exception {
        assertEquals(Double.NaN, statistics.getVariance(), 0);
    }

    @Test
    public void testGetDuplicateCount() throws Exception {
        assertEquals(17, statistics.getDuplicateCount(), 0);
    }

    @Test
    public void testGetDistinctCount() throws Exception {
        assertEquals(48, statistics.getDistinctCount(), 0);
    }

    @Test
    public void testGetDataFrequencies() throws Exception {
        assertEquals(2, statistics.getDataFrequencies().size());
        assertEquals("", statistics.getDataFrequencies().get(0).data);
        assertEquals(18, statistics.getDataFrequencies().get(0).occurrences);
        assertEquals("2014-11-03 13:32:13", statistics.getDataFrequencies().get(1).data);
        assertEquals(1, statistics.getDataFrequencies().get(1).occurrences);
    }

    @Test
    public void testGetPatternFrequencies() throws Exception {
        assertEquals(2, statistics.getPatternFrequencies().size());
        assertEquals("MM/dd/yyyy", statistics.getPatternFrequencies().get(0).getPattern());
        assertEquals(47, statistics.getPatternFrequencies().get(0).occurrences);
        assertEquals("MM-dd-yy", statistics.getPatternFrequencies().get(1).getPattern());
        assertEquals(27, statistics.getPatternFrequencies().get(1).occurrences);
    }

    @Test
    public void testGetQuantiles() throws Exception {
        assertEquals(Double.NaN, statistics.getQuantiles().getMedian(), 0);
        assertEquals(Double.NaN, statistics.getQuantiles().getLowerQuantile(), 0);
        assertEquals(Double.NaN, statistics.getQuantiles().getUpperQuantile(), 0);
    }

    @Test
    public void testGetHistogram() throws Exception {
        final List<HistogramRange> histogramRanges = statistics.getHistogram().getItems();
        assertEquals(2, histogramRanges.size());
        assertEquals(1.0, histogramRanges.get(0).getRange().getMin(), 0);
        assertEquals(1.5, histogramRanges.get(0).getRange().getMax(), 0);
        assertEquals(1, histogramRanges.get(0).getOccurrences());
        assertEquals(2.0, histogramRanges.get(1).getRange().getMin(), 0);
        assertEquals(2.5, histogramRanges.get(1).getRange().getMax(), 0);
        assertEquals(1, histogramRanges.get(1).getOccurrences());
    }

    @Test
    public void testGetTextLengthSummary() throws Exception {
        assertEquals(20, statistics.getTextLengthSummary().getAverageLength(), 0);
        assertEquals(30, statistics.getTextLengthSummary().getMaximalLength(), 0);
        assertEquals(10, statistics.getTextLengthSummary().getMinimalLength(), 0);
    }

}