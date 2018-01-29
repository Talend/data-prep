//  ============================================================================
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.util;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.api.transformer.ConfiguredCacheWriter;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.InitialTransformationMetadataCacheKey;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.model.WriterNode;
import org.talend.dataprep.transformation.service.TransformationRowMetadataUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MetadataGenerator {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataGenerator.class);

    @Autowired
    private CommandUtil commandUtil;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PipelineGenerator pipelineGenerator;

    @Autowired
    protected ContentCache contentCache;

    @Autowired
    protected CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private WriterRegistrationService writerRegistrationService;

    @Autowired
    private TransformationRowMetadataUtils transformationRowMetadataUtils;

    @Autowired
    protected ApplicationContext applicationContext;

    /** The transformer factory. */
    @Autowired
    protected TransformerFactory factory;

    public void generateMetadataForPreparation(String preparationId, String stepId, ContentCacheKey metadataCacheKey) {

        PreparationDetailsGet preparationDetailsGet = commandUtil.getPreparationDetailsCommand(preparationId);
        try (InputStream details = preparationDetailsGet.execute();
                JsonParser parser = mapper.getFactory().createParser(details)) {

            // get the preparation
            PreparationMessage preparation = mapper.readerFor(PreparationMessage.class).readValue(parser);

            // get content cache key
            final TransformationCacheKey contentCacheKey = cacheKeyGenerator.generateContentKey(preparation.getDataSetId(),
                    preparationId, stepId, "JSON", ExportParameters.SourceType.HEAD, null);

            // get metadata cache key
            final InitialTransformationMetadataCacheKey initialMetadataCacheKey =
                    cacheKeyGenerator.generateInitialMetadataKey(preparationId, stepId, null);

            // get dataset from cache
            DataSet dataSet = null;
            try {
                dataSet = getDataSetFromCache(contentCacheKey, initialMetadataCacheKey);
            } catch (IOException e) {
                LOGGER.warn("Cannot get initial data from cache. We relaunch the pipeline to generate it");
                dataSet = getDataSetFromPipeline(preparation, contentCacheKey, initialMetadataCacheKey);
            }

            try {

                // initialize the writer. We only want to write metadata on the cache with this method
                final TransformerWriter writer =
                        writerRegistrationService.getWriter("JSON", new NullOutputStream(), Collections.emptyMap());
                final ConfiguredCacheWriter metadataWriter =
                        new ConfiguredCacheWriter(contentCache, ContentCache.TimeToLive.DEFAULT);

                // prepare the fallback row metadata
                RowMetadata fallBackRowMetadata =
                        transformationRowMetadataUtils.getMatchingEmptyRowMetadata(preparation.getRowMetadata());

                final WriterNode writerNode = new WriterNode(writer, metadataWriter, metadataCacheKey, fallBackRowMetadata);

                Pipeline pipeline = pipelineGenerator.buildPipelineToCalculateMetadata(writerNode);
                pipeline.execute(dataSet);

            } catch (Throwable e) { // NOSONAR
                contentCache.evict(metadataCacheKey);
                throw e;
            }

        } catch (Exception e) {
            LOGGER.error("Unable to read preparation {}", preparationId, e);
        }
    }

    private DataSet getDataSetFromPipeline(PreparationMessage preparation, TransformationCacheKey contentCacheKey,
            InitialTransformationMetadataCacheKey initialMetadataCacheKey) {
        try {
            this.launchPreparationPipeline(preparation, contentCacheKey, initialMetadataCacheKey);
            return getDataSetFromCache(contentCacheKey, initialMetadataCacheKey);
        } catch (IOException e) {
            LOGGER.error("Cannot get dataset from cache but we just relaunch the pipeline.", e);
            // Not expected: We've just ran a transformation, yet no cached?
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_COMPUTE_DATASET_ACTIONS);
        }
    }

    private DataSet getDataSetFromCache(ContentCacheKey contentCacheKey, ContentCacheKey metadataCacheKey) throws IOException {
        // get dataset
        JsonParser parser = mapper.getFactory().createParser(contentCache.get(contentCacheKey));
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

        // get metadata
        parser = mapper.getFactory().createParser(contentCache.get(metadataCacheKey));
        DataSetMetadata metadata = mapper.readerFor(DataSetMetadata.class).readValue(parser);

        // link and return
        dataSet.setMetadata(metadata);
        return dataSet;
    }

    private void launchPreparationPipeline(PreparationMessage preparation, ContentCacheKey contentCacheKey,
            ContentCacheKey metadataCacheKey) {

        // initialize the writer. We want to write both transformation and metadata into the cache
        final TransformerWriter writer = writerRegistrationService.getWriter("JSON",
                contentCache.put(contentCacheKey, ContentCache.TimeToLive.DEFAULT), Collections.emptyMap());
        final ConfiguredCacheWriter metadataWriter = new ConfiguredCacheWriter(contentCache, ContentCache.TimeToLive.DEFAULT);

        // prepare the fallback row metadata
        RowMetadata fallBackRowMetadata =
                transformationRowMetadataUtils.getMatchingEmptyRowMetadata(preparation.getRowMetadata());

        // creating the pipeline
        String dataSetId = preparation.getDataSetId();
        final WriterNode writerNode = new WriterNode(writer, metadataWriter, metadataCacheKey, fallBackRowMetadata);

        Pipeline pipeline = pipelineGenerator.buildPipelineToApplyActionsFromPreparation(preparation, r -> true,
                preparation.getRowMetadata(), false, false, false, writerNode);

        final DataSetGet dataSetGet = commandUtil.getDataSet(dataSetId);
        try (InputStream datasetContent = dataSetGet.execute();
                JsonParser parser = mapper.getFactory().createParser(datasetContent)) {

            DataSet dataset = mapper.readerFor(DataSet.class).readValue(parser);

            pipeline.execute(dataset);

        } catch (Exception e) {
            LOGGER.error("Unable to read dataset {}", dataSetId, e);
            throw new TDPException(UNABLE_TO_READ_DATASET_CONTENT, e, build().put("id", dataSetId));
        }

    }

}
