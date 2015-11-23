// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.api.dataset.statistics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.talend.dataquality.statistics.numeric.histogram.Range;

/**
 * This class implements implements a new kind of histogram suitable for evolving data sets like streams. This histogram
 * has to kind of internal representations: its singular representation and regular representation. When starting, the
 * histogram maintains singular bins: it has at most numberOfBins different values, each of them corresponds to a
 * singular bin. A bin is singular if it contains one or many occurrences of a unique value. When the numberOfBins + 1
 * value appears we switch to a regular representation: each bin may contains many values.
 *
 */
public class StreamHistogramStatistics {

    /**
     * The default bin number which is 32.
     */
    private static final int DEFAULT_BIN_NUMBER = 32;

    /**
     * The number of regulars of this histogram which must be power of 2.
     */
    public int numberOfBins = DEFAULT_BIN_NUMBER;

    /**
     * Internal representation of the histogram in its regular version.
     */
    private long[] regulars;

    /**
     * Internal representation of histogram in its singular version.
     */
    private Map<Double, Long> singulars = new HashMap<>();

    /**
     * The minimum value know to this histogram.
     */
    private double min = Double.NaN;

    /**
     * The maximum value known to this histogram.
     */
    private double max = Double.NaN;

    /**
     * The lower bound (start point of the minimum bin). It may be different from the minimum value.
     */
    private double lowerBound = Double.NaN;

    /**
     * The currently bin size which may change depending on how the values are inserted into this histogram.
     */
    private double binSize = Double.NaN;

    /**
     * The number of values added, so far, to this histogram.
     */
    private long numberOfValues;

    /**
     * A flag to know whether or not all regulars of this histogram are singular. A bin is singular if it contains one
     * or many occurrences of a unique value.
     *
     */
    private boolean singular = true;

    /**
     * The sum of values contained in the histogram.
     */
    private double sum = Double.NaN;

    /**
     * Add the specified value to this histogram.
     *
     * @param d the value to add to this histogram
     */
    public void add(double d) {
        // So far, we have not met n different values
        if ((singulars != null) && (singulars.size() < numberOfBins || singulars.containsKey(d))) {
            singularAdd(d);
        }
        // We have already met n different values
        else {
            regularAdd(d);
        }
    }

    /**
     * Add the specified value to this histogram as a singular value
     *
     * @param d the value to add to this histogram
     */
    private void singularAdd(double d) {
        Long count = singulars.get(d);
        if (count == null) {
            singulars.put(d, 1L);
        } else {
            singulars.put(d, count + 1);
        }
        // update minimum and maximum values
        if (Double.isNaN(min) || d < min) {
            min = d;
            lowerBound = min;
        }
        if (Double.isNaN(max) || max < d) {
            max = d;
        }
        // increment the number of values stored in this histogram
        numberOfValues++;
        sum = Double.isNaN(sum) ? d : sum + d;
    }

    /**
     * Add the specified value to this histogram as as regular value ,i.e., in a bucket.
     *
     * @param d the value to add to this histogram
     */
    private void regularAdd(double d) {
        // We now have in the histogram more than numberOfBins different values and
        // we have to transform it to an histogram of equal size
        if (singular) {
            turnSingularsToRegulars();
            // singulars can be garbage collected
            singulars = null;
            singular = false;
        }
        if (d < lowerBound) {
            extendToLeft(d);
        } else if (lowerBound + (numberOfBins * binSize) <= d) { // if d is higher than the upper bound
            extendToRight(d);
        }
        // d is the min
        if (d < min) {
            min = d;
        }
        // d is the max
        if (max < d) {
            max = d;
        }
        int bin = (int) ((d - lowerBound) / binSize);
        regulars[bin]++;
        // increment the number of values stored in this histogram
        numberOfValues++;
        sum += d;
    }

    /**
     * This method transform values to bin. Prior to calling this method, all the bins are singular and after its call
     * all the singular bins become regular (each bin may contains many different values).
     */
    private void turnSingularsToRegulars() {
        regulars = new long[numberOfBins];
        binSize = (max - min) * 2 / numberOfBins;
        lowerBound = min;
        for (int i = 0; i < numberOfBins; i++) {
            regulars[i] = 0L;
        }

        for (Map.Entry<Double, Long> entry : singulars.entrySet()) {
            int bin = (int) ((entry.getKey() - min) / binSize);
            regulars[bin] += entry.getValue();
        }
    }

    /**
     * This method first merges previously existing regulars up to the factor determined and set the new bin size to a
     * multiple of the previous one. It then place freshly merged bins at the right position in the array.
     *
     * @param d the new value to add to this histogram
     */
    private void extendToLeft(double d) {
        double histogramWidth = numberOfBins * binSize;
        int factor = 2;
        while (d < lowerBound - histogramWidth * (factor >>> 1)) {
            factor <<= 1;
        }
        binSize = binSize * factor;
        int offset = (int) (histogramWidth * (factor >>> 1) / binSize);
        lowerBound = this.lowerBound - histogramWidth * (factor >>> 1);

        // merge previously existing regulars
        merge(factor, offset);
    }

