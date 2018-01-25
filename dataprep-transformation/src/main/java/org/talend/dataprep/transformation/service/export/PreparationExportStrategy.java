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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link BaseExportStrategy strategy} to export a preparation, using its default data set with {@link ExportParameters.SourceType HEAD} sample.
 */
@Component
public class PreparationExportStrategy extends BaseSampleExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationExportStrategy.class);

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private SecurityProxy securityProxy;

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
        final String formatName = parameters.getExportType();
        final ExportFormat format = getFormat(formatName);
        ExportUtils.setExportHeaders(parameters.getExportName(), //
                parameters.getArguments().get(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING), //
                format);

        return outputStream -> performPreparation(parameters, outputStream);
    }

    private void performPreparation(final ExportParameters parameters, final OutputStream outputStream) {
        final String stepId = parameters.getStepId();
        final String preparationId = parameters.getPreparationId();
        final String formatName = parameters.getExportType();
        final PreparationMessage preparation = getPreparation(preparationId, stepId);
        final String dataSetId = preparation.getDataSetId();
        final ExportFormat format = getFormat(parameters.getExportType());

        // get the dataset content (in an auto-closable block to make sure it is properly closed)
        boolean releasedIdentity = false;
        securityProxy.asTechnicalUser(); // Allow get dataset and get dataset metadata access whatever share status is
        final DataSetGet dataSetGet = applicationContext.getBean(DataSetGet.class, dataSetId, false, true);
        final DataSetGetMetadata dataSetGetMetadata = applicationContext.getBean(DataSetGetMetadata.class, dataSetId);
        try (InputStream datasetContent = dataSetGet.execute()) {
            try (JsonParser parser = mapper.getFactory().createParser(datasetContent)) {
                // head is not allowed as step id
                final String version = getCleanStepId(preparation, stepId);

                // Create dataset
                final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                dataSet.setMetadata(dataSetGetMetadata.execute());

                // All good, can already release identity
                securityProxy.releaseIdentity();
                releasedIdentity = true;

                // get the actions to apply (no preparation ==> dataset export ==> no actions)
                final String actions = getActions(preparationId, version);

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
                    final Configuration configuration = Configuration.builder() //
                            .args(parameters.getArguments()) //
                            .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                            .sourceType(parameters.getFrom())
                            .format(format.getName()) //
                            .actions(actions) //
                            .preparation(preparation) //
                            .stepId(version) //
                            .volume(Configuration.Volume.SMALL) //
                            .output(tee) //
                            .limit(limit) //
                            .build();
                    factory.get(configuration).buildExecutable(dataSet, configuration).execute();
                    tee.flush();
                } catch (Throwable e) { // NOSONAR
                    contentCache.evict(key);
                    throw e;
                }
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
