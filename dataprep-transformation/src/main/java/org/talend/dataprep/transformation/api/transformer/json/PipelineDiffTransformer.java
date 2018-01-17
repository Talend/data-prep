// ============================================================================
//
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

package org.talend.dataprep.transformation.api.transformer.json;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.transformer.ExecutableTransformer;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.api.transformer.configuration.PreviewConfiguration;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.model.DiffWriterNode;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

/**
 * Transformer that preview the transformation (puts additional json content so that the front can display the
 * difference between current and previous transformation).
 */
@Component
class PipelineDiffTransformer implements Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineDiffTransformer.class);

    @Autowired
    private ActionParser actionParser;

    @Autowired
    private ActionRegistry actionRegistry;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private WriterRegistrationService writerRegistrationService;

    @Autowired
    private StatisticsAdapter adapter;

    /**
     * Starts the transformation in preview mode.
     *
     * @param input the dataset content.
     * @param configuration The {@link Configuration configuration} for this transformation.
     */
    @Override
    public ExecutableTransformer buildExecutable(DataSet input, Configuration configuration) {
        Validate.notNull(input, "Input cannot be null.");
        final PreviewConfiguration previewConfiguration = (PreviewConfiguration) configuration;
        final RowMetadata rowMetadata = input.getMetadata().getRowMetadata();
        final TransformerWriter writer = writerRegistrationService.getWriter(
                configuration.formatId(),
                configuration.output(),
                configuration.getArguments()
        );

        // Build diff pipeline
        final Node diffWriterNode = new DiffWriterNode(writer);
        final String referenceActions = previewConfiguration.getReferenceActions();
        final String previewActions = previewConfiguration.getPreviewActions();
        final Pipeline referencePipeline = buildPipeline(rowMetadata, referenceActions);
        final Pipeline previewPipeline = buildPipeline(rowMetadata, previewActions);

        // Filter source records (extract TDP ids information)
        final List<Long> indexes = previewConfiguration.getIndexes();
        final boolean isIndexLimited = indexes != null && !indexes.isEmpty();
        final Long minIndex = isIndexLimited ? indexes.stream().mapToLong(Long::longValue).min().getAsLong() : 0L;
        final Long maxIndex = isIndexLimited ? indexes.stream().mapToLong(Long::longValue).max().getAsLong() : Long.MAX_VALUE;
        final Predicate<DataSetRow> filter = isWithinWantedIndexes(minIndex, maxIndex);

        // Build diff pipeline
        final Node diffPipeline = NodeBuilder.filteredSource(filter) //
                .dispatchTo(referencePipeline, previewPipeline) //
                .zipTo(diffWriterNode) //
                .build();

        // wrap this transformer into an ExecutableTransformer
        return new ExecutableTransformer() {

            @Override
            public void execute() {
                // Run diff
                try {
                    // Print pipeline before execution (for debug purposes).
                    diffPipeline.logStatus(LOGGER, "Before execution: {}");
                    input.getRecords().forEach(r -> diffPipeline.exec().receive(r, rowMetadata));
                    diffPipeline.exec().signal(Signal.END_OF_STREAM);
                } finally {
                    // Print pipeline after execution (for debug purposes).
                    diffPipeline.logStatus(LOGGER, "After execution: {}");
                }
            }

            @Override
            public void signal(Signal signal) {
                diffPipeline.exec().signal(signal);
            }
        };
    }

    private Pipeline buildPipeline(RowMetadata rowMetadata, String actions) {
        return Pipeline.Builder.builder() //
                .withAnalyzerService(analyzerService) //
                .withActionRegistry(actionRegistry) //
                .withActions(actionParser.parse(actions)) //
                .withInitialMetadata(rowMetadata, true) //
                .withOutput(BasicNode::new) //
                .withGlobalStatistics(false) //
                .allowMetadataChange(false) //
                .withStatisticsAdapter(adapter) //
                .build();
    }

    @Override
    public boolean accept(Configuration configuration) {
        return PreviewConfiguration.class.isAssignableFrom(configuration.getClass());
    }

    private Predicate<DataSetRow> isWithinWantedIndexes(Long minIndex, Long maxIndex) {
        return row -> row.getTdpId() >= minIndex && row.getTdpId() <= maxIndex;
    }
}
