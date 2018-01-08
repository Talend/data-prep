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

import java.io.InputStream;
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
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

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
        final Preparation preparation = getPreparation(preparationId);
        final String dataSetId = parameters.getDatasetId();
        final ExportFormat format = getFormat(parameters.getExportType());

        // dataset content must be retrieved as the technical user because it might not be shared
        boolean technicianIdentityReleased = false;
        securityProxy.asTechnicalUser();
        // get the dataset content (in an auto-closable block to make sure it is properly closed)
        final boolean fullContent = parameters.getFrom() == ExportParameters.SourceType.FILTER;
        final DataSetGet dataSetGet = applicationContext.getBean(DataSetGet.class, dataSetId, fullContent, true);

        try (final InputStream datasetContent = dataSetGet.execute();
                final JsonParser parser = mapper.getFactory().createParser(datasetContent)) {

            // release the technical user identity
            securityProxy.releaseIdentity();
            technicianIdentityReleased = true;
            // head is not allowed as step id
            final String version = getCleanStepId(preparation, stepId);

            // Create dataset
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

            // get the actions to apply (no preparation ==> dataset export ==> no actions)
            final String actions = getActions(preparationId, version);

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
            LOGGER.debug("Cache key: " + key.getKey());
            LOGGER.debug("Cache key details: " + key.toString());
            try (final TeeOutputStream tee = new TeeOutputStream(outputStream,
                    contentCache.put(key, ContentCache.TimeToLive.DEFAULT))) {
                final Configuration.Builder configurationBuilder = Configuration.builder() //
                        .args(parameters.getArguments()) //
                        .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                        .sourceType(parameters.getFrom()).format(format.getName()) //
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
            } catch (Throwable e) { // NOSONAR
                LOGGER.debug("evicting cache {}", key.getKey());
                contentCache.evict(key);
                throw e;
            }
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        } finally {
            if (!technicianIdentityReleased) {
                securityProxy.releaseIdentity();
            }
        }
    }
}
