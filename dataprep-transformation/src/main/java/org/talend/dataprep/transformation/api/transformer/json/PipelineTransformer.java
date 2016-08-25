package org.talend.dataprep.transformation.api.transformer.json;

import static org.talend.dataprep.cache.ContentCache.TimeToLive.DEFAULT;
import static org.talend.dataprep.transformation.api.transformer.configuration.Configuration.Volume.SMALL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.transformer.ConfiguredCacheWriter;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.model.WriterNode;
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

    @Override
    public void transform(DataSet input, Configuration configuration) {
        final RowMetadata rowMetadata = input.getMetadata().getRowMetadata();

        // prepare the fallback row metadata
        RowMetadata fallBackRowMetadata = transformationRowMetadataUtils.getMatchingEmptyRowMetadata(rowMetadata);

        final TransformerWriter writer = writerRegistrationService.getWriter(configuration.formatId(), configuration.output(),
                configuration.getArguments());
        final ConfiguredCacheWriter metadataWriter = new ConfiguredCacheWriter(contentCache, DEFAULT);
        final TransformationMetadataCacheKey metadataKey = cacheKeyGenerator.generateMetadataKey(configuration.getPreparationId(), configuration.stepId(), configuration.getSourceType());
        final Pipeline pipeline = Pipeline.Builder.builder()
                .withAnalyzerService(analyzerService) //
                .withActionRegistry(actionRegistry) //
                .withActions(actionParser.parse(configuration.getActions())) //
                .withInitialMetadata(rowMetadata, configuration.volume() == SMALL) //
                .withMonitor(configuration.getMonitor()) //
                .withFilter(configuration.getFilter()) //
                .withFilterOut(configuration.getOutFilter()) //
                .withOutput(() -> new WriterNode(writer, metadataWriter, metadataKey, fallBackRowMetadata)) //
                .withStatisticsAdapter(adapter) //
                .withGlobalStatistics(configuration.isGlobalStatistics()) //
                .allowMetadataChange(configuration.isAllowMetadataChange()) //
                .build();
        try {
            LOGGER.debug("Before transformation: {}", pipeline);
            pipeline.execute(input);
        } finally {
            LOGGER.debug("After transformation: {}", pipeline);
        }
    }

    @Override
    public boolean accept(Configuration configuration) {
        return Configuration.class.equals(configuration.getClass());
    }
}
