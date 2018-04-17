package org.talend.dataprep.dataset.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of a Talend dataset entity.
 * See <a href="https://github.com/Talend/dataset/blob/master/common/src/main/scala/org/talend/dataflow/common/model/Dataset.scala#L63">scala implementation in dataset</a>.
 */
public class Dataset {

    private String id;

    private DateTime created;

    private DateTime updated;

    private Boolean enabled;

    private String label;

    /** TComp type. */
    private String type;

    private String description;

    private Set<String> tags;

    private Long version;

    private String datastoreId;

    private String schemaId;

    /** Raw TComp JSON properties. */
    private ObjectNode properties;

    private EncodedSample sample;

    /** ID of owner. */
    private String owner;

    private Integer schemaVersion;

    public Dataset() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public void setUpdated(DateTime updated) {
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

    public EncodedSample getSample() {
        return sample;
    }

    public void setSample(EncodedSample sample) {
        this.sample = sample;
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
}