    /**
     * This method first merges previously existing regulars up to the factor determined and set the new bin size to a
     * multiple of the previous one. It then place freshly merged bins at the right position in the array.
     *
     * @param d the new value to add to this histogram
     */
    private void extendToRight(double d) {
        double histogramWidth = numberOfBins * binSize;
        int factor = 2;
        while (lowerBound + histogramWidth * (factor) <= d) {
            factor <<= 1;
        }
        binSize = binSize * factor;
        // merge previously existing regulars
        merge(factor, 0);
    }

    /**
     * Merge previously existing regulars to have new regulars of width binSize * factor.
     *
     * @param factor the factor by which the binSize is multiplied
     * @param offset the position of the older lower bound in the new array
     */
    private void merge(int factor, int offset) {
        // merge previous regulars to form new regulars of newBinSize width
        int k = 0;
        for (int i = 0; i < numberOfBins; i += factor) {
            long count = 0L;
            for (int j = i; j < i + factor && j < numberOfBins; j++) {
                count += regulars[j];
                regulars[j] = 0;
            }
            regulars[k++] = count;
        }
        if (0 < offset) {
            // move bins according to the offset
            // to avoid overwriting some bins that must not we start from "numberOfBins - 1 - offset"
            for (int i = numberOfBins - 1 - offset; 0 <= i; i--) {
                long count = regulars[i];
                regulars[i] = 0;
                regulars[i + offset] = count;
            }
        }
    }

    /**
     * Get histograms as a map.
     *
     * @return the histogram map where Key is the range and value is the frequency. <br>
     * Note that the returned ranges are in pattern of [Min, Min+binSize),[Min+binSize,Min+binSize*2)...[Max-binSize,Max
     * <b>]</b>
     */
    public Map<Range, Long> getHistogram() {

        if (singular) {
            return getSingularHistogram();
        }

        Map<Range, Long> result = new LinkedHashMap<>();
        // leading and trailing bins with a count of 0 will not be returned
        int start = firstNonEmptyBin();
        int end = lastNonEmptyBin();
        // create ranges
        for (int i = start; i <= end; i++) {
            double d = lowerBound + binSize * i;
            Range r = new Range(d, d + binSize);
            result.put(r, regulars[i]);
        }

        return result;

    }

    /**
     * Return the histogram map where Key is the range and value is the frequency.<br>
     * Key will be of form [d, d]
     *
     * @return
     */
    private Map<Range, Long> getSingularHistogram() {
        Map<Range, Long> result = new LinkedHashMap<>();
        TreeMap<Number, Long> bins = new TreeMap<>(this.singulars);
        for (Number n : bins.keySet()) {
            double d = (double) n;
            Range r = new Range(d, d);
            result.put(r, bins.get(d));
        }
        return result;
    }

    /**
     * Return the first non empty bin (a bin with a strictly positive count ).
     *
     * @return Return the first non empty bin
     */
    private int firstNonEmptyBin() {
        int start = 0;
        for (int i = 0; i < numberOfBins; i++) {
            if (regulars[i] == 0) {
                start++;
            } else {
                break;
            }
        }
        return start;
    }

    /**
     * Return the last non empty bin (a bin with a strictly positive count ).
     *
     * @return the last non empty bin
     */
    private int lastNonEmptyBin() {
        int end = numberOfBins - 1;

        for (int i = numberOfBins - 1; i >= 0; i--) {
            if (regulars[i] == 0) {
                end--;
            } else {
                break;
            }
        }
        return end;
    }

    /**
     * Return the minimum value added to this histogram.
     *
     * @return the minimum value added to this histogram
     */
    public double getMin() {
        return Double.isNaN(min) ? 0 : min;
    }

    /**
     * Return the maximum value added to this histogram.
     *
     * @return the maximum value added to this histogram
     */
    public double getMax() {
        return Double.isNaN(max) ? 0 : max;
    }

    /**
     * Return the arithmetic mean of values added to this histogram.
     *
     * @return the arithmetic mean of values added to this histogram
     */
    public double getMean() {
        return Double.isNaN(sum) ? 0 : sum / numberOfValues;
    }

    /**
     * Return the maximum number of bins of this histogram.
     *
     * @return
     */
    public int getNumberOfBins() {
        return numberOfBins;
    }

    /**
     * Set number of bins in histogram. Number must be a positive integer and a power of 2.
     *
     * @param numberOfBins the number of regulars of this histogram. Value must be a positive integer and a power of 2.
     */
    public void setNumberOfBins(int numberOfBins) {
        if (numberOfBins <= 0) {
            throw new IllegalArgumentException("The number of bin must be a positive integer and a power of 2");
        }
        if ((numberOfBins & (numberOfBins - 1)) != 0) {
            throw new IllegalArgumentException("The number of bin which is " + numberOfBins + " must be a power of 2");
        }
        this.numberOfBins = numberOfBins;
    }

    /**
     * Return the number of values added to this histogram.
     * 
     * @return the number of values added to this histogram
     */
    public long getNumberOfValues() {
        return numberOfValues;
    }
}