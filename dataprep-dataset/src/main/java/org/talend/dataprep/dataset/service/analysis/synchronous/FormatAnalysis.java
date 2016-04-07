// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.service.analysis.synchronous;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.configuration.EncodingSupport;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.schema.*;

/**
 * <p>
 * Analyzes the raw content of a dataset and determine its format (XLS, CSV...).
 * </p>
 *
 * <p>
 * It also parses column name information. Once analyzed, data prep would know how to access content.
 * </p>
 */
@Component
public class FormatAnalysis implements SynchronousDataSetAnalyzer {

    /** This class' header. */
    private static final Logger LOG = LoggerFactory.getLogger(FormatAnalysis.class);

    /** DataSet Metadata repository. */
    @Autowired
    private DataSetMetadataRepository repository;

    /** DataSet content store. */
    @Autowired
    private ContentStoreRouter store;

    /**
     * Format guess factory.
     */
    @Autowired
    private FormatFamily.Factory formatFamilyFactory;

    @Autowired
    private CompositeFormatDetector detector;

    /** Bean that list supported encodings. */
    @Autowired
    private EncodingSupport encodings;

    /**
     * @see SynchronousDataSetAnalyzer#analyze(String)
     */
    @Override
    public void analyze(String dataSetId) {

        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }

        final Marker marker = Markers.dataset(dataSetId);

        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata != null) {

                Format detectedFormat;
                try (InputStream content = store.getAsRaw(metadata)) {
                    detectedFormat = detector.detect(content);
                } catch (IOException e) {
                    throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
                }

                LOG.debug(marker, "using {} to parse the dataset", detectedFormat);

                verifyFormat(metadata, detectedFormat);

                internalUpdateMetadata(metadata, detectedFormat);

                LOG.debug(marker, "format analysed for dataset");
            } else {
                LOG.info(marker, "Data set no longer exists.");
            }
        } finally {
            datasetLock.unlock();
        }
    }

    /**
     * Checks for format validity. Clean up and throw exception if the format is null or unsupported.
     * 
     * @param metadata the metadata of the dataset being imported
     * @param detectedFormat the detected format of the dataset
     */
    private void verifyFormat(DataSetMetadata metadata, Format detectedFormat) {

        TDPException hypotheticalException = null;
        Set<Charset> supportedEncodings = encodings != null ? encodings.getSupportedCharsets() : Collections.emptySet();
        if (detectedFormat == null
                || UnsupportedFormatFamily.class.isAssignableFrom(detectedFormat.getFormatFamily().getClass())) {
            hypotheticalException = new TDPException(DataSetErrorCodes.UNSUPPORTED_CONTENT);
        } else if (!supportedEncodings.contains(Charset.forName(detectedFormat.getEncoding()))) {
            hypotheticalException = new TDPException(DataSetErrorCodes.UNSUPPORTED_ENCODING);
        }
        if (hypotheticalException != null) {
            // Clean up content & metadata (don't keep invalid information)
            store.delete(metadata);
            repository.remove(metadata.getId());
            // Throw exception to indicate unsupported content
            throw hypotheticalException;
        }
    }

    /**
     * Update the given dataset metadata with the specified format.
     *
     * @param metadata the dataset metadata to update.
     * @param format the specified format used to update the dataset metadata
     */
    private void internalUpdateMetadata(DataSetMetadata metadata, Format format) {
        FormatFamily formatFamily = format.getFormatFamily();
        DataSetContent dataSetContent = metadata.getContent();

        final String mediaType = metadata.getLocation().toMediaType(format.getFormatFamily());
        dataSetContent.setFormatGuessId(formatFamily.getBeanId());
        dataSetContent.setMediaType(mediaType);
        metadata.setEncoding(format.getEncoding());

        parseColumnNameInformation(metadata.getId(), metadata, format);

        repository.add(metadata);
    }

    /**
     * Update the dataset schema information from its metadata.
     * 
     * @param original the orginal dataset metadata.
     * @param updated the dataset to update.
     */
    public void update(DataSetMetadata original, DataSetMetadata updated) {

        final Marker marker = Markers.dataset(updated.getId());

        FormatFamily formatFamily = formatFamilyFactory.getFormatFamily(original.getContent().getFormatGuessId());

        if (!formatFamily.getSchemaGuesser().accept(updated)) {
            LOG.debug(marker, "the schema cannot be updated");
            return;
        }

        // update the schema
        Format format = new Format(formatFamily, updated.getEncoding());
        internalUpdateMetadata(updated, format);

        LOG.debug(marker, "format updated for dataset");
    }

    /**
     * Parse and store column name information.
     *
     * @param dataSetId the dataset id.
     * @param metadata the dataset metadata to parse.
     * @param format the format guesser.
     */
    private void parseColumnNameInformation(String dataSetId, DataSetMetadata metadata, Format format) {

        final Marker marker = Markers.dataset(dataSetId);
        LOG.debug(marker, "Parsing column information...");
        try (InputStream content = store.getAsRaw(metadata)) {
            SchemaParser parser = format.getFormatFamily().getSchemaGuesser();

            Schema schema = parser.parse(new SchemaParser.Request(content, metadata));
            if (schema.draft()) {
                metadata.setSheetName(schema.getSheetContents().get(0).getName());
                metadata.setDraft(true);
                metadata.setSchemaParserResult(schema);
                repository.add(metadata);
                LOG.info(Markers.dataset(dataSetId), "format analysed");
                return;
            }
            metadata.setDraft(false);
            if (schema.getSheetContents().isEmpty()) {
                throw new IOException("Parser could not detect file format for " + metadata.getId());
            }
            metadata.getRowMetadata().setColumns(schema.getSheetContents().get(0).getColumnMetadatas());
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
        }
        LOG.debug(marker, "Parsed column information.");
    }

    @Override
    public int order() {
        return 0;
    }
}
