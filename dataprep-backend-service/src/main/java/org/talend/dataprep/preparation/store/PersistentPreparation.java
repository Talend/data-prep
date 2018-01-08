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

package org.talend.dataprep.preparation.store;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Version;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.BasicUserLock;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@link org.talend.dataprep.api.preparation.Preparation} for persistent storage.
 *
 * @see org.talend.dataprep.conversions.BeanConversionService
 * @see org.talend.dataprep.configuration.PreparationRepositoryConfiguration.PersistentPreparationConversions
 */
public class PersistentPreparation extends PersistentIdentifiable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

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

    /** The user locking the preparation. */
    private BasicUserLock lock;

    /**
     * Default empty constructor.
     */
    public PersistentPreparation() {
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

    public BasicUserLock getLock() {
        return lock;
    }

    public void setLock(BasicUserLock lock) {
        this.lock = lock;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("dataSetId", dataSetId).append("author", author)
                .append("name", name).append("creationDate", creationDate).append("lastModificationDate", lastModificationDate)
                .append("headId", headId).toString();
    }
}
