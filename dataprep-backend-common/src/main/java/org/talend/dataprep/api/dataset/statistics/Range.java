package org.talend.dataprep.api.dataset.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Class that represents a range [min, max[
 */
public class Range implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The min range value
     */
    @JsonProperty("min")
    double min;

    /**
     * The max range value
     */
    @JsonProperty("max")
    double max;

    /**
     * Constructor
     */
    public Range() {
    }

    /**
     * Constructor
     * @param min The minimum value
     * @param max The maximum value
     */
    public Range(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Minimum value getter
     * @return The minimum value
     */
    public double getMin() {
        return min;
    }

    /**
     * Minimum value setter
     * @param min The new minimum value
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * Maximum value getter
     * @return The maximum value
     */
    public double getMax() {
        return max;
    }

    /**
     * Maximum value setter
     * @param max The new maximum value
     */
    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Range)) {
            return false;
        }

        final Range range = (Range) o;
        return Double.compare(range.min, min) == 0 && Double.compare(range.max, max) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(min);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
