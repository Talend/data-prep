/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import java.util.Set;

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.Schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Representation of a Talend dataset entity.
 * Contains ONLY metadata about the dataset. Not its schema, not a sample, no analysis..
 * See <a href="https://github.com/Talend/dataset/blob/master/common/src/main/scala/org/talend/dataflow/common/model/Dataset.scala#L63">scala implementation in dataset</a>.
 */
@JsonIgnoreProperties({"__acl"})
public class Dataset {

    private String id;

    private Long created;

    private Long updated;

    private Boolean enabled;

    private String label;

    /** TComp type. */
    private String type;

    private String description;

    private Set<String> tags;

    private Long version;

    private String datastoreId;

    private Datastore datastore;

    private String schemaId;

    /** Raw TComp JSON properties. */
    private ObjectNode properties;

    /** ID of owner. */
    private String owner;

    private Integer schemaVersion;

    private CertificationState certification;

    private boolean favorite;

    private Set<String> entitlements;

    // TODO remove when no support of dataset legacy mode
    @JsonIgnoreProperties(ignoreUnknown = true)
    private DataSetMetadataLegacy dataSetMetadataLegacy;

    /**
     * // FIXME legacy DatasetMetadata fields that doesn't match Dataset Catalog fields
     * @see DataSetMetadata
     */
    public static class DataSetMetadataLegacy {

        private String encoding;
        private Schema schemaParserResult;
        private boolean draft;
        private String sheetName;
        private String tag;

        public DataSetMetadataLegacy() {
        }

        public Schema getSchemaParserResult() {
            return schemaParserResult;
        }

        public void setSchemaParserResult(Schema schemaParserResult) {
            this.schemaParserResult = schemaParserResult;
        }

        public boolean isDraft() {
            return draft;
        }

        public void setDraft(boolean draft) {
            this.draft = draft;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

    }

    public Dataset() {
    }

    public CertificationState getCertification() {
        return certification;
    }

    public void setCertification(CertificationState certification) {
        this.certification = certification;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(String datastoreId) {
        this.datastoreId = datastoreId;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public ObjectNode getProperties() {
        return properties;
    }

    public void setProperties(ObjectNode properties) {
        this.properties = properties;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public DataSetMetadataLegacy getDataSetMetadataLegacy() {
        return dataSetMetadataLegacy;
    }

    public void setDataSetMetadataLegacy(DataSetMetadataLegacy dataSetMetadataLegacy) {
        this.dataSetMetadataLegacy = dataSetMetadataLegacy;
    }

    public Set<String> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(Set<String> entitlements) {
        this.entitlements = entitlements;
    }

    public enum CertificationState {
        NONE,
        PENDING,
        CERTIFIED
    }
}
