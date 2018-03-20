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

package org.talend.dataprep.dataset;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.*;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.schema.Schema;

/**
 * <p>
 * Convenient class used to build DataSetMetadata.
 * </p>
 *
 * <p>
 * This class is <b>not</b> thread safe ! Use the metadata() to get as single instance.
 * </p>
 *
 * @see #metadata()
 */
@Component
public class DataSetMetadataBuilder {

    @Autowired
    private VersionService versionService;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#id
     */
    private String id;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#appVersion
     */
    private String appVersion;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#author
     */
    private String author = "anonymous";

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#name
     */
    private String name = "";

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#creationDate
     */
    private long createdDate = System.currentTimeMillis();

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#lastModificationDate
     */
    private long lastModificationDate = System.currentTimeMillis();

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#sheetName
     */
    private String sheetName;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#draft
     */
    private boolean draft = true;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#favorite
     */
    private boolean isFavorite;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetMetadata#location
     */
    private DataSetLocation location = new LocalStoreLocation();

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#nbRecords
     */
    private long size;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#limit
     */
    private Long limit;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#nbLinesInHeader
     */
    private int headerSize;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#nbLinesInFooter
     */
    private int footerSize;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#formatFamilyId
     */
    private String formatFamilyId;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#mediaType
     */
    private String mediaType;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetContent#parameters
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * @see org.talend.dataprep.api.dataset.DataSetLifecycle#contentAnalyzed
     */
    private boolean contentAnalyzed;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetLifecycle#schemaAnalyzed
     */
    private boolean schemaAnalyzed;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetLifecycle#importing
     */
    private boolean importing;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetLifecycle#inProgress
     */
    private boolean inProgress = true;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetLifecycle#qualityAnalyzed
     */
    private boolean qualityAnalyzed;

    /**
     * @see org.talend.dataprep.api.dataset.DataSetGovernance#certificationStep
     */
    private DataSetGovernance.Certification certificationStep;

    /**
     * @see DataSetMetadata#schemaParserResult
     */
    private Schema schemaParserResult;

    /** Dataset builder. */
    private ColumnMetadata.Builder[] columnBuilders;

    /** Encoding of data set content */
    private String encoding = "UTF8";

    /** Flag used to make sure the builder went through metadata(). */
    private boolean builtWithMetadata = false;

    private boolean sharedDataSet = false;

    private Owner owner;

    private Set<String> roles = new HashSet<>();

    private long dataSetSize;

    private String tag;

    /**
     * Private constructor to ensure IoC use.
     */
    private DataSetMetadataBuilder() {
        // private constructor to ensure the IoC
    }

    public DataSetMetadataBuilder metadata() {
        final DataSetMetadataBuilder builder = new DataSetMetadataBuilder();
        builder.builtWithMetadata = true;
        builder.appVersion = versionService.version().getVersionId();
        return builder;
    }

    public DataSetMetadataBuilder id(String id) {
        this.id = id;
        return this;
    }

    public DataSetMetadataBuilder appVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    public DataSetMetadataBuilder author(String author) {
        this.author = author;
        return this;
    }

