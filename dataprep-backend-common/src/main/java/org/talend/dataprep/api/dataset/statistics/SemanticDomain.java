// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information about alternate semantic type
 * <ul>
 * <li>Id ({@link #getId()})</li>
 * <li>Label ({@link #getLabel()})</li>
 * <li>Frequency percentage this type is matched ({@link #getFrequency()} ()})</li>
 * </ul>
 *
 */
public class SemanticDomain implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("label")
    private String label;

    @JsonProperty("frequency")
    private float frequency;

    /**
     * Default empty constructor.
     */
    public SemanticDomain() {
        // empty default constructor
    }

    public SemanticDomain(String id, String label, float frequency) {
        this.id = id;
        this.label = label;
        this.frequency = frequency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "SemanticDomain{" + "id='" + id + '\'' + ", label='" + label + '\'' + ", frequency=" + frequency + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SemanticDomain that = (SemanticDomain) o;
        return Objects.equals(frequency, that.frequency) //
                && Objects.equals(id, that.id) //
                && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, frequency);
    }
}
