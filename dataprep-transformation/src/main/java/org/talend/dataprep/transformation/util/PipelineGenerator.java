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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ApplyDataSetRowAction;
import org.talend.dataprep.transformation.actions.common.CompileDataSetRowAction;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.*;
import org.talend.dataprep.transformation.pipeline.builder.ActionNodesBuilder;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.model.WriterNode;
import org.talend.dataprep.transformation.pipeline.node.*;
import org.talend.dataprep.transformation.service.StepMetadataRepository;

@Component
public class PipelineGenerator {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineGenerator.class);

    @Value("${dataset.records.limit:10000}")
    protected long limit;

    @Autowired
    private StatisticsAdapter statisticsAdapter;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private ActionRegistry actionRegistry;

    @Autowired
    private StepMetadataRepository preparationUpdater;

    /**
     * Build a pipeline to calculate metadata (type, stats and invalid detection)
     *
     * @param writer the writer to user
     * @return the pipeline to execute
     */
    public Pipeline buildPipelineToCalculateMetadata(WriterNode writer) {

        Pipeline pipeline = new Pipeline();

        NodeBuilder nodeBuilder = NodeBuilder.source();
        nodeBuilder //
                .to(new TypeDetectionNode(c -> true, statisticsAdapter, analyzerService::schemaAnalysis))
                .to(new StatisticsNode(analyzerService, c -> true, statisticsAdapter)) //
                .to(new InvalidDetectionNode(c -> true)) //
                .to(new LimitNode(limit, () -> pipeline.signal(Signal.STOP))) // limit the output to the sample
                .to(writer); // writer

        // FIXME: rajouter l'analyse statique des actions pour analyser uniquement les colonnes (see StaticsNodesBuilder)

        pipeline.setNode(nodeBuilder.build());
        return pipeline;
    }

    public Pipeline buildPipelineToApplyActionsFromPreparation(PreparationMessage preparation, Predicate<DataSetRow> inFilter,
            RowMetadata rowMetadata, Boolean statisticsBefore, Boolean statisticsAfter, Boolean allowSchemaAnalysis, WriterNode writer) {
        LOGGER.debug("Running using preparation #{} ({} step(s))", preparation.getId(), preparation.getSteps().size());

        return buildPipelineToApplyActions(preparation.getActions(), preparation.getSteps(), inFilter, rowMetadata,
                statisticsBefore, statisticsAfter, allowSchemaAnalysis, writer);
    }

    public Pipeline buildPipelineToApplyActions(List<Action> actions, List<Step> steps, Predicate<DataSetRow> inFilter,
            RowMetadata rowMetadata, Boolean statisticsBefore, Boolean statisticsAfter, Boolean allowSchemaAnalysis, WriterNode writer) {
        final NodeBuilder nodeBuilder;
        final Pipeline pipeline = new Pipeline();

        if (inFilter != null) {
            nodeBuilder = NodeBuilder.filteredSource(inFilter);
        } else {
            nodeBuilder = NodeBuilder.source();
        }

        // Build nodes for actions
        LOGGER.debug("Running using actions ({} action(s))", actions.size());
        final List<RunnableAction> runnableActions = transformActionToRunnableAction(actions);
        final Node actionsNode = transformRunnableActionToNode(runnableActions, rowMetadata, statisticsBefore, statisticsAfter,
                allowSchemaAnalysis);

        if(steps != null){
            LOGGER.debug("Applying step node transformations...");
            actionsNode.logStatus(LOGGER, "Before transformation\n{}");

            final Function<Step, RowMetadata> rowMetadataSupplier = s -> Optional.ofNullable(s.getRowMetadata()) //
                    .map(id -> preparationUpdater.get(id)) //
                    .orElse(null);

            final Node node = StepNodeTransformer.transform(actionsNode, steps, rowMetadataSupplier);
            nodeBuilder.to(node);
            node.logStatus(LOGGER, "After transformation\n{}");
        } else {
            nodeBuilder.to(actionsNode);
        }

        nodeBuilder
            .to(new LimitNode(limit, () -> pipeline.signal(Signal.STOP))) // limit the output to the sample
            .to(writer); // writer

        pipeline.setNode(nodeBuilder.build());
        return pipeline;

    }

    private Node transformRunnableActionToNode(List<RunnableAction> runnableActions, RowMetadata rowMetadata,
            Boolean statisticsBefore, Boolean statisticsAfter, Boolean allowSchemaAnalysis) {
        return ActionNodesBuilder
                .builder() //
                .initialMetadata(rowMetadata) //
                .actions(runnableActions) //
                // statistics requests
                .needStatisticsBefore(statisticsBefore) //
                .needStatisticsAfter(statisticsAfter) //
                .allowSchemaAnalysis(allowSchemaAnalysis) //
                // statistics dependencies/arguments
                .actionRegistry(actionRegistry) //
                .analyzerService(analyzerService) //
                .statisticsAdapter(statisticsAdapter) //
                .build();
    }

    private List<RunnableAction> transformActionToRunnableAction(List<Action> actions) {
        return actions
                .stream() //
                .map(a -> {
                    // gather all info for creating runnable actions
                    final Map<String, String> parameters = a.getParameters();
                    final ScopeCategory scope = ScopeCategory.from(parameters.get(ImplicitParameters.SCOPE.getKey()));
                    final ActionDefinition actionDefinition = actionRegistry.get(a.getName());
                    final CompileDataSetRowAction compile = new CompileDataSetRowAction(parameters, actionDefinition, scope);
                    final ApplyDataSetRowAction apply = new ApplyDataSetRowAction(actionDefinition, parameters, scope);

                    // Create runnable action
                    return RunnableAction.Builder
                            .builder() //
                            .withCompile(compile) //
                            .withRow(apply) //
                            .withName(a.getName()) //
                            .withParameters(new HashMap<>(parameters)) //
                            .build();
                }) //
                .collect(Collectors.toList());
    }

}
