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

package org.talend.dataprep.helper.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Element of an aggregate result.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregateResult {

    public String data;

    @JsonProperty("MAX")
    public String max;

    @JsonProperty("AVERAGE")
    public String average;

    @JsonProperty("MIN")
    public String min;

    @JsonProperty("SUM")
    public String sum;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AggregateResult that = (AggregateResult) o;

        if (data != null ? !data.equals(that.data) : that.data != null)
            return false;
        if (max != null ? !max.equals(that.max) : that.max != null)
            return false;
        if (average != null ? !average.equals(that.average) : that.average != null)
            return false;
        if (min != null ? !min.equals(that.min) : that.min != null)
            return false;
        return sum != null ? sum.equals(that.sum) : that.sum == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (max != null ? max.hashCode() : 0);
        result = 31 * result + (average != null ? average.hashCode() : 0);
        result = 31 * result + (min != null ? min.hashCode() : 0);
        result = 31 * result + (sum != null ? sum.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregateResult{" + "data='" + data + '\'' + ", max='" + max + '\'' + ", average='" + average + '\''
                + ", min='" + min + '\'' + ", sum='" + sum + '\'' + '}';
    }
}
