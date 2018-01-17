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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the lifecycle of the data set (information that can change over time).
 */
public class DataSetLifecycle implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("inProgress")
    private boolean inProgress;

    @JsonProperty("importing")
    private boolean importing;

    @JsonProperty("contentAnalyzed")
    private boolean contentAnalyzed;

    @JsonProperty("schemaAnalyzed")
    private boolean schemaAnalyzed;

    @JsonProperty("qualityAnalyzed")
    private boolean qualityAnalyzed;

    /**
     * Changes the information on content indexed (is the data set content ready to be queried).
     *
     * @param contentAnalyzed The new value for the information.
     */
    public void contentIndexed(boolean contentAnalyzed) {
        this.contentAnalyzed = contentAnalyzed;
    }

    /**
     * @return <code>true</code> if data set content was indexed and can be queried, <code>false</code> otherwise.
     */
    public boolean contentIndexed() {
        return contentAnalyzed;
    }

    /**
     * Changes the information on schema analysis (is the data set content analyzed so column names and types are
     * available).
     *
     * @param schemaAnalyzed The new value for the information.
     */
    public void schemaAnalyzed(boolean schemaAnalyzed) {
        this.schemaAnalyzed = schemaAnalyzed;
    }

    /**
     * @return <code>true</code> if data set content was analyzed and column types were correctly inferred from values,
     * <code>false</code> otherwise.
     * @see RowMetadata
     */
    public boolean schemaAnalyzed() {
        return schemaAnalyzed;
    }

    /**
     * Changes the information on columns quality (is information about number of valid, invalid, empty values available
     * or not).
     *
     * @param qualityAnalyzed The new value for the information.
     */
    public void qualityAnalyzed(boolean qualityAnalyzed) {
        this.qualityAnalyzed = qualityAnalyzed;
    }

    /**
     * @return <code>true</code> if data set column values were analyzed and quality information is available,
     * <code>false</code> otherwise.
     */
    public boolean qualityAnalyzed() {
        return qualityAnalyzed;
    }

    /**
     * Changes the isImporting status of the data set.
     *
     * @param importing <code>true</code> to indicate all synchronous analysis are done, <code>false</code> otherwise.
     * @see #isImporting()
     */
    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    /**
     * @return <code>true</code> if all mandatory and synchronous analysis were done, <code>false</code> otherwise.
     */
    public boolean isImporting() {
        return importing;
    }

    /**
     * @return <code>true</code> if all mandatory analysis were done <b>but</b> results are not yet complete.
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * Changes the inProgress status of the data set.
     *
     * @param inProgress <code>true</code> to indicate not all analysis are done, <code>false</code> to indicate all
     * analysis is done.
     * @see #isInProgress()
     */
    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataSetLifecycle that = (DataSetLifecycle) o;
        return Objects.equals(importing, that.importing) && //
                Objects.equals(contentAnalyzed, that.contentAnalyzed) && //
                Objects.equals(schemaAnalyzed, that.schemaAnalyzed) && //
                Objects.equals(inProgress, that.inProgress) && //
                Objects.equals(qualityAnalyzed, that.qualityAnalyzed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(importing, contentAnalyzed, schemaAnalyzed, inProgress, qualityAnalyzed);
    }

    @Override
    public String toString() {
        return "DataSetLifecycle{" + "inProgress=" + inProgress + ", importing=" + importing + ", contentAnalyzed="
                + contentAnalyzed + ", schemaAnalyzed=" + schemaAnalyzed + ", qualityAnalyzed=" + qualityAnalyzed + '}';
    }
}
