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

import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.io.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.service.BaseExportStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link BaseExportStrategy strategy} to export a preparation, using its default data set with
 * {@link ExportParameters.SourceType HEAD} sample.
 */
@Component
public class PreparationExportStrategy extends BaseSampleExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationExportStrategy.class);

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Override
    public boolean accept(final ExportParameters parameters) {
        if (parameters == null) {
            return false;
        }
        return parameters.getContent() == null //
                && (parameters.getFrom() == null || parameters.getFrom() == HEAD) //
                && !StringUtils.isEmpty(parameters.getPreparationId()) //
                && StringUtils.isEmpty(parameters.getDatasetId());
    }

    @Override
    public StreamingResponseBody execute(final ExportParameters parameters) {
        TransformationCacheKey key = cacheKeyGenerator.generateContentKey(parameters);
        formatService.setExportHeaders(parameters);
        return outputStream -> doExecute(parameters,
                new TeeOutputStream(outputStream, contentCache.put(key, ContentCache.TimeToLive.DEFAULT)), key);
    }

    @Override
    public void writeToCache(ExportParameters parameters, TransformationCacheKey key) {
        doExecute(parameters, contentCache.put(key, ContentCache.TimeToLive.DEFAULT), key);
    }

    public void doExecute(final ExportParameters parameters, OutputStream outputStream,
            TransformationCacheKey key) {
        final String stepId = parameters.getStepId();
        final String preparationId = parameters.getPreparationId();
        final PreparationMessage preparation = getPreparation(preparationId, stepId);
        final String dataSetId = preparation.getDataSetId();
        final ExportFormat format = formatService.getFormat(parameters.getExportType());

        boolean releasedIdentity = false;
        // Allow get dataset and get dataset metadata access whatever share status is
        securityProxy.asTechnicalUserForDataSet();
        try (DataSet dataSet = datasetClient.getDataSet(dataSetId)) {
            // head is not allowed as step id
            final String version = getCleanStepId(preparation, stepId);

            // All good, can already release identity
            securityProxy.releaseIdentity();
            releasedIdentity = true;

            // get the actions to apply (no preparation ==> dataset export ==> no actions)
            final String actions = getActions(preparationId, version);

            try {
                final Configuration configuration = Configuration
                        .builder() //
                        .args(parameters.getArguments()) //
                        .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                        .sourceType(parameters.getFrom())
                        .format(format.getName()) //
                        .actions(actions) //
                        .preparation(preparation) //
                        .stepId(version) //
                        .volume(Configuration.Volume.SMALL) //
                        .output(outputStream) //
                        .limit(limit) //
                        .build();
                factory.get(configuration).buildExecutable(dataSet, configuration).execute();
                outputStream.flush();
            } catch (Throwable e) { // NOSONAR
                contentCache.evict(key);
                throw e;
            } finally {
                outputStream.close();
            }
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        } finally {
            if (!releasedIdentity) {
                securityProxy.releaseIdentity(); // Release identity in case of error.
            }
        }
    }

    void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
