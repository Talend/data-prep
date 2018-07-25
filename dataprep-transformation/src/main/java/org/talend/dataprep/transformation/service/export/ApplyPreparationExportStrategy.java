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

package org.talend.dataprep.transformation.service.export;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.dataprep.transformation.api.transformer.configuration.Configuration.Volume.SMALL;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.service.BaseExportStrategy;

import com.fasterxml.jackson.core.JsonParser;

/**
 * A {@link BaseExportStrategy strategy} to apply a preparation on a different dataset (different from the one initially
 * in the preparation).
 */
@Component
public class ApplyPreparationExportStrategy extends BaseSampleExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyPreparationExportStrategy.class);

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    private Boolean technicianIdentityReleased = true;

    @Override
    public boolean accept(ExportParameters parameters) {
        if (parameters == null) {
            return false;
        }
        // Valid if both data set and preparation are set.
        return parameters.getContent() == null //
                && !StringUtils.isEmpty(parameters.getDatasetId()) //
                && !StringUtils.isEmpty(parameters.getPreparationId());
    }

    @Override
    public StreamingResponseBody execute(ExportParameters parameters) {
        formatService.setExportHeaders(parameters);
        TransformationCacheKey key = cacheKeyGenerator.generateContentKey(parameters);
        return outputStream -> doExecute(parameters,
                new TeeOutputStream(outputStream, contentCache.put(key, ContentCache.TimeToLive.DEFAULT)), key);
    }

    @Override
    public void writeToCache(ExportParameters parameters, TransformationCacheKey key) {
        doExecute(parameters, contentCache.put(key, ContentCache.TimeToLive.DEFAULT), key);
    }

    private void doExecute(ExportParameters parameters, OutputStream outputStream, TransformationCacheKey key) {
        final String stepId = parameters.getStepId();
        final String preparationId = parameters.getPreparationId();
        final PreparationDTO preparation = getPreparation(preparationId);
        final String dataSetId = parameters.getDatasetId();
        final ExportFormat format = formatService.getFormat(parameters.getExportType());

        try {
            DataSet dataSet = getDatatset(parameters, dataSetId, preparationId);

            // head is not allowed as step id
            final String version = getCleanStepId(preparation, stepId);

            // create tee to broadcast to cache + service output

            // get the actions to apply (no preparation ==> dataset export ==> no actions)
            final String actions = getActions(preparationId, version);

            // create tee to broadcast to cache + service output
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Transformation Cache Key : {}", key.getKey());
                LOGGER.debug("Cache key details: {}", key);
            }

            final Configuration.Builder configurationBuilder = Configuration.builder() //
                    .args(parameters.getArguments()) //
                    .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                    .sourceType(parameters.getFrom()).format(format.getName()) //
                    .actions(actions) //
                    .preparation(getPreparation(preparationId)) //
                    .stepId(version) //
                    .volume(SMALL) //
                    .output(outputStream) //
                    .limit(this.limit);

            // no need for statistics if it's not JSON output
            if (!Objects.equals(format.getName(), JSON)) {
                configurationBuilder.globalStatistics(false);
            }

            final Configuration configuration = configurationBuilder.build();

            factory.get(configuration).buildExecutable(dataSet, configuration).execute();
            outputStream.flush();
        } catch (Exception e) { // NOSONAR
            LOGGER.debug("evicting cache {}", key.getKey());
            contentCache.evict(key);
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Return the dataset sample.
     *
     * @param parameters the export parameters
     * @param dataSetId the id of the corresponding dataset
     * @param preparationId the id of the corresponding preparation
     *
     * @return the dataset sample either from cache if the key corresponding key exists either the full sample.
     */
    private DataSet getDatatset(ExportParameters parameters, String dataSetId, String preparationId) throws IOException {

        final ContentCacheKey asyncSampleKey = cacheKeyGenerator.generateContentKey(//
                dataSetId, //
                preparationId, //
                Step.ROOT_STEP.id(), //
                JSON, //
                parameters.getFrom(), //
                parameters.getFilter() //
        );
        LOGGER.info("using {} as content input", asyncSampleKey.getKey());

        if (contentCache.has(asyncSampleKey)) {
            JsonParser parser = mapper.getFactory().createParser(new InputStreamReader(contentCache.get(asyncSampleKey), UTF_8));
            return mapper.readerFor(DataSet.class).readValue(parser);
        }

        final boolean fullContent = parameters.getFrom() == ExportParameters.SourceType.FILTER;
        // dataset content must be retrieved as the technical user because it might not be shared
        technicianIdentityReleased = false;
        securityProxy.asTechnicalUserForDataSet();

        DataSet dataSet = datasetClient.getDataSet(dataSetId, fullContent, false, null);
        securityProxy.releaseIdentity();
        technicianIdentityReleased = true;
        return dataSet;
    }
}
