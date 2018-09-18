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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents information about alternate semantic type
 * <ul>
 * <li>Id ({@link #getId()})</li>
 * <li>Label ({@link #getLabel()})</li>
 * <li>Frequency percentage this type is matched ({@link #getScore()} ()})</li>
 * </ul>
 *
 */
public class SemanticDomain implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    private String id;

    private String label;

    @JsonProperty("frequency")
    private float score;

    public SemanticDomain() {
        // empty default constructor
    }

    public SemanticDomain(String id, String label, float score) {
        this.id = id;
        this.label = label;
        this.score = score;
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

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "SemanticDomain{" + "id='" + id + '\'' + ", label='" + label + '\'' + ", score=" + score + '}';
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
        return Objects.equals(score, that.score) //
                && Objects.equals(id, that.id) //
                && Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, score);
    }
}
