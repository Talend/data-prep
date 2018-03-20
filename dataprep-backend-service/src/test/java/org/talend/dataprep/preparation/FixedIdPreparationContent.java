// ============================================================================
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

package org.talend.dataprep.preparation;

import java.util.Objects;

import org.talend.dataprep.api.preparation.PreparationActions;

public class FixedIdPreparationContent extends PreparationActions {

    public FixedIdPreparationContent() {
    }

    public FixedIdPreparationContent(String fixedId) {
        this.id = fixedId;
    }

    /**
     * Custom equals for the unit tests.
     *
     * @param o the other object to compare.
     * @return true if the other object is equals to this.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!o.getClass().isAssignableFrom(this.getClass())) {
            return false;
        }
        final PreparationActions other = (PreparationActions) o;
        if (!other.id().equals(this.id)) {
            return false;
        }
        if (!Objects.equals(this.getActions(), other.getActions())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
