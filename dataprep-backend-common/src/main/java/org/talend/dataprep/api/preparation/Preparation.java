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

package org.talend.dataprep.api.preparation;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.talend.dataprep.api.dataset.RowMetadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Preparation class represents the series of {@link Step steps} one can apply on a dataset to transform it.
 */
public class Preparation extends Identifiable implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset id. */
    private String dataSetId;

    /** Metadata on which the preparation is based. **/
    @NotNull
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
    private List<Step> steps = Collections.singletonList(Step.ROOT_STEP);

    /** The user locking the preparation. */
    private BasicUserLock lock;

    /**
     * Default empty constructor.
     */
    public Preparation() {
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
     * Copy constructor.
     *
     * @param origin
     */
    public Preparation(Preparation origin) {
        this.dataSetId = origin.dataSetId;
        this.rowMetadata = origin.rowMetadata;
        this.author = origin.author;
        this.name = origin.name;
        this.creationDate = origin.creationDate;
        this.lastModificationDate = origin.lastModificationDate;
        this.headId = origin.headId;
        this.appVersion = origin.appVersion;
        this.steps = origin.steps;
    }

    /**
     * @return List of the steps id for this preparation.
     */
    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
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

    public BasicUserLock getLock() {
        return lock;
    }

    public void setLock(BasicUserLock lock) {
        this.lock = lock;
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
        merge.lock = other.lock != null ? other.lock : lock;
        return merge;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("dataSetId", dataSetId).append("author", author)
                .append("name", name).append("creationDate", creationDate).append("lastModificationDate", lastModificationDate)
                .append("headId", headId).toString();
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
                Objects.equals(rowMetadata, that.rowMetadata) && Objects.equals(creationDate, that.creationDate)
                && Objects.equals(lastModificationDate, that.lastModificationDate) && Objects.equals(dataSetId, that.dataSetId)
                && Objects.equals(author, that.author) && Objects.equals(name, that.name) && Objects.equals(headId, that.headId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowMetadata, dataSetId, author, name, creationDate, lastModificationDate, headId);
    }
}
