//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.api;

import java.io.Serializable;
import java.util.Objects;

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.preparation.service.UserPreparation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple bean used to display a preparation and a summary of its related dataset and its location.
 */
@JsonIgnoreProperties({"dataSetId"})
public class EnrichedPreparation extends UserPreparation {

    /** For the Serialization interface. */
    private static final long serialVersionUID = 1L;

    /** The dataset metadata to summarize. */
    @JsonProperty("dataset")
    private DataSetMetadataSummary summary;

    /** Where the preparation is stored. */
    private Folder folder;

    /**
     * Private constructor used to for code reuse.
     *
     * @param preparation the preparation to display.
     */
    private EnrichedPreparation(UserPreparation preparation) {
        this.setId(preparation.id());
        this.setAppVersion(preparation.getAppVersion());
        this.setDataSetId(preparation.getDataSetId());
        this.setAuthor(preparation.getAuthor());
        this.setName(preparation.getName());
        this.setCreationDate(preparation.getCreationDate());
        this.setLastModificationDate(preparation.getLastModificationDate());
        this.setHeadId(preparation.getHeadId());
        this.setSteps(preparation.getSteps());
        this.setOwner(preparation.getOwner());
    }

    /**
     * Create an enriched preparation with dataset information.
     *
     * @param preparation the preparation to display.
     * @param dataSetMetadata the dataset metadata to summarize.
     */
    public EnrichedPreparation(UserPreparation preparation, DataSetMetadata dataSetMetadata) {
        this(preparation);
        this.summary = new DataSetMetadataSummary(dataSetMetadata);
    }

    /**
     * Create an enriched preparation with additional path information.
     *
     * @param preparation the preparation to display.
     * @param folder where the folder is stored.
     */
    public EnrichedPreparation(UserPreparation preparation, Folder folder) {
        this(preparation);
        this.folder = folder;
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

    public DataSetMetadataSummary getSummary() {
        return summary;
    }

    public Folder getFolder() {
        return folder;
    }

    @Override
    public String toString() {
        return "EnrichedPreparation{" +
                "preparation id=" + getId() +'\''+
                ", summary=" + summary +
                ", folder=" + folder +
                '}';
    }

    /**
     * Inner class that summarize a dataset metadata.
     */
    private static class DataSetMetadataSummary implements Serializable {
        /** For the Serialization interface.*/
        private static final long serialVersionUID = 1L;
        /** The dataset id. */
        private String dataSetId = null;
        /** The dataset name. */
        private String dataSetName = null;
        /** the number of rows in the dataset. */
        private long dataSetNbRow;

        /**
         * Constructor.
         * @param metadata the dataset metadata to create the summary from.
         */
        DataSetMetadataSummary(DataSetMetadata metadata) {
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
            return "DataSetMetadataSummary{" +
                    "dataSetId='" + dataSetId + '\'' +
                    ", dataSetName='" + dataSetName + '\'' +
                    ", dataSetNbRow=" + dataSetNbRow +
                    '}';
        }
    }

}
