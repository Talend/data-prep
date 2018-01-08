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

package org.talend.dataprep.api.dataset;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Quality bean linked to column metadata.
 */
public class Quality implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Number of empty records. */
    @JsonProperty("empty")
    private int empty;

    /** Number of invalid records. */
    @JsonProperty("invalid")
    private int invalid;

    /** Number of valid records. */
    @JsonProperty("valid")
    private int valid;

    /**
     * @return the number of empty records.
     */
    public int getEmpty() {
        return empty;
    }

    /**
     * @param empty the number of empty records to set.
     */
    public void setEmpty(int empty) {
        this.empty = empty;
    }

    /**
     * @return the number of invalid records.
     */
    public int getInvalid() {
        return invalid;
    }

    /**
     * @param invalid the number of invalid records to set.
     */
    public void setInvalid(int invalid) {
        this.invalid = invalid;
    }

    /**
     * @return the number of valid records.
     */
    public int getValid() {
        return valid;
    }

    /**
     * @param valid the number of valid records to set.
     */
    public void setValid(int valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quality)) {
            return false;
        }

        Quality quality = (Quality) o;

        if (empty != quality.empty) {
            return false;
        }
        if (invalid != quality.invalid) {
            return false;
        }
        if (valid != quality.valid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = empty;
        result = 31 * result + invalid;
        result = 31 * result + valid;
        return result;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Quality{" + "empty=" + empty + ", invalid=" + invalid + ", valid=" + valid + '}';
    }
}
