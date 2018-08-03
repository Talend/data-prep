package org.talend.dataprep.api.preparation;

import java.util.List;
import java.util.Set;

import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreparationListItemDTO implements SharedResource {

    /** The dataset id. */
    private String id;

    /** The creation date. */
    private long creationDate;

    /** The last modification date. */
    private long lastModificationDate;

    /** The creation date. */
    @JsonProperty("dataset")
    private PreparationListItemDTO.DataSet dataSet = new DataSet();

    /** The preparation name. */
    private String name;

    private List<String> steps;

    private Owner owner;

    private boolean shared;

    private boolean sharedByMe;

    private Set<String> roles;

    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public void setSharedResource(boolean shared) {
        this.shared = shared;
    }

    @Override
    public void setSharedByMe(boolean sharedByMe) {
        this.sharedByMe = sharedByMe;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String getOwnerId() {
        return owner.getId();
    }

    public Owner getOwner() {
        return owner;
    }

    public boolean isShared() {
        return shared;
    }

    public boolean isSharedByMe() {
        return sharedByMe;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public class DataSet {

        /** The creation date. */
        private String dataSetName;

        public String getDataSetName() {
            return dataSetName;
        }

        public void setDataSetName(String dataSetName) {
            this.dataSetName = dataSetName;
        }
    }
}
