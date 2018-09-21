package org.talend.dataprep.transformation.pipeline.builder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CleanUpNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;

public class ActionNodesBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionNodesBuilder.class);

    private final List<RunnableAction> actions = new ArrayList<>();

    private RowMetadata initialMetadata;

    // analyze requests
    private boolean needStatisticsBefore = false;

    private boolean needStatisticsAfter = false;

    private boolean allowSchemaAnalysis = true;

    // analyse dependencies
    private ActionRegistry actionRegistry;

    private StatisticsAdapter statisticsAdapter;

    private AnalyzerService analyzerService;

    public static ActionNodesBuilder builder() {
        return new ActionNodesBuilder();
    }

    public ActionNodesBuilder initialMetadata(final RowMetadata initialMetadata) {
        this.initialMetadata = initialMetadata;
        return this;
    }

    public ActionNodesBuilder actions(final List<RunnableAction> actions) {
        this.actions.addAll(actions);
        return this;
    }

    public ActionNodesBuilder needStatisticsBefore(final boolean needStatisticsBefore) {
        this.needStatisticsBefore = needStatisticsBefore;
        return this;
    }

    public ActionNodesBuilder needStatisticsAfter(final boolean needStatisticsAfter) {
        this.needStatisticsAfter = needStatisticsAfter;
        return this;
    }

    public ActionNodesBuilder allowSchemaAnalysis(final boolean allowSchemaAnalysis) {
        this.allowSchemaAnalysis = allowSchemaAnalysis;
        return this;
    }

    public ActionNodesBuilder actionRegistry(final ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
        return this;
    }

    public ActionNodesBuilder analyzerService(final AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
        return this;
    }

    public ActionNodesBuilder statisticsAdapter(final StatisticsAdapter statisticsAdapter) {
        this.statisticsAdapter = statisticsAdapter;
        return this;
    }

    /**
     * Build the actions pipeline
     */
    public Node build() {
        final StatisticsNodesBuilder statisticsNodesBuilder =
                StatisticsNodesBuilder
                        .builder()
                        .analyzerService(analyzerService) //
                        .actionRegistry(actionRegistry) //
                        .statisticsAdapter(statisticsAdapter) //
                        .allowSchemaAnalysis(allowSchemaAnalysis) //
                        .actions(actions) //
                        .columns(initialMetadata.getColumns());

        final NodeBuilder builder = NodeBuilder.source();

        // first node doesn't need reservoir analysis
        // unless we don't have initial metadata or we explicitly ask it
        if (needStatisticsBefore || initialMetadata.getColumns().isEmpty()) {
            LOGGER.debug("No initial metadata submitted for transformation, computing new one.");
            builder.to(statisticsNodesBuilder.buildPreStatistics());
        }

        // transformation context is the parent of every action context
        // it will hold all the action context
        // that makes it the perfect entry point to clean up all the contexts
        final TransformationContext context = new TransformationContext();

        // append actions
        // actions are composed of
        // * a reservoir if fresh statistics are needed for the action
        // * a compile node
        // * an action node
        RowMetadata lastRowMetadata = initialMetadata.clone();
        boolean continuePreCompile = true;
        for (final RunnableAction nextAction : actions) {
            // some actions need fresh statistics
            // in those cases, we gather the rows in a reservoir node that triggers statistics computation
            // before dispatching each row to the next node
            final boolean needIntermediateStatistics = statisticsNodesBuilder.needIntermediateStatistics(nextAction);
            if (needIntermediateStatistics) {
                final Node neededReservoir = statisticsNodesBuilder.buildIntermediateStatistics(nextAction);
                builder.to(neededReservoir);
                continuePreCompile = false;
            }

            final DataSetRowAction rowAction = nextAction.getRowAction();

            lastRowMetadata.clearDiffStatus();
            final ActionContext actionContext = context.create(rowAction, lastRowMetadata.clone());
            actionContext.setParameters(nextAction.getParameters());
            if (continuePreCompile) {
                nextAction.getRowAction().compile(actionContext);
            }
            lastRowMetadata = actionContext.getRowMetadata();

            builder.to(new CompileNode(nextAction, actionContext));
            builder.to(new ActionNode(nextAction, actionContext));
        }

        // global analysis after actions
        // when it is explicitly asked and the actions changes the columns
        if (needStatisticsAfter) {
            statisticsNodesBuilder.columns(lastRowMetadata.getColumns());
            builder.to(statisticsNodesBuilder.buildPostStatistics());
        } else {
            builder.to(statisticsNodesBuilder.buildTypeDetection());
        }

        // cleanup all contexts after all actions
        builder.to(new CleanUpNode(context));

        return builder.build();
    }
}
