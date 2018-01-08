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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("statistics")
public class Statistics implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    private long count;

    @JsonProperty("valid")
    private long valid;

    @JsonProperty("invalid")
    private long invalid;

    @JsonProperty("empty")
    private long empty;

    @JsonProperty("max")
    private double max = 0;

    @JsonProperty("min")
    private double min = 0;

    @JsonProperty("mean")
    private double mean = 0;

    @JsonProperty("variance")
    private double variance = 0;

    @JsonProperty("duplicateCount")
    private long duplicateCount = 0;

    @JsonProperty("distinctCount")
    private long distinctCount = 0;

    @JsonProperty("frequencyTable")
    private List<DataFrequency> dataFrequencies = new LinkedList<>();

    @JsonProperty("patternFrequencyTable")
    private List<PatternFrequency> patternFrequencies = new LinkedList<>();

    @JsonProperty("quantiles")
    private Quantiles quantiles = new Quantiles();

    @JsonProperty("histogram")
    private Histogram histogram;

    @JsonProperty("textLengthSummary")
    private TextLengthSummary textLengthSummary = new TextLengthSummary();

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getValid() {
        return valid;
    }

    public void setValid(long valid) {
        this.valid = valid;
    }

    public long getInvalid() {
        return invalid;
    }

    public void setInvalid(long invalid) {
        this.invalid = invalid;
    }

    public long getEmpty() {
        return empty;
    }

    public void setEmpty(long empty) {
        this.empty = empty;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public long getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(long duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public long getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(long distinctCount) {
        this.distinctCount = distinctCount;
    }

    public List<DataFrequency> getDataFrequencies() {
        return dataFrequencies;
    }

    public void setDataFrequencies(List<DataFrequency> dataFrequencies) {
        this.dataFrequencies = dataFrequencies;
    }

    public List<PatternFrequency> getPatternFrequencies() {
        return patternFrequencies;
    }

    public void setPatternFrequencies(List<PatternFrequency> patternFrequencies) {
        this.patternFrequencies = patternFrequencies;
    }

    public Quantiles getQuantiles() {
        return quantiles;
    }

    public void setQuantiles(Quantiles quantiles) {
        this.quantiles = quantiles;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    public TextLengthSummary getTextLengthSummary() {
        return textLengthSummary;
    }

    public void setTextLengthSummary(TextLengthSummary textLengthSummary) {
        this.textLengthSummary = textLengthSummary;
    }

    @Override
    public String toString() {
        return "Statistics{" + "count=" + count + ", valid=" + valid + ", invalid=" + invalid + ", empty=" + empty + ", max="
                + max + ", min=" + min + ", mean=" + mean + ", variance=" + variance + ", duplicateCount=" + duplicateCount
                + ", distinctCount=" + distinctCount + ", dataFrequencies=" + dataFrequencies + ", patternFrequencies="
                + patternFrequencies + ", quantiles=" + quantiles + ", histogram=" + histogram + ", textLengthSummary="
                + textLengthSummary + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Statistics))
            return false;

        Statistics that = (Statistics) o;

        if (count != that.count) {
            return false;
        }
        if (valid != that.valid) {
            return false;
        }
        if (invalid != that.invalid) {
            return false;
        }
        if (empty != that.empty) {
            return false;
        }
        if (Double.compare(that.max, max) != 0) {
            return false;
        }
        if (Double.compare(that.min, min) != 0) {
            return false;
        }
        if (Double.compare(that.mean, mean) != 0) {
            return false;
        }
        if (Double.compare(that.variance, variance) != 0) {
            return false;
        }
        if (duplicateCount != that.duplicateCount) {
            return false;
        }
        if (distinctCount != that.distinctCount) {
            return false;
        }
        if (!new HashSet<>(dataFrequencies).equals(new HashSet<>(that.dataFrequencies))) {
            return false;
        }
        if (!new HashSet<>(patternFrequencies).equals(new HashSet<>(that.patternFrequencies))) {
            return false;
        }
        if (!quantiles.equals(that.quantiles)) {
            return false;
        }
        if ((histogram == null && that.histogram != null) || (histogram != null && !histogram.equals(that.histogram))) {
            return false;
        }
        return textLengthSummary.equals(that.textLengthSummary);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (count ^ (count >>> 32));
        result = 31 * result + (int) (valid ^ (valid >>> 32));
        result = 31 * result + (int) (invalid ^ (invalid >>> 32));
        result = 31 * result + (int) (empty ^ (empty >>> 32));
        temp = Double.doubleToLongBits(max);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(min);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mean);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(variance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (duplicateCount ^ (duplicateCount >>> 32));
        result = 31 * result + (int) (distinctCount ^ (distinctCount >>> 32));
        result = 31 * result + dataFrequencies.hashCode();
        result = 31 * result + patternFrequencies.hashCode();
        result = 31 * result + quantiles.hashCode();
        result = 31 * result + histogram.hashCode();
        result = 31 * result + textLengthSummary.hashCode();
        return result;
    }
}
