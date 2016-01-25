package org.talend.dataprep.api.dataset;

import java.io.Serializable;
import java.util.*;

import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;
import org.talend.dataprep.schema.SchemaParserResult;
import org.talend.dataprep.schema.csv.CSVSerializer;

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
 * @see DataSetMetadata.Builder
 */
public class DataSetMetadata implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The dataset id. */
    @Id
    private String id;

    /** Row description. */
    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(value = "columns", required = false)
    private RowMetadata rowMetadata;

    /** Dataset life cycle status. */
    @JsonProperty("lifecycle")
    private final DataSetLifecycle lifecycle = new DataSetLifecycle();

    @JsonProperty("content")
    @JsonUnwrapped
    private DataSetContent content = new DataSetContent();

    /** Dataset governance. */
    @JsonProperty("governance")
    @JsonUnwrapped
    private final DataSetGovernance governance = new DataSetGovernance();

    /** Dataset location. */
    @JsonProperty("location")
    private DataSetLocation location;

    /** Dataset name. */
    @JsonProperty("name")
    private String name;

    /** Dataset author. */
    @JsonProperty("author")
    private String author;

    @JsonProperty("created")
    private long creationDate;

    /** Sheet number in case of excel source. */
    @JsonProperty("sheetName")
    private String sheetName;

    /**
     * if <code>true</code> this dataset is still a draft as we need more information from the user
     */
    @JsonProperty("draft")
    private boolean draft = false;

    /**
     * available only when draft is <code>true</code> i.e until some information has been confirmed by the user
     */
    @JsonProperty("schemaParserResult")
    private SchemaParserResult schemaParserResult;

    /**
     * flag to tell the dataset is one of the favorites for the current user this value is sent back to front but not
     * stored because it stored in another user related storage
     */
    @JsonProperty("favorite")
    private transient boolean favorite;

    /**
     * indicates what encoding should be used to read raw content. Defaults to UTF-8 but may be changed depending on
     * content.
     * @see CSVSerializer#serialize(java.io.InputStream, org.talend.dataprep.api.dataset.DataSetMetadata)
     */
    @JsonProperty("encoding")
    private String encoding = "UTF-8";

    /**
     * Default empty constructor.
     */
    public DataSetMetadata() {
        // no op
    }

    /**
     * Constructor.
     * 
     * @param id dataset id.
     * @param name dataset name.
     * @param author dataset author.
     * @param creationDate dataset creation date.
     * @param rowMetadata row metadata.
     */
    public DataSetMetadata(String id, String name, String author, long creationDate, RowMetadata rowMetadata) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.creationDate = creationDate;
        this.rowMetadata = rowMetadata;
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
     * @return the dataset creation date.
     */
    public long getCreationDate() {
        return creationDate;
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
    public SchemaParserResult getSchemaParserResult() {
        return schemaParserResult;
    }

    /**
     * @param schemaParserResult the schema parser result to set.
     */
    public void setSchemaParserResult(SchemaParserResult schemaParserResult) {
        this.schemaParserResult = schemaParserResult;
    }

    /**
     * Getter for favorite.
     * 
     * @return the favorite
     */
    public boolean isFavorite() {
        return this.favorite;
    }

    /**
     * @return The data set content's encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Changes the encoding of the data set content.
     * @param encoding The new encoding. Must be supported by current JVM.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the favorite.
     *
     * @param favorite the favorite to set
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString()
    {
        return "DataSetMetadata{" +
            "id='" + id + '\'' +
            ", rowMetadata=" + rowMetadata +
            ", lifecycle=" + lifecycle +
            ", content=" + content +
            ", governance=" + governance +
            ", name='" + name + '\'' +
            ", author='" + author + '\'' +
            ", creationDate=" + creationDate +
            ", sheetName='" + sheetName + '\'' +
            ", draft=" + draft +
            ", schemaParserResult=" + schemaParserResult +
            ", favorite=" + favorite +
            '}';
    }

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
                Objects.equals(draft, that.draft) && //
                Objects.equals(id, that.id) && //
                Objects.equals(rowMetadata, that.rowMetadata) && //
                Objects.equals(lifecycle, that.lifecycle) && //
                Objects.equals(content, that.content) && //
                Objects.equals(governance, that.governance) && //
                Objects.equals(location, that.location) && //
                Objects.equals(name, that.name) && //
                Objects.equals(author, that.author) && //
                Objects.equals(sheetName, that.sheetName) && //
                Objects.equals(schemaParserResult, that.schemaParserResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowMetadata, lifecycle, content, governance, location, name, author, creationDate, sheetName,
                draft, schemaParserResult, favorite);
    }

    /**
     * @see Object#clone()
     */
    @Override
    public DataSetMetadata clone() {
        return Builder.metadata().copy(this).build();
    }

    /**
     * Dataset builder.
     */
    public static class Builder {

        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#id */
        private String id;
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#author */
        private String author = "anonymous";
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#name */
        private String name = "";
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#creationDate */
        private long createdDate = System.currentTimeMillis();
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#sheetName */
        private String sheetName;
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#draft */
        private boolean draft = true;
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#favorite */
        private boolean isFavorite;
        /** @see org.talend.dataprep.api.dataset.DataSetMetadata#location */
        private DataSetLocation location = new LocalStoreLocation();

        /** @see org.talend.dataprep.api.dataset.DataSetContent#nbRecords */
        private long size;

        /**
         * @see org.talend.dataprep.api.dataset.DataSetContent#limit
         */
        private Long limit = null;
        /** @see org.talend.dataprep.api.dataset.DataSetContent#nbLinesInHeader */
        private int headerSize;
        /** @see org.talend.dataprep.api.dataset.DataSetContent#nbLinesInFooter */
        private int footerSize;
        /** @see org.talend.dataprep.api.dataset.DataSetContent#formatGuessId */
        private String formatGuessId;
        /** @see org.talend.dataprep.api.dataset.DataSetContent#mediaType */
        private String mediaType;

        /**
         * @see org.talend.dataprep.api.dataset.DataSetContent#parameters
         */
        private Map<String, String> parameters = new HashMap<>();

        /** @see org.talend.dataprep.api.dataset.DataSetLifecycle#contentAnalyzed */
        private boolean contentAnalyzed;
        /** @see org.talend.dataprep.api.dataset.DataSetLifecycle#schemaAnalyzed */
        private boolean schemaAnalyzed;

        /**
         * @see org.talend.dataprep.api.dataset.DataSetLifecycle#importing
         */
        private boolean importing;

        /**
         * @see org.talend.dataprep.api.dataset.DataSetLifecycle#inProgress
         */
        private boolean inProgress = true;
        /** @see org.talend.dataprep.api.dataset.DataSetLifecycle#qualityAnalyzed */
        private boolean qualityAnalyzed;

        /**
         * @see org.talend.dataprep.api.dataset.DataSetGovernance#certificationStep
         */
        private DataSetGovernance.Certification certificationStep;

        /**
         * @see DataSetMetadata#schemaParserResult
         */
        private SchemaParserResult schemaParserResult;

        /** Dataset builder. */
        private ColumnMetadata.Builder[] columnBuilders;

        /** Encoding of data set content */
        private String encoding = "UTF8";

        public static DataSetMetadata.Builder metadata() {
            return new Builder();
        }

        public DataSetMetadata.Builder id(String id) {
            this.id = id;
            return this;
        }

        public DataSetMetadata.Builder author(String author) {
            this.author = author;
            return this;
        }

        public DataSetMetadata.Builder name(String name) {
            this.name = name;
            return this;
        }

        public DataSetMetadata.Builder created(long createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public DataSetMetadata.Builder row(ColumnMetadata.Builder... columns) {
            columnBuilders = columns;
            return this;
        }

        public DataSetMetadata.Builder size(long size) {
            this.size = size;
            return this;
        }

        public DataSetMetadata.Builder limit(Long limit) {
            this.limit = limit;
            return this;
        }

        public DataSetMetadata.Builder parameter(String name, String value) {
            this.parameters.put(name, value);
            return this;
        }

        public DataSetMetadata.Builder headerSize(int headerSize) {
            this.headerSize = headerSize;
            return this;
        }

        public DataSetMetadata.Builder footerSize(int footerSize) {
            this.footerSize = footerSize;
            return this;
        }

        public Builder contentAnalyzed(boolean contentAnalyzed) {
            this.contentAnalyzed = contentAnalyzed;
            return this;
        }

        public Builder schemaAnalyzed(boolean schemaAnalyzed) {
            this.schemaAnalyzed = schemaAnalyzed;
            return this;
        }

        public Builder qualityAnalyzed(boolean qualityAnalyzed) {
            this.qualityAnalyzed = qualityAnalyzed;
            return this;
        }

        public Builder importing(boolean importing) {
            this.importing = importing;
            return this;
        }

        public Builder inProgress(boolean inProgress) {
            this.inProgress = inProgress;
            return this;
        }

        public Builder sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder draft(boolean draft) {
            this.draft = draft;
            return this;
        }

        public Builder formatGuessId(String formatGuessId) {
            this.formatGuessId = formatGuessId;
            return this;
        }

        public Builder mediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public DataSetMetadata.Builder isFavorite(boolean isFavorite) {
            this.isFavorite = isFavorite;
            return this;
        }

        public DataSetMetadata.Builder location(DataSetLocation location) {
            this.location = location;
            return this;
        }

        public DataSetMetadata.Builder certificationStep(DataSetGovernance.Certification certificationStep) {
            this.certificationStep = certificationStep;
            return this;
        }

        public DataSetMetadata.Builder schemaParserResult(SchemaParserResult schemaParserResult) {
            this.schemaParserResult = schemaParserResult;
            return this;
        }

        public DataSetMetadata.Builder copy(DataSetMetadata original) {
            this.id = original.getId();
            this.author = original.getAuthor();
            this.name = original.getName();
            this.createdDate = original.getCreationDate();
            this.sheetName = original.getSheetName();
            this.draft = original.isDraft();
            this.isFavorite = original.isFavorite();
            this.location = original.getLocation();
            this.size = original.getContent().getNbRecords();
            if (original.getContent().getLimit().isPresent()) {
                this.limit = original.getContent().getLimit().get();
            }
            this.headerSize = original.getContent().getNbLinesInHeader();
            this.footerSize = original.getContent().getNbLinesInFooter();
            this.formatGuessId = original.getContent().getFormatGuessId();
            this.mediaType = original.getContent().getMediaType();
            this.contentAnalyzed = original.getLifecycle().contentIndexed();
            this.qualityAnalyzed = original.getLifecycle().qualityAnalyzed();
            this.schemaAnalyzed = original.getLifecycle().schemaAnalyzed();
            this.inProgress = original.getLifecycle().inProgress();
            this.importing = original.getLifecycle().importing();
            this.parameters = original.getContent().getParameters();
            this.encoding = original.getEncoding();
            List<ColumnMetadata.Builder> builders = new ArrayList<>();
            if (original.getRowMetadata() != null) {
                for (ColumnMetadata col : original.getRowMetadata().getColumns()) {
                    builders.add(ColumnMetadata.Builder.column().copy(col));
                }
            }
            this.columnBuilders = builders.toArray(new ColumnMetadata.Builder[builders.size()]);
            this.certificationStep = original.getGovernance().getCertificationStep();
            this.schemaParserResult = original.getSchemaParserResult();
            return this;
        }

        public DataSetMetadata build() {
            if (id == null) {
                throw new IllegalStateException("No id set for dataset.");
            }
            List<ColumnMetadata> columns;
            if (columnBuilders != null) {
                columns = new ArrayList<>();
                for (ColumnMetadata.Builder columnBuilder : columnBuilders) {
                    columns.add(columnBuilder.build());
                }
            } else {
                columns = Collections.emptyList();
            }
            RowMetadata row = new RowMetadata(columns);
            DataSetMetadata metadata = new DataSetMetadata(id, name, author, createdDate, row);
            metadata.sheetName = this.sheetName;
            metadata.draft = this.draft;
            metadata.setFavorite(this.isFavorite);
            metadata.setLocation(this.location);
            if (this.certificationStep != null) {
                metadata.getGovernance().setCertificationStep(this.certificationStep);
            }
            metadata.setSchemaParserResult(this.schemaParserResult);

            // Content information
            metadata.setEncoding(encoding);
            DataSetContent currentContent = metadata.getContent();
            currentContent.setNbRecords(size);
            currentContent.setLimit(limit);
            currentContent.setNbLinesInHeader(headerSize);
            currentContent.setNbLinesInFooter(footerSize);
            currentContent.setParameters(parameters);

            if (formatGuessId != null) {
                currentContent.setFormatGuessId(formatGuessId);
            }
            currentContent.setMediaType(mediaType);

            // Lifecycle information
            DataSetLifecycle metadataLifecycle = metadata.getLifecycle();
            metadataLifecycle.contentIndexed(contentAnalyzed);
            metadataLifecycle.schemaAnalyzed(schemaAnalyzed);
            metadataLifecycle.qualityAnalyzed(qualityAnalyzed);
            metadataLifecycle.importing(importing);
            metadataLifecycle.inProgress(inProgress);
            return metadata;
        }
    }

}
