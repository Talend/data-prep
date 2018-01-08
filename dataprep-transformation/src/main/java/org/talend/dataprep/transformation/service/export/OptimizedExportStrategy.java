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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.format.CSVFormat;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportUtils;

import com.fasterxml.jackson.core.JsonParser;

/**
 * A {@link BaseExportStrategy strategy} to export a preparation (using its default data set), using any information
 * available in cache (metadata and content).
 */
@Component
public class OptimizedExportStrategy extends BaseSampleExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizedExportStrategy.class);

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Override
    public boolean accept(ExportParameters parameters) {
        if (parameters == null) {
            return false;
        }
        if (parameters.getContent() != null) {
            return false;
        }

        if (StringUtils.isEmpty(parameters.getPreparationId())){
            return false;
        }
        final OptimizedPreparationInput optimizedPreparationInput = new OptimizedPreparationInput(parameters);
        return optimizedPreparationInput.applicable();
    }

    @Override
    public StreamingResponseBody execute(ExportParameters parameters) {
        final String formatName = parameters.getExportType();
        final ExportFormat format = getFormat(formatName);
        ExportUtils.setExportHeaders(parameters.getExportName(), //
                parameters.getArguments().get(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING), //
                format);

        return outputStream -> performOptimizedTransform(parameters, outputStream);
    }

    private void performOptimizedTransform(ExportParameters parameters, OutputStream outputStream) throws IOException {
        // Initial check
        final OptimizedPreparationInput optimizedPreparationInput = new OptimizedPreparationInput(parameters).invoke();
        if (optimizedPreparationInput == null) {
            throw new IllegalStateException("Unable to use this strategy (call accept() before calling this).");
        }
        final String preparationId = parameters.getPreparationId();
        final String dataSetId = optimizedPreparationInput.getDataSetId();
        final TransformationCacheKey transformationCacheKey = optimizedPreparationInput.getTransformationCacheKey();
        final DataSetMetadata metadata = optimizedPreparationInput.getMetadata();
        final String previousVersion = optimizedPreparationInput.getPreviousVersion();
        final String version = optimizedPreparationInput.getVersion();
        final ExportFormat format = getFormat(parameters.getExportType());

        // Get content from previous step
        try (JsonParser parser = mapper.getFactory().createParser(contentCache.get(transformationCacheKey))) {
            // Create dataset
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            dataSet.setMetadata(metadata);

            // get the actions to apply (no preparation ==> dataset export ==> no actions)
            final String actions = getActions(preparationId, previousVersion, version);
            final PreparationMessage preparation = getPreparation(preparationId);
            preparation.setSteps(getMatchingSteps(preparation.getSteps(), previousVersion, version));

            LOGGER.debug("Running optimized strategy for preparation {} @ step #{}", preparationId, version);

            // create tee to broadcast to cache + service output
            final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                    dataSetId, //
                    preparationId, //
                    version, //
                    parameters.getExportType(), //
                    parameters.getFrom(), //
                    parameters.getArguments(), //
                    parameters.getFilter() //
            );
            LOGGER.debug("Cache key: " + key.getKey());
            LOGGER.debug("Cache key details: " + key.toString());

            try (final TeeOutputStream tee = new TeeOutputStream(outputStream, contentCache.put(key, ContentCache.TimeToLive.DEFAULT))) {
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
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }
    }

    /**
     * Return the steps that are between the from and the to steps IDs.
     *
     * @param steps the steps to start from.
     * @param fromId the from step id.
     * @param toId the to step id.
     * @return the steps that are between the from and the to steps IDs.
     */
    private List<Step> getMatchingSteps(List<Step> steps, String fromId, String toId) {
        List<Step> result = new ArrayList<>();
        boolean addStep = false;
        for (Step step : steps) {
            // skip steps before the from
            if (fromId.equals(step.id())) {
                addStep = true;
            } else if (addStep) { // fromId should not be added, hence the else !
                result.add(step);
            }
            // skip steps after
            if (addStep && toId.equals(step.getId())) {
                break;
            }
        }
        LOGGER.debug("Matching steps from {} to {} are {}", fromId, toId, steps);
        return result;
    }

    /**
     * A utility class to both extract information to run optimized strategy <b>and</b> check if there's enough information
     * to use the strategy.
     */
    private class OptimizedPreparationInput {

        private final String stepId;

        private final String preparationId;

        private final String dataSetId;

        private final String formatName;

        private final Preparation preparation;

        private final ExportParameters.SourceType sourceType;

        private String version;

        private DataSetMetadata metadata;

        private TransformationCacheKey transformationCacheKey;

        private String previousVersion;

        private String filter;

        private OptimizedPreparationInput(ExportParameters parameters) {
            this.stepId = parameters.getStepId();
            this.preparationId = parameters.getPreparationId();
            this.sourceType = parameters.getFrom();
            if (preparationId != null) {
                this.preparation = getPreparation(preparationId);
            } else {
                preparation = null;
            }
            if (StringUtils.isEmpty(parameters.getDatasetId()) && preparation != null) {
                this.dataSetId = preparation.getDataSetId();
            } else {
                this.dataSetId = parameters.getDatasetId();
            }
            this.formatName = parameters.getExportType();
            this.filter = parameters.getFilter();
        }

        private String getDataSetId() {
            return dataSetId;
        }

        private String getVersion() {
            return version;
        }

        private DataSetMetadata getMetadata() {
            return metadata;
        }

        private String getFilter() {
            return filter;
        }

        private TransformationCacheKey getTransformationCacheKey() {
            return transformationCacheKey;
        }

        private boolean applicable() {
            try {
                return invoke() != null;
            } catch (IOException e) {
                LOGGER.debug("Unable to check if optimized preparation path is applicable.", e);
                return false;
            }
        }

        private String getPreviousVersion() {
            return previousVersion;
        }

        // Extract information or returns null is not applicable.
        private OptimizedPreparationInput invoke() throws IOException {
            if (preparation == null) {
                // Not applicable (need preparation to work on).
                return null;
            }
            // head is not allowed as step id
            version = stepId;
            previousVersion = Step.ROOT_STEP.getId();
            final List<String> steps = preparation.getSteps().stream().map(Step::id).collect(Collectors.toList());
            if (steps.size() <= 2) {
                LOGGER.debug("Not enough steps ({}) in preparation.", steps.size());
                return null;
            }
            if (StringUtils.equals("head", stepId) || StringUtils.isEmpty(stepId)) {
                version = steps.get(steps.size() - 1);
                previousVersion = steps.get(steps.size() - 2);
            }
            // Get metadata of previous step
            final TransformationMetadataCacheKey transformationMetadataCacheKey = cacheKeyGenerator.generateMetadataKey(preparationId, previousVersion, sourceType);
            if (!contentCache.has(transformationMetadataCacheKey)) {
                LOGGER.debug("No metadata cached for previous version '{}' (key for lookup: '{}')", previousVersion,
                        transformationMetadataCacheKey.getKey());
                return null;
            }
            try (InputStream input = contentCache.get(transformationMetadataCacheKey)) {
                metadata = mapper.readerFor(DataSetMetadata.class).readValue(input);
            }
            transformationCacheKey = cacheKeyGenerator.generateContentKey( //
                    dataSetId, //
                    preparationId, //
                    previousVersion, //
                    formatName, //
                    sourceType, //
                    filter //
            );
            LOGGER.debug("Previous content cache key: " + transformationCacheKey.getKey());
            LOGGER.debug("Previous content cache key details: " + transformationCacheKey.toString());

            if (!contentCache.has(transformationCacheKey)) {
                LOGGER.debug("No content cached for previous version '{}'", previousVersion);
                return null;
            }
            return this;
        }
    }

}
