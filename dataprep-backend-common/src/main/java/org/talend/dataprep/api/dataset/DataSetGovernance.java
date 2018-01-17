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
import java.util.Objects;

/**
 * Represents governance data of the dataset.
 */
public class DataSetGovernance implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    public enum Certification {
        NONE,
        PENDING,
        CERTIFIED
    }

    private Certification certificationStep = Certification.NONE;

    public Certification getCertificationStep() {
        return this.certificationStep;
    }

    public void setCertificationStep(Certification certificationStep) {
        this.certificationStep = certificationStep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSetGovernance that = (DataSetGovernance) o;
        return Objects.equals(certificationStep, that.certificationStep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificationStep);
    }

    @Override
    public String toString() {
        return "DataSetGovernance{" + "certificationStep=" + certificationStep + '}';
    }
}
