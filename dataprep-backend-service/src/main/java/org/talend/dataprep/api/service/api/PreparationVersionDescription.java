// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.api;

import java.util.Objects;

/**
 * Bean that models a preparation version description.
 */
public class PreparationVersionDescription {

    /** The preparation version description. */
    private String description;

    /**
     * @return the Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreparationVersionDescription that = (PreparationVersionDescription) o;
        return Objects.equals(description, that.description);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(description);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "PreparationVersionDescription{" + "description='" + description + '\'' + '}';
    }
}
