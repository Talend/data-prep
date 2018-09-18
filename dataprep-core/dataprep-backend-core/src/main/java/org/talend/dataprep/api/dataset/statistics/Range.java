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
 * Class that represents a range [min, max[
 */
public class Range implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The min range value
     */
    @JsonProperty("min")
    private double min;

    /**
     * The max range value
     */
    @JsonProperty("max")
    private double max;

    /**
     * Constructor
     */
    public Range() {
        // needed for the json serialization
    }

    /**
     * Constructor
     * 
     * @param min The minimum value
     * @param max The maximum value
     */
    public Range(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Minimum value getter
     * 
     * @return The minimum value
     */
    public double getMin() {
        return min;
    }

    /**
     * Minimum value setter
     * 
     * @param min The new minimum value
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * Maximum value getter
     * 
     * @return The maximum value
     */
    public double getMax() {
        return max;
    }

    /**
     * Maximum value setter
     * 
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