    public DataSetMetadataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DataSetMetadataBuilder created(long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public DataSetMetadataBuilder modified(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    public DataSetMetadataBuilder row(ColumnMetadata.Builder... columns) {
        columnBuilders = columns;
        return this;
    }

    public DataSetMetadataBuilder size(long size) {
        this.size = size;
        return this;
    }

    public DataSetMetadataBuilder limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public DataSetMetadataBuilder parameter(String name, String value) {
        this.parameters.put(name, value);
        return this;
    }

    public DataSetMetadataBuilder headerSize(int headerSize) {
        this.headerSize = headerSize;
        return this;
    }

    public DataSetMetadataBuilder footerSize(int footerSize) {
        this.footerSize = footerSize;
        return this;
    }

    public DataSetMetadataBuilder contentAnalyzed(boolean contentAnalyzed) {
        this.contentAnalyzed = contentAnalyzed;
        return this;
    }

    public DataSetMetadataBuilder schemaAnalyzed(boolean schemaAnalyzed) {
        this.schemaAnalyzed = schemaAnalyzed;
        return this;
    }

    public DataSetMetadataBuilder qualityAnalyzed(boolean qualityAnalyzed) {
        this.qualityAnalyzed = qualityAnalyzed;
        return this;
    }

    public DataSetMetadataBuilder importing(boolean importing) {
        this.importing = importing;
        return this;
    }

    public DataSetMetadataBuilder inProgress(boolean inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    public DataSetMetadataBuilder sheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public DataSetMetadataBuilder draft(boolean draft) {
        this.draft = draft;
        return this;
    }

    public DataSetMetadataBuilder shared(boolean shared) {
        this.sharedDataSet = shared;
        return this;
    }

    public DataSetMetadataBuilder owner(Owner owner) {
        this.owner = owner;
        return this;
    }

    public DataSetMetadataBuilder roles(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public DataSetMetadataBuilder formatFamilyId(String formatGuessId) {
        this.formatFamilyId = formatGuessId;
        return this;
    }

    public DataSetMetadataBuilder mediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public DataSetMetadataBuilder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public DataSetMetadataBuilder isFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    public DataSetMetadataBuilder location(DataSetLocation location) {
        this.location = location;
        return this;
    }

    public DataSetMetadataBuilder certificationStep(DataSetGovernance.Certification certificationStep) {
        this.certificationStep = certificationStep;
        return this;
    }

    public DataSetMetadataBuilder schemaParserResult(Schema schemaParserResult) {
        this.schemaParserResult = schemaParserResult;
        return this;
    }

    public DataSetMetadataBuilder dataSetSize(long dataSetSize) {
        this.dataSetSize = dataSetSize;
        return this;
    }

    /**
     * Copies fields of the specified data set metadata that are not related to the content of the data set.
     *
     * @param original the specified data set metadata
     * @return the data set metadata builder obtained from the non-content-related fields of the specified data set metadata
     */
    public DataSetMetadataBuilder copyNonContentRelated(final DataSetMetadata original) {
        this.id = original.getId();
        this.appVersion = original.getAppVersion();
        this.author = original.getAuthor();
        this.name = original.getName();
        this.createdDate = original.getCreationDate();
        this.location = original.getLocation();
        this.lastModificationDate = original.getLastModificationDate();
        this.tag = original.getTag();
        return this;
    }

    /**
     * Copies fields of the specified data set metadata that are related to the content of the data set.
     *
     * @param original the specified data set metadata
     * @return the data set metadata builder obtained from the content-related fields of the specified data set metadata
     */
    public DataSetMetadataBuilder copyContentRelated(final DataSetMetadata original) {
        this.certificationStep = original.getGovernance().getCertificationStep();

        this.sheetName = original.getSheetName();
        this.draft = original.isDraft();
        this.dataSetSize = original.getDataSetSize();

        this.size = original.getContent().getNbRecords();
        if (original.getContent().getLimit().isPresent()) {
            this.limit = original.getContent().getLimit().get();
        }

        this.headerSize = original.getContent().getNbLinesInHeader();
        this.footerSize = original.getContent().getNbLinesInFooter();

        this.formatFamilyId = original.getContent().getFormatFamilyId();
        this.mediaType = original.getContent().getMediaType();
        this.parameters = original.getContent().getParameters();
        this.encoding = original.getEncoding();

        this.contentAnalyzed = original.getLifecycle().contentIndexed();
        this.qualityAnalyzed = original.getLifecycle().qualityAnalyzed();
        this.schemaAnalyzed = original.getLifecycle().schemaAnalyzed();
        this.inProgress = original.getLifecycle().isInProgress();
        this.importing = original.getLifecycle().isImporting();

        this.schemaParserResult = original.getSchemaParserResult();
        if (original.getRowMetadata() != null) {
            this.columnBuilders = original.getRowMetadata().getColumns().stream()
                    .map(col -> ColumnMetadata.Builder.column().copy(col)).toArray(ColumnMetadata.Builder[]::new);
        }
        return this;
    }

    /**
     * Initializes a data set metadata builder obtained from the specified data set metadata.
     *
     * @param original the specified data set metadata
     * @return the data set metadata builder obtained from the specified data set metadata
     */
    public DataSetMetadataBuilder copy(DataSetMetadata original) {
        copyNonContentRelated(original);
        copyContentRelated(original);
        return this;
    }

    public DataSetMetadataBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @return the dataset metadata.
     */
    public DataSetMetadata build() {

        if (!builtWithMetadata) {
            throw new IllegalStateException(
                    "DataSetMetadataBuilder is not thread safe, use metadata() to get your own instance.");
        }

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
        DataSetMetadata metadata = new DataSetMetadata(id, name, author, createdDate, lastModificationDate, row, appVersion);
        if (!StringUtils.isEmpty(tag)) {
            metadata.setTag(tag);
        }
        metadata.setSheetName(this.sheetName);
        metadata.setDraft(this.draft);
        metadata.setDataSetSize(this.dataSetSize);
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

        if (formatFamilyId != null) {
            currentContent.setFormatFamilyId(formatFamilyId);
        }
        currentContent.setMediaType(mediaType);

        // Lifecycle information
        DataSetLifecycle metadataLifecycle = metadata.getLifecycle();
        metadataLifecycle.contentIndexed(contentAnalyzed);
        metadataLifecycle.schemaAnalyzed(schemaAnalyzed);
        metadataLifecycle.qualityAnalyzed(qualityAnalyzed);
        metadataLifecycle.setImporting(importing);
        metadataLifecycle.setInProgress(inProgress);
        return metadata;
    }
}
