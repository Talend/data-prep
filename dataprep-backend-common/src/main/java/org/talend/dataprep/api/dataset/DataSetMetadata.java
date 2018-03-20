// ============================================================================
//
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

package org.talend.dataprep.api.dataset;

import java.io.Serializable;
import java.util.Objects;

import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.Serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Represents all information needed to look for a data set ({@link #getId()} as well as information inferred from data
 * set content:
 * <ul>
 * <li>Metadata information: see {@link #getRowMetadata()}</li>
 * <li>Current progress on content processing:: see {@link #getLifecycle()}</li>
 * </ul>
 *
 */
public class DataSetMetadata implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset id. */
    private String id;

    /** Row description. */
    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "columns")
    private RowMetadata rowMetadata;

    /** Dataset life cycle status. */
    @JsonProperty("lifecycle")
    private DataSetLifecycle lifecycle = new DataSetLifecycle();

    @JsonProperty("content")
    @JsonUnwrapped
    private DataSetContent content = new DataSetContent();

    /** Dataset governance. */
    @JsonProperty("governance")
    @JsonUnwrapped
    private DataSetGovernance governance = new DataSetGovernance();

    /** Dataset location. */
    @JsonProperty("location")
    private DataSetLocation location;

    /** Dataset name. */
    @JsonProperty("name")
    private String name;

    /** Dataset author ID. */
    @JsonProperty("author")
    private String author;

    @JsonProperty("created")
    private long creationDate;

    @JsonProperty("lastModificationDate")
    private long lastModificationDate;

    /** Sheet number in case of excel source. */
    @JsonProperty("sheetName")
    private String sheetName;

    /** The application version. */
    @JsonProperty("app-version")
    private String appVersion;

    /**
     * if <code>true</code> this dataset is still a draft as we need more information from the user
     */
    @JsonProperty("draft")
    private boolean draft = false;

    /**
     * available only when draft is <code>true</code> i.e until some information has been confirmed by the user
     */
    @JsonProperty("schemaParserResult")
    private Schema schemaParserResult;

    /**
     * indicates what encoding should be used to read raw content. Defaults to UTF-8 but may be changed depending on
     * content.
     *
     * @see Serializer#serialize(java.io.InputStream, DataSetMetadata, long)
     */
    @JsonProperty("encoding")
    private String encoding = "UTF-8";

    /**
     * Size of the data set, in bytes.
     */
    @JsonProperty("dataSetSize")
    private long dataSetSize;

    /** A arbitrary tag for the data set (used by studio on creation for a visual distinction). */
    private String tag;

    /**
     * Default empty constructor.
     */
    public DataSetMetadata() {
        // no op
    }

    /**
     * Protected constructor to make user users use the DataSetMetadataBuilder.
     *
     * @param id dataset id.
     * @param name dataset name.
     * @param author dataset author.
     * @param creationDate dataset creation date.
     * @param rowMetadata row metadata.
     * @param appVersion the application version.
     */
    public DataSetMetadata(String id, String name, String author, long creationDate, long lastModificationDate,
            RowMetadata rowMetadata, String appVersion) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.creationDate = creationDate;
        this.lastModificationDate = lastModificationDate;
        this.rowMetadata = rowMetadata;
        this.appVersion = appVersion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * @return the dataset id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the dataset row description.
     */
    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    public void setRowMetadata(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
    }

    /**
     * @return the dataset lifecycle.
     */
    public DataSetLifecycle getLifecycle() {
        return lifecycle;
    }

    /**
     * @param lifecycle the lifecycle to set.
     */
    public void setLifecycle(DataSetLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * @return the dataset content summary.
     */
    public DataSetContent getContent() {
        return content;
    }

    public void setContent(DataSetContent content) {
        this.content = content;
    }

    /**
     * @return the dataset governance.
     */
    public DataSetGovernance getGovernance() {
        return this.governance;
    }

    /**
     * @param governance the governance to set.
     */
    public void setGovernance(DataSetGovernance governance) {
        this.governance = governance;
    }

    /**
     * @return the Location.
     */
    public DataSetLocation getLocation() {
        return location;
    }

    /**
     * @param location the location to set.
     */
    public void setLocation(DataSetLocation location) {
        this.location = location;
    }

    /**
     * @return the dataset name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the dataset name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dataset author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the sheet name
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * @param sheetName the new sheet name
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * @return the dataset creation date in <strong>milliseconds</strong>.
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * @return the dataset last modification date in <strong>milliseconds</strong>.
     */
    public long getLastModificationDate() {
        return lastModificationDate;
    }

    /**
     * @param lastModificationDate the dataset last modification date in <strong>milliseconds</strong>.
     */
    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    /**
     * @return true if the dataset metadata is a draft.
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * @param draft The draft value to set.
     */
    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    /**
     * @return the schema parser result.
     */
    public Schema getSchemaParserResult() {
        return schemaParserResult;
    }

    /**
     * @param schemaParserResult the schema parser result to set.
     */
    public void setSchemaParserResult(Schema schemaParserResult) {
        this.schemaParserResult = schemaParserResult;
    }


    /**
     * @return The data set content's encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Changes the encoding of the data set content.
     *
     * @param encoding The new encoding. Must be supported by current JVM.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return the Version
     */
    public String getAppVersion() {
        return appVersion;
    }

    public long getDataSetSize() {
        return dataSetSize;
    }

    public void setDataSetSize(long dataSetSize) {
        this.dataSetSize = dataSetSize;
    }

    /**
     * @return The tag value for the data set metadata. This tag is for example used to distinguish data sets created by a job
     * from one created through UI.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Set a "tag" value on the data set metadata. This tag is for example used to distinguish data sets created by a job
     * from one created through UI.
     *
     * @param tag The tag value for the data set metadata.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returns true if this data set metadata is compatible with <tt>rowMetadata</tt> (they have same columns names and
     * same types and in the same order) and false otherwise.
     *
     * @param other the specified data set metadata
     * @return true if this data set metadata is similar with the specified one and false otherwise
     */
    public boolean compatible(DataSetMetadata other) {
        if (other == null) {
            return false;
        }
        return rowMetadata != null ? rowMetadata.compatible(other.getRowMetadata()) : rowMetadata == other.getRowMetadata();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "DataSetMetadata{" + //
                "id='" + id + '\'' + //
                ", rowMetadata=" + rowMetadata + //
                ", appVersion=" + appVersion + //
                ", lifecycle=" + lifecycle + //
                ", content=" + content + //
                ", dataSetSize=" + dataSetSize + //
                ", governance=" + governance + //
                ", name='" + name + '\'' + //
                ", author='" + author + '\'' + //
                ", creationDate=" + creationDate + //
                ", lastModificationDate=" + lastModificationDate + //
                ", sheetName='" + sheetName + '\'' + //
                ", draft=" + draft + //
                ", schemaParserResult=" + schemaParserResult + //
                '}';
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
        DataSetMetadata that = (DataSetMetadata) o;
        return Objects.equals(creationDate, that.creationDate) && //
                Objects.equals(lastModificationDate, that.lastModificationDate) && //
                Objects.equals(draft, that.draft) && //
                Objects.equals(id, that.id) && //
                Objects.equals(rowMetadata, that.rowMetadata) && //
                Objects.equals(lifecycle, that.lifecycle) && //
                Objects.equals(dataSetSize, that.dataSetSize) && //
                Objects.equals(content, that.content) && //
                Objects.equals(governance, that.governance) && //
                Objects.equals(location, that.location) && //
                Objects.equals(name, that.name) && //
                Objects.equals(author, that.author) && //
                Objects.equals(sheetName, that.sheetName) && //
                Objects.equals(schemaParserResult, that.schemaParserResult) && //
                Objects.equals(appVersion, that.appVersion);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, rowMetadata, lifecycle, dataSetSize, content, governance, location, name, author, creationDate,
                lastModificationDate, sheetName, draft, schemaParserResult, appVersion);
    }
}
