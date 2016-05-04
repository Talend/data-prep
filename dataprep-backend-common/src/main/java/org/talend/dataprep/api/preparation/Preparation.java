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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Preparation extends Identifiable implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset id. */
    private String dataSetId;

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

    /**
     * @return the HeadId
     */
    public String getHeadId() {
        return headId;
    }

    /**
     * @param headId the headId to set.
     */
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

    /**
     * @return the AppVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set.
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public void setId(String id) {
        // No op
    }

    @Override
    public String toString() {
        return "Preparation {" + //
                "name='" + name + '\'' + //
                ", id='" + id + '\'' + //
                ", dataSetId='" + dataSetId + '\'' + //
                ", author='" + author + '\'' + //
                ", creationDate=" + creationDate + //
                ", lastModificationDate=" + lastModificationDate + //
                ", headId='" + headId +"'}";
    }

    public void updateLastModificationDate() {
        this.lastModificationDate = System.currentTimeMillis();
    }

    public Preparation merge(Preparation other) {
        Preparation merge = new Preparation(id, other.getAppVersion());
        merge.dataSetId = other.dataSetId != null ? other.dataSetId : dataSetId;
        merge.author = other.author != null ? other.author : author;
        merge.name = other.name != null ? other.name : name;
        merge.creationDate = min(other.creationDate, creationDate);
        merge.lastModificationDate = max(other.lastModificationDate, lastModificationDate);
        merge.headId = other.headId != null ? other.headId : headId;
        return merge;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preparation that = (Preparation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(lastModificationDate, that.lastModificationDate) &&
                Objects.equals(dataSetId, that.dataSetId) &&
                Objects.equals(author, that.author) &&
                Objects.equals(name, that.name) &&
                Objects.equals(headId, that.headId);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, dataSetId, author, name, creationDate, lastModificationDate, headId);
    }
}
