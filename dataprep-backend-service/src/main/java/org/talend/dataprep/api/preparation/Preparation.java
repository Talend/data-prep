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

package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Transient;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * The Preparation class represents the series of {@link Step steps} one can apply on a dataset to transform it.
 */
public class Preparation extends Identifiable implements SharedResource, Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset id. */
    private String dataSetId;

    /** Metadata on which the preparation is based. **/
    private RowMetadata rowMetadata;

    /** The author name. */
    private String author;

    /** The preparation name. */
    private String name;

    /** The creation date. */
    private long creationDate;

    /** The last modification date. */
    private long lastModificationDate;

    /** The head id. */
    private String headId;

    /** Version of the app */
    @JsonProperty("app-version")
    private String appVersion;

    /** List of the steps id for this preparation. */
    private List<String> steps;

    /** This preparation owner. */
    @Transient // no saved in the database but computed when needed
    private Owner owner;

    /** True if this preparation is shared by another user. */
    @Transient // no saved in the database but computed when needed
    private boolean sharedPreparation = false;

    /** True if this preparation is shared by current user. */
    @Transient // no saved in the database but computed when needed
    private boolean sharedByMe = false;

    /** What role has the current user on this preparation. */
    @Transient // no saved in the database but computed when needed
    private Set<String> roles = new HashSet<>();

    /**
     * Default empty constructor.
     */
    public Preparation() {
        // needed for mongodb integration
    }

    /**
     * Default constructor.
     *
     * @param id the preparation id.
     * @param appVersion the application version to store within the preparation.
     */
    @JsonCreator
    public Preparation(@JsonProperty("id") String id, @JsonProperty("app-version") String appVersion) {
        this.id = id;
        this.creationDate = System.currentTimeMillis();
        this.lastModificationDate = this.creationDate;
        this.appVersion = appVersion;
    }

    /**
     * Create a preparation out of the given parameters.
     *
     * @param id the preparation id.
     * @param dataSetId the dataset id.
     * @param headId the head step id.
     * @param appVersion the application version to store within the preparation.
     */
    public Preparation(String id, String dataSetId, String headId, String appVersion) {
        this(id, appVersion);
        this.dataSetId = dataSetId;
        this.headId = headId;
    }

    /**
     * @return List of the steps id for this preparation.
     * @see org.talend.dataprep.preparation.store.PreparationRepository#get(String, Class)
     */
    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    public void setRowMetadata(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Owner getOwner() {
        return owner;
    }

    /**
     * @see SharedResource#setOwner(Owner)
     */
    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public void setSharedResource(boolean shared) {
        this.sharedPreparation = shared;
    }

    public boolean isSharedByMe() {
        return sharedByMe;
    }

    @Override
    public void setSharedByMe(boolean sharedByMe) {
        this.sharedByMe = sharedByMe;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isSharedPreparation() {
        return sharedPreparation;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getOwnerId() {
        return author;
    }

    public void updateLastModificationDate() {
        this.lastModificationDate = System.currentTimeMillis();
    }

    public Preparation merge(Preparation other) {
        Preparation merge = new Preparation(id, other.getAppVersion());
        merge.dataSetId = other.dataSetId != null ? other.dataSetId : dataSetId;
        merge.rowMetadata = other.rowMetadata != null ? other.rowMetadata : rowMetadata;
        merge.author = other.author != null ? other.author : author;
        merge.name = other.name != null ? other.name : name;
        merge.creationDate = min(other.creationDate, creationDate);
        merge.lastModificationDate = max(other.lastModificationDate, lastModificationDate);
        merge.headId = other.headId != null ? other.headId : headId;
        return merge;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dataSetId", dataSetId)
                .append("rowMetadata", rowMetadata)
                .append("author", author)
                .append("name", name)
                .append("creationDate", creationDate)
                .append("lastModificationDate", lastModificationDate)
                .append("headId", headId)
                .append("appVersion", appVersion)
                .append("steps", steps)
                .append("owner", owner)
                .append("sharedPreparation", sharedPreparation)
                .append("sharedByMe", sharedByMe)
                .append("roles", roles)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preparation that = (Preparation) o;
        return Objects.equals(id, that.id) && // NOSONAR generated code that's easy to read
                Objects.equals(rowMetadata, that.rowMetadata) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(lastModificationDate, that.lastModificationDate) &&
                Objects.equals(dataSetId, that.dataSetId) &&
                Objects.equals(author, that.author) &&
                Objects.equals(name, that.name) &&
                Objects.equals(headId, that.headId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowMetadata, dataSetId, author, name, creationDate, lastModificationDate, headId);
    }
}
