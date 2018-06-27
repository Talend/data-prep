//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.dataset.statistics.number;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.IntStream;

import org.junit.Test;
import org.talend.dataquality.statistics.numeric.histogram.Range;

public class StreamNumberHistogramStatisticsTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNegativeOrZeroNumberOfBins() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        histogram.setNumberOfBins(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNumberOfBinsNotPowerOfTwo() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        histogram.setNumberOfBins(3);
    }

    @Test
    public void shouldBConsistentWhenNoValueAdded() throws Exception {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
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

    @Test
    public void shouldHaveAccurateBinSizeWhenUsingValuesWithinSuccessiveIntegers() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        histogram.setNumberOfBins(2);
        histogram.add(0.03);
        histogram.add(0.035);
        histogram.add(0.045);
        histogram.add(0.04);
        histogram.add(0.05);

        // expected
        ArrayList<Range> ranges = new ArrayList<>(histogram.getHistogram().keySet());
        Range min = ranges.get(0);
        Range max = ranges.get(ranges.size() - 1);

        assertTrue(min.compareTo(new Range(0.03, 0.045625)) == 0);
        assertTrue(max.compareTo(new Range(0.045625, 0.06125)) == 0);
    }

    @Test
    public void shouldHaveIntegerBinSizeWhenBinSizeExceedOne() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        histogram.setNumberOfBins(2);
        histogram.add(0);
        histogram.add(1);
        histogram.add(2);
        histogram.add(3);
        histogram.add(4);

        // expected
        ArrayList<Range> ranges = new ArrayList<>(histogram.getHistogram().keySet());
        Range min = ranges.get(0);
        Range max = ranges.get(ranges.size() - 1);

        assertTrue(min.compareTo(new Range(0, 4)) == 0);
        assertTrue(max.compareTo(new Range(4, 8)) == 0);
    }

    /**
     * See https://jira.talendforge.org/browse/TDP-4027
     */
    @Test(timeout = 5000L)
    public void shouldDealWithIntegerOverflow() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        double[] array = { 110000000003016d, 110000000001315d, 110000000007584d, 110000000000097d, 110000000000098d,
                110000000000099d, 110000000000100d, 110000000000101d, 110000000000102d, 110000000003847d, 110000000002272d,
                110000000002670d, 110000000002671d, 110000000002672d, 110000000002673d, 110000000002700d, 110000000003262d,
                110000000003263d, 110000000003264d, 110000000003265d, 110000000007711d, 110000000007712d, 110000000000632d,
                110000000000632d, 110000000002677d, 110000000002678d, 110000000002708d, 110000000002710d, 110000000007644d,
                110000000004115d, 110000000004116d, 110000000002020d, 110000000004010d, 110000000004011d, 110000000004012d,
                110000000004013d, 110000000004014d, 110000000004015d, 110000000004016d, 110000000004017d, 110000000001722d,
                110000000003599d, 110000000002264d, 110000000004288d, 110000000002390d, 110000000002390d, 110000000002390d,
                110000000002417d, 110000000002418d, 110000000002419d, 110000000004286d, 110000000004281d, 110000000001774d,
                110000000002135d, 110000000002839d, 110000000000105d, 6196 };

        // when
        for (double i : array) {
            histogram.add(i);
        }

        // then
        assertEquals(57, histogram.getNumberOfValues());
        assertEquals(1.080701754415868E14, histogram.getMean(), 0);
        assertEquals(6196.0, histogram.getMin(), 0);
        assertEquals(1.10000000007712E14, histogram.getMax(), 0);
        assertEquals(32, histogram.getNumberOfBins());
    }

    // TDP-5987 Manage infinity values in dataSet cells
    @Test(timeout = 5000L)
    public void shouldManageInfinity() {
        // given
        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        IntStream.range(0, 101).forEach(histogram::add);
        histogram.add(Double.POSITIVE_INFINITY);

        // expected
        assertEquals(101, histogram.getNumberOfValues());
        assertEquals(50.0, histogram.getMean(), 0);
        assertEquals(0.0, histogram.getMin(), 0);
        assertEquals(100.0, histogram.getMax(), 0);
        assertEquals(32, histogram.getNumberOfBins());

        ArrayList<Range> ranges = new ArrayList<>(histogram.getHistogram().keySet());
        assertEquals(26, ranges.size());
        int i = 0;
        for (Range range: ranges) {
            assertEquals(i * 4, range.getLower(), 0.1);
            assertEquals(++i * 4, range.getUpper(), 0.1);
        }
    }


    @Test
    public void test_ShouldHandleLargeDoubleValue_TDQ_14216() {
        double[] values = new double[] { 72198.0, 7991950.0, 3.475323329569707E66, 4.396083155691154E27, 41.0,
                1.6075987281615199E75, 550.0, 4182584.0, 524.0, 269.0, 183.0, 521.0, 47.0, 6.407941754374855E31,
                1.498966653368336E28, 2.0, 13.0, 86.0, 76.0, 614.0, 8.142234265275721E19, 131.0, 6.310128323602232E55,
                15.0, 1.5827630573546136E25, 2.531684377025742E15, 2.2410456214174434E29, 917.0, 2.6716580898775493E85,
                495.0, 424.0, 5.378968161947957E42, 5.81617964E8, 1.85381838393095E65, 764.0, 700.0, 3.508718456E9,
                331.0, 5419688.0, 3.918635686283509E62, 45.0, 3.435631060454532E25, 7.988557543373271E24, 127.0, 904.0,
                956.0, 4.0, 5.943121614498576E43, 808.0, 8.580685006748972E23, 1.1355600021759779E33, 34.0,
                2.599806368419402E27, 557.0, 16.0, 245.0, 3.2039733254E10, 9823378.0, 40.0, 179.0, 9920342.0, 118.0,
                87.0, 7.67338162536E11, 84.0, 47.0, 376.0, 94.0, 7.948720640334525E50, 4.49611305E9, 30.0, 47908.0,
                564.0, 833.0, 119.0, 7222.0, 71.0, 441.0, 738.0, 55.0, 522.0, 93.0, 98.0, 452.0, 522180.0, 50.0, 431.0,
                8.6909037928E10, 1.010391217522E12, 2.301075757576416E34, 73.0, 4.0, 386.0, 3.5416798288095646E22,
                3.13363828E8, 5.69380203824883E43, 571.0, 877.0, 93.0, 9.675628114E9, 19.0, 54.0, 53.0, 931.0, 703.0,
                61.0, 8.711017235835673E46, 8.319202586565984E25, 5.587108965973643E43, 461.0, 99.0, 619.0,
                2.60548818E8, 4.5225501596115746E52, 5.5099692855056589E18, 5.2707619427382E13, 870.0,
                4.73457089995558E14, 2392324.0, 305732.0, 7.929836198592712E39, 66.0, 744.0, 7.906456738319738E45, 45.0,
                64.0, 6.6312268E7, 3.319753324606348E22, 4.7023101293671324E21, 77.0, 553.0, 7.479359501410888E26, 10.0,
                6.005374415694628E52, 9.8362196E7, 8.543723933209155E53, 8.289699617503132E35, 51.0,
                9.687285882747374E28, 194.0, 4.8025374E7, 5.478153764005704E25, 425.0, 8.90327081703358E54, 319.0, 37.0,
                56.0, 5.676737214034868E21, 489.0, 49.0, 36.0, 358.0, 908.0, 20.0, 7.2692624E7, 9.61093313384E11, 346.0,
                4.21579611811756E27, 222.0, 4.17687911038214E51, 2.9086009101109986E40, 467.0, 397.0,
                4.6841868978502356E42, 9.158130724547904E23, 26.0, 823.0, 548.0, 678130.0, 341.0, 8.445680084669625E72,
                357.0, 5.969027099976196E28, 68.0, 56488.0, 303.0, 529.0, 2.118720076E9, 8.86785060334E11, 118.0,
                8.5594711262E10, 9.163296719080909E36, 29.0, 787.0, 2.391563132818E12, 5.9840628E7, 5.7905064E8,
                6.9902886935763098E17, 79.0, 1.517874E7, 6.3445474E7, 951.0, 305.0, 66.0, 84.0, 1.7798172961352858E62,
                8.04117558803465E97 };

        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        // when
        for (double i : values) {
            histogram.add(i);
        }
    }

    @Test
    public void test_ShouldHandleVeryLargeDoubleValue_TDQ_14216() {
        double[] values = new double[] { 6.471292719031975E24,
                7.005697576483916E27,
                1.4209533856E10,
                64802.0,
                39.0,
                9.88079E7,
                313.0,
                3.1218063E8,
                655.0,
                444.0,
                16.0,
                822.0,
                262.0,
                4.9504559347780128E16,
                4.3136944E7,
                2.189301411798859E53,
                553.0,
                48.0,
                877.0,
                721.0,
                4.3699032144442255E20,
                326.0,
                7.569952728E9,
                83.0,
                9.477758927501843E52,
                9.60463518E8,
                4.141266E7,
                702.0,
                5.6496322046823373E18,
                296.0,
                580.0,
                9.0289352E7,
                73884.0,
                8.0,
                957.0,
                607.0,
                9.385833613374734E94};

        final StreamNumberHistogramStatistics histogram = new StreamNumberHistogramStatistics();
        // when
        for (double i : values) {
            histogram.add(i);
        }
    }

}
