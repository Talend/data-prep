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

import static org.talend.dataprep.transformation.api.transformer.configuration.Configuration.Volume.SMALL;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

/**
 * A {@link BaseExportStrategy strategy} to apply a preparation on a different dataset
 * (A dataset different from the one initially in the preparation).
 */
@Component
public class ApplyPreparationExportStrategy extends BaseSampleExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyPreparationExportStrategy.class);

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private DatasetClient dataSetClient;

    private Boolean technicianIdentityReleased = true;

    @Override
    public boolean test(ExportParameters parameters) {
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
        final String formatName = parameters.getExportType();
        final ExportFormat format = getFormat(formatName);
        ExportUtils.setExportHeaders(parameters.getExportName(), //
                parameters.getArguments().get(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING), //
                format);

        return outputStream -> executeApplyPreparation(parameters, outputStream);
    }

    private void executeApplyPreparation(ExportParameters parameters, OutputStream outputStream) {
        final String stepId = parameters.getStepId();
        final String preparationId = parameters.getPreparationId();
        final String formatName = parameters.getExportType();
        final PreparationDTO preparation = getPreparation(preparationId);
        final String dataSetId = parameters.getDatasetId();

        try (DataSet dataSet = getDataset(parameters, dataSetId)) {

            // head is not allowed as step id
            final String version = getCleanStepId(preparation, stepId);

            // create tee to broadcast to cache + service output
            final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                    dataSetId, //
                    preparationId, //
                    version, //
                    formatName, //
                    parameters.getFrom(), //
                    parameters.getArguments(), //
                    parameters.getFilter() //
            );

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Transformation Cache Key : {}", key.getKey());
                LOGGER.debug("Cache key details: {}", key);
            }

            executePipeline(parameters, outputStream, key, preparationId, version, dataSet);

        } finally {
            if (!technicianIdentityReleased) {
                securityProxy.releaseIdentity();
            }
        }
    }

    /**
     * Return the dataset sample.
     *
     * @param parameters the export parameters
     * @param dataSet the sample
     * @param preparationId the id of the corresponding preparation
     *
     */
    private void executePipeline(ExportParameters parameters, OutputStream outputStream, TransformationCacheKey key,
            String preparationId, String version, DataSet dataSet) {

        final ExportFormat format = getFormat(parameters.getExportType());
        // get the actions to apply (no preparation ==> dataset export ==> no actions)
        final String actions = getActions(preparationId, version);

        try (final TeeOutputStream tee =
                new TeeOutputStream(outputStream, contentCache.put(key, ContentCache.TimeToLive.DEFAULT))) {
            final Configuration.Builder configurationBuilder = Configuration
                    .builder() //
                    .args(parameters.getArguments()) //
                    .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                    .sourceType(parameters.getFrom())
                    .format(format.getName()) //
                    .actions(actions) //
                    .preparation(getPreparation(preparationId)) //
                    .stepId(version) //
                    .volume(SMALL) //
                    .output(tee) //
                    .limit(this.limit);

            // no need for statistics if it's not JSON output
            if (!Objects.equals(format.getName(), JSON)) {
                configurationBuilder.globalStatistics(false);
            }

            final Configuration configuration = configurationBuilder.build();

            factory.get(configuration).buildExecutable(dataSet, configuration).execute();

            tee.flush();

        } catch (IOException e1) { // NOSONAR
            LOGGER.debug("evicting cache {}", key.getKey());
            contentCache.evict(key);
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e1);
        }
    }

    /**
     * Return the dataset sample.
     *
     * @param parameters the export parameters
     * @param dataSetId the id of the corresponding dataset
     * @return the dataset sample either from cache if the key corresponding key exists either the full sample.
     */
    private DataSet getDataset(ExportParameters parameters, String dataSetId) {
        final boolean fullContent = parameters.getFrom() == ExportParameters.SourceType.FILTER;
        // dataset content must be retrieved as the technical user because it might not be shared
        DataSet dataSet;
        try {
            securityProxy.asTechnicalUserForDataSet();
            dataSet = dataSetClient.getDataSet(dataSetId, fullContent, true);
        } finally {
            securityProxy.releaseIdentity();
        }
        return dataSet;
    }
}
