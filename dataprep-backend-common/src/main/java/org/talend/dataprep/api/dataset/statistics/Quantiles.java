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

public class Quantiles implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("median")
    private double median = Double.NaN;

    @JsonProperty("lowerQuantile")
    private double lowerQuantile = Double.NaN;

    @JsonProperty("upperQuantile")
    private double upperQuantile = Double.NaN;

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getLowerQuantile() {
        return lowerQuantile;
    }

    public void setLowerQuantile(double lowerQuantile) {
        this.lowerQuantile = lowerQuantile;
    }

    public double getUpperQuantile() {
        return upperQuantile;
    }

    public void setUpperQuantile(double upperQuantile) {
        this.upperQuantile = upperQuantile;
    }

    @Override
    public String toString() {
        return "Quantiles{" + "median=" + median + ", lowerQuantile=" + lowerQuantile + ", upperQuantile=" + upperQuantile + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quantiles)) {
            return false;
        }

        Quantiles quantiles = (Quantiles) o;

        if (Double.compare(quantiles.median, median) != 0) {
            return false;
        }
        if (Double.compare(quantiles.lowerQuantile, lowerQuantile) != 0) {
            return false;
        }
        return Double.compare(quantiles.upperQuantile, upperQuantile) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(median);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lowerQuantile);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(upperQuantile);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
