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

public class TextLengthSummary implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    public TextLengthSummary() {
    }

    @JsonProperty("minimalLength")
    private double minimalLength = Double.NaN;

    @JsonProperty("maximalLength")
    private double maximalLength = Double.NaN;

    @JsonProperty("averageLength")
    private double averageLength = Double.NaN;

    public double getMinimalLength() {
        return minimalLength;
    }

    public void setMinimalLength(double minimalLength) {
        this.minimalLength = minimalLength;
    }

    public double getMaximalLength() {
        return maximalLength;
    }

    public void setMaximalLength(double maximalLength) {
        this.maximalLength = maximalLength;
    }

    public double getAverageLength() {
        return averageLength;
    }

    public void setAverageLength(double averageLength) {
        this.averageLength = averageLength;
    }

    @Override
    public String toString() {
        return "TextLengthSummary{" + "minimalLength=" + minimalLength + ", maximalLength=" + maximalLength + ", averageLength="
                + averageLength + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextLengthSummary)) {
            return false;
        }

        TextLengthSummary that = (TextLengthSummary) o;

        if (Double.compare(that.minimalLength, minimalLength) != 0) {
            return false;
        }
        if (Double.compare(that.maximalLength, maximalLength) != 0) {
            return false;
        }
        return Double.compare(that.averageLength, averageLength) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(minimalLength);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maximalLength);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(averageLength);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
