// ============================================================================
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

package org.talend.dataprep.transformation.api.transformer.json;

import static org.talend.dataprep.cache.ContentCache.TimeToLive.DEFAULT;
import static org.talend.dataprep.transformation.api.transformer.configuration.Configuration.Volume.SMALL;

import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.transformer.ConfiguredCacheWriter;
import org.talend.dataprep.transformation.api.transformer.ExecutableTransformer;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.InitialTransformationMetadataCacheKey;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.model.WriterNode;
import org.talend.dataprep.transformation.service.StepMetadataRepository;
import org.talend.dataprep.transformation.service.TransformationRowMetadataUtils;

@Component
public class PipelineTransformer implements Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineTransformer.class);

    @Autowired
    ActionParser actionParser;

    @Autowired
    ActionRegistry actionRegistry;

    @Autowired
    AnalyzerService analyzerService;

    @Autowired
    WriterRegistrationService writerRegistrationService;

    @Autowired
    StatisticsAdapter adapter;

    @Autowired
    ContentCache contentCache;

    @Autowired
    CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private TransformationRowMetadataUtils transformationRowMetadataUtils;

    @Autowired
    private StepMetadataRepository preparationUpdater;

    @Override
    public ExecutableTransformer buildExecutable(DataSet input, Configuration configuration) {
        final RowMetadata rowMetadata = input.getMetadata().getRowMetadata();

        // prepare the fallback row metadata
        RowMetadata fallBackRowMetadata = transformationRowMetadataUtils.getMatchingEmptyRowMetadata(rowMetadata);

        final TransformerWriter writer = writerRegistrationService.getWriter(configuration.formatId(), configuration.output(),
                configuration.getArguments());

        final ConfiguredCacheWriter metadataWriter = new ConfiguredCacheWriter(contentCache, DEFAULT);

        final InitialTransformationMetadataCacheKey metadataKey = cacheKeyGenerator.generateInitialMetadataKey(configuration.getPreparationId(),
                configuration.stepId(), configuration.getSourceType());

        //TODO: A verifier
        final PreparationMessage preparation = configuration.getPreparation();
        final Function<Step, RowMetadata> rowMetadataSupplier = s -> Optional.ofNullable(s.getRowMetadata()) //
                .map(id -> preparationUpdater.get(id)) //
                .orElse(null);

        final Pipeline pipeline = Pipeline.Builder.builder() //
                .withAnalyzerService(analyzerService) //
                .withActionRegistry(actionRegistry) //
                .withPreparation(preparation) //
                .withActions(actionParser.parse(configuration.getActions())) //
                .withInitialMetadata(rowMetadata, configuration.volume() == SMALL) //
                .withMonitor(configuration.getMonitor()) //
                .withFilter(configuration.getFilter()) //
                .withLimit(configuration.getLimit()) //
                .withFilterOut(configuration.getOutFilter()) //
                .withOutput(() -> new WriterNode(writer, metadataWriter, metadataKey, fallBackRowMetadata)) //
                .withStatisticsAdapter(adapter) //
                .withStepMetadataSupplier(rowMetadataSupplier) //
                .withGlobalStatistics(configuration.isGlobalStatistics()) //
                .allowMetadataChange(configuration.isAllowMetadataChange()) //
                .build();


        // wrap this transformer into an executable transformer
        return new ExecutableTransformer() {

            @Override
            public void execute() {
                try {
                    LOGGER.info("Before transformation: {}", pipeline);

                    System.out.println("pipeline = " + pipeline.getClass());

                    pipeline.execute(input);
                } finally {
                    LOGGER.info("After transformation: {}", pipeline);
                }

                if (preparation != null) {
                    final UpdatedStepVisitor visitor = new UpdatedStepVisitor(preparationUpdater);
                    pipeline.accept(visitor);
                }
            }

            @Override
            public void signal(Signal signal) {
                pipeline.signal(signal);
            }
        };
    }

    @Override
    public boolean accept(Configuration configuration) {
        return Configuration.class.equals(configuration.getClass());
    }

}
