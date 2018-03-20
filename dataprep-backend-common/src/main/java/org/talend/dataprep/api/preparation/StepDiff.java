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

package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StepDiff implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    private List<String> createdColumns = new ArrayList<>(0);

    public List<String> getCreatedColumns() {
        return createdColumns;
    }

    public void setCreatedColumns(List<String> createdColumns) {
        this.createdColumns = createdColumns;
    }

    @Override
    public String toString() {
        return "StepDiff{" + "createdColumns=" + createdColumns + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StepDiff)) {
            return false;
        }
        StepDiff stepDiff = (StepDiff) o;
        return createdColumns != null ? createdColumns.equals(stepDiff.createdColumns) : stepDiff.createdColumns == null;
    }

    @Override
    public int hashCode() {
        return createdColumns != null ? createdColumns.hashCode() : 0;
    }
}
