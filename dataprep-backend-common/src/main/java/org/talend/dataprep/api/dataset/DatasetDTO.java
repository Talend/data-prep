package org.talend.dataprep.api.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;
import org.talend.dataprep.dataset.adapter.Dataset;

import java.util.Set;

public class DatasetDTO implements SharedResource {

    /**
     * The dataset id.
     */
    private String id;

    /**
     * The creation date.
     */
    @JsonProperty("created")
    private Long creationDate;

    /**
     * The last modification date.
     */
    private Long lastModificationDate;

    /**
     * Owner of the dataset
     */
    private Owner owner;

    /**
     * Author ID of the dataset
     */
    @JsonIgnore
    private String author;

    /**
     * Is the dataset shared
     */
    private boolean shared;

    /**
     * Is the dataset shared by the current user
     */
    private boolean sharedByMe;

    private Set<String> roles;

    private String type;

    private Long records;

    /**
     * Certification state of the dataset
     */
    @JsonProperty("certificationStep")
    private Dataset.CertificationState certification;

    private String name;

    private boolean draft;

    private boolean favorite;

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

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public Owner getOwner() {
        return owner;
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

    public Dataset.CertificationState getCertification() {
        return certification;
    }

    public void setCertification(Dataset.CertificationState certification) {
        this.certification = certification;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRecords() {
        return records;
    }

    public void setRecords(Long records) {
        this.records = records;
    }
}
