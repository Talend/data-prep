package org.talend.dataprep.api.preparation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;

public class PreparationDTO implements SharedResource {

    /** The dataset id. */
    private String id;

    /** The creation date. */
    private long creationDate = System.currentTimeMillis();

    /** The last modification date. */
    private long lastModificationDate;

    /** The creation date. */
    private String dataSetId;

    /** The preparation name. */
    private String name;

    private List<String> steps = new ArrayList<>();

    private Owner owner = new Owner();

    private boolean shared;

    private boolean sharedByMe;

    private Set<String> roles;

    private String author;

    private String headId;

    public String getDataSetId() {
        return dataSetId;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public List<String> getSteps() {
        return steps;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public Owner getOwner() {
        return owner;
    }

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

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSteps(List<String> stepIds) {
        this.steps = stepIds;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isSharedByMe() {
        return sharedByMe;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

}
