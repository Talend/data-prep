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

package org.talend.dataprep.api.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.share.Owner;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple bean used to display a preparation and a summary of its related dataset and its location.
 */
public class EnrichedPreparation {

    private String id;

    private String dataSetId;

    /** Metadata on which the preparation is based. **/
    private RowMetadata rowMetadata;

    private String author;

    private String name;

    private long creationDate;

    private long lastModificationDate;

    private String headId;

    private List<String> steps;

    /** List of action metadata (description) */
    private List<ActionForm> metadata;

    private List<Action> actions;

    /** This preparation owner. */
    private Owner owner;

    private boolean allowFullRun;

    private List<StepDiff> diff;

    /** The dataset metadata to summarize. */
    @JsonProperty("dataset")
    private DataSetMetadataSummary summary;

    /** Where the preparation is stored. */
    private Folder folder;

    public void setSummary(DataSetMetadataSummary summary) {
        this.summary = summary;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public DataSetMetadataSummary getSummary() {
        return summary;
    }

    public Folder getFolder() {
        return folder;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public boolean isAllowFullRun() {
        return allowFullRun;
    }

    public void setAllowFullRun(boolean allowFullRun) {
        this.allowFullRun = allowFullRun;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<StepDiff> getDiff() {
        return diff;
    }

    public void setDiff(List<StepDiff> diff) {
        this.diff = diff;
    }

    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    public void setRowMetadata(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
    }

    public List<ActionForm> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<ActionForm> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        EnrichedPreparation that = (EnrichedPreparation) o;
        return Objects.equals(summary, that.summary) && Objects.equals(folder, this.folder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), summary, folder);
    }

    @Override
    public String toString() {
        return "EnrichedPreparation{" + "preparation id=" + getId() + '\'' + ", summary=" + summary + ", folder=" + folder + '}';
    }

    /**
     * Inner class that summarize a dataset metadata.
     */
    public static class DataSetMetadataSummary implements Serializable {

        /** The dataset id. */
        private String dataSetId = null;

        /** The dataset name. */
        private String dataSetName = null;

        /** the number of rows in the dataset. */
        private long dataSetNbRow;

        // For deserialization purposes only
        public DataSetMetadataSummary() {
        }

        /**
         * Constructor.
         *
         * @param metadata the dataset metadata to create the summary from.
         */
        public DataSetMetadataSummary(DataSetMetadata metadata) {
            if (metadata != null) {
                this.dataSetId = metadata.getId();
                this.dataSetName = metadata.getName();
                this.dataSetNbRow = metadata.getContent().getNbRecords();
            }
        }

        public String getDataSetId() {
            return dataSetId;
        }

        public String getDataSetName() {
            return dataSetName;
        }

        public long getDataSetNbRow() {
            return dataSetNbRow;
        }

        @Override
        public String toString() {
            return "DataSetMetadataSummary{" + "dataSetId='" + dataSetId + '\'' + ", dataSetName='" + dataSetName + '\''
                    + ", dataSetNbRow=" + dataSetNbRow + '}';
        }
    }

}
