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

public class DataFrequency implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    String data;

    @JsonProperty("occurrences")
    long occurrences;

    /**
     * Default empty constructor.
     */
    public DataFrequency() {
        // Here for JSON deserialization
    }

    public DataFrequency(String data, long occurrences) {
        this.data = data;
        this.occurrences = occurrences;
    }

    @Override
    public String toString() {
        return "DataFrequency{" + "data='" + data + '\'' + ", occurrences=" + occurrences + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataFrequency)) {
            return false;
        }

        DataFrequency that = (DataFrequency) o;

        if (occurrences != that.occurrences) {
            return false;
        }
        return data.equals(that.data);

    }

    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + (int) (occurrences ^ (occurrences >>> 32));
        return result;
    }
}
