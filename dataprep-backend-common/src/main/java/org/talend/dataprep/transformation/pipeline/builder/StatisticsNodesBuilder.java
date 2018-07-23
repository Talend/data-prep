package org.talend.dataprep.transformation.pipeline.builder;

import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_FREQUENCY;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_INVALID;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_PATTERN;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_QUALITY;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.RowMetadataFallbackProvider;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.StatisticsNode;
import org.talend.dataprep.transformation.pipeline.node.TypeDetectionNode;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class StatisticsNodesBuilder {

    private static final Set<ActionDefinition.Behavior> BEHAVIORS = Stream.of(NEED_STATISTICS_PATTERN, NEED_STATISTICS_INVALID,
            NEED_STATISTICS_QUALITY, NEED_STATISTICS_FREQUENCY).collect(Collectors.toSet());

    private static final Predicate<ColumnMetadata> ALL_COLUMNS = c -> true;

    private AnalyzerService analyzerService;

    private ActionRegistry actionRegistry;

    private StatisticsAdapter statisticsAdapter;

    private List<RunnableAction> actions;

    private List<ColumnMetadata> columns;

    private boolean allowSchemaAnalysis = true;

    private ActionsProfile actionsProfile;

    private Map<Action, ActionDefinition> actionToMetadata;

    private StatisticsNodesBuilder() {
    }

    public static StatisticsNodesBuilder builder() {
        return new StatisticsNodesBuilder();
    }

    public StatisticsNodesBuilder analyzerService(final AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
        return this;
    }

    public StatisticsNodesBuilder actionRegistry(final ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
        return this;
    }

    public StatisticsNodesBuilder statisticsAdapter(final StatisticsAdapter statisticsAdapter) {
        this.statisticsAdapter = statisticsAdapter;
        return this;
    }

    public StatisticsNodesBuilder allowSchemaAnalysis(final boolean allowSchemaAnalysis) {
        this.allowSchemaAnalysis = allowSchemaAnalysis;
        return this;
    }

    public StatisticsNodesBuilder actions(final List<RunnableAction> actions) {
        this.actions = actions;
        return this;
    }

    public StatisticsNodesBuilder columns(final List<ColumnMetadata> columns) {
        this.columns = columns;
        return this;
    }

    public Map<Action, ActionDefinition> getActionToMetadata() {
        return actionToMetadata;
    }

    public Node buildPreStatistics(RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        // TODO remove this and fix tests
        if (analyzerService == null) {
            return new BasicNode();
        }

        performActionsProfiling();
        return getTypeDetectionNode(ALL_COLUMNS, rowMetadataFallbackProvider);
    }

    public Node buildPostStatistics(RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        // TODO remove this and fix tests
        if (analyzerService == null) {
            return new BasicNode();
        }

        performActionsProfiling();
        if (actionsProfile.needFullAnalysis()) {
            return NodeBuilder
                    .from(getTypeDetectionNode(actionsProfile.getFilterForFullAnalysis(), rowMetadataFallbackProvider))
                    .to(getInvalidDetectionNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .to(getFullStatisticsNode(actionsProfile.getFilterForInvalidAnalysis(),
                            rowMetadataFallbackProvider))
                    .build();
        }

        if (actionsProfile.needOnlyInvalidAnalysis()) {
            return NodeBuilder
                    .from(getInvalidDetectionNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .to(getQualityStatisticsNode(actionsProfile.getFilterForInvalidAnalysis(),
                            rowMetadataFallbackProvider))
                    .build();
        }
        return new BasicNode();
    }

    /**
     * Insert statistics computing nodes before the supplied action node if needed.
     * Will try each case one by one.
     * @param nextAction action needing
     * @return
     */
    public Node buildIntermediateStatistics(final Action nextAction,
            RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        Node node = null;
        // TODO remove this and fix tests
        if (analyzerService == null) {
            node = new BasicNode();
        } else {
            performActionsProfiling();

            if (needIntermediateStatistics(nextAction)) {

                final Set<ActionDefinition.Behavior> behavior =
                        actionToMetadata.get(nextAction).getBehavior(nextAction);
                NodeBuilder nodeBuilder = NodeBuilder.from(
                        getTypeDetectionNode(actionsProfile.getFilterForFullAnalysis(), rowMetadataFallbackProvider));

                if (behavior.contains(NEED_STATISTICS_PATTERN)) {
                    // the type detection is needed by some actions : see bug TDP-4926
                    // this modification needs performance analysis
                    nodeBuilder.to(getPatternDetectionNode(actionsProfile.getFilterForPatternAnalysis(),
                            rowMetadataFallbackProvider));
                }
                if (behavior.contains(NEED_STATISTICS_QUALITY)) {
                    // the quality of the dataset is needed by some actions : see DeleteAllEmptyColumns
                    nodeBuilder.to(getQualityStatisticsNode(actionsProfile.getFilterForPatternAnalysis(),
                            rowMetadataFallbackProvider));
                }
                if (behavior.contains(NEED_STATISTICS_FREQUENCY)) {
                    // the frequency of each pattern is needed by some actions : see DeleteAllEmptyColumns
                    nodeBuilder.to(getFrequencyStatisticsNode(actionsProfile.getFilterForPatternAnalysis()));
                }
                if (nextAction.getParameters().containsKey(FILTER.getKey())
                        || behavior.contains(NEED_STATISTICS_INVALID)) {
                    // 2 cases remain as this point: action needs invalid values or filter attached to action does
                    // equivalent to the default case
                    nodeBuilder.to(getInvalidDetectionNode(actionsProfile.getFilterForInvalidAnalysis()));
                }
                node = nodeBuilder.build();
            }
        }
        return node;
    }

    private boolean needIntermediateStatistics(final Action nextAction) {
        // next action indicates that it need fresh statistics
        final Set<ActionDefinition.Behavior> behavior = actionToMetadata.get(nextAction).getBehavior(nextAction);
        if (!Collections.disjoint(behavior, BEHAVIORS)) {
            return true;
        }

        // action has filter that is on valid/invalid
        if (nextAction.getParameters().containsKey(FILTER.getKey())) {
            // action has a filterForFullAnalysis, to cover cases where filters are on invalid values
            final String filterAsString = nextAction.getParameters().get(FILTER.getKey());
            return StringUtils.contains(filterAsString, "valid") || StringUtils.contains(filterAsString, "invalid");
        }

        return false;
    }

    private void performActionsProfiling() {
        if (actionsProfile != null) {
            return;
        }
        checkInputs();

        final ActionsStaticProfiler profiler = new ActionsStaticProfiler(actionRegistry);
        actionsProfile = profiler.profile(columns, actions);
        actionToMetadata = actionsProfile.getMetadataByAction();
    }

    private void checkInputs() {
        if (actionRegistry == null) {
            throw new MissingResourceException("You need to provide an actionRegistry", "ActionRegistry", null);
        }
        if (statisticsAdapter == null) {
            throw new MissingResourceException("You need to provide an statistics adapter", "StatisticsAdapter", null);
        }
        if (actions == null) {
            throw new MissingResourceException("You need to provide the whole list of actions", "List", null);
        }
        if (columns == null) {
            throw new MissingResourceException("You need to provide the whole list of columns", "List", null);
        }
    }

    /**
     * Create a full analyzer
     */
    private Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> getQualityAnalyzer() {
        return c -> analyzerService.build(c, AnalyzerService.Analysis.QUALITY);
    }

    /**
     * Create a full analyzer
     */
    private Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> getFullAnalyzer() {
        return StatisticsNode.getDefaultAnalyzer(analyzerService);
    }

    private Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> getFrequencyAnalyzer() {
        return c -> analyzerService.build(c, AnalyzerService.Analysis.FREQUENCY);
    }

    private Node getTypeDetectionNode(final Predicate<ColumnMetadata> columnFilter,
            RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        return allowSchemaAnalysis
                ? new TypeDetectionNode(columnFilter, statisticsAdapter, analyzerService::schemaAnalysis,
                        rowMetadataFallbackProvider)
                : new BasicNode();
    }

    private Node getPatternDetectionNode(final Predicate<ColumnMetadata> columnFilter,
            RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        return allowSchemaAnalysis
                ? new TypeDetectionNode(columnFilter, statisticsAdapter,
                        c -> analyzerService.build(c, AnalyzerService.Analysis.PATTERNS), rowMetadataFallbackProvider)
                : new BasicNode();
    }

    private Node getInvalidDetectionNode(final Predicate<ColumnMetadata> columnFilter) {
        return new InvalidDetectionNode(columnFilter);
    }

    private Node getFullStatisticsNode(final Predicate<ColumnMetadata> columnFilter,
            RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        return new StatisticsNode(getFullAnalyzer(), columnFilter, statisticsAdapter, rowMetadataFallbackProvider);
    }

    private Node getQualityStatisticsNode(final Predicate<ColumnMetadata> columnFilter,
            RowMetadataFallbackProvider rowMetadataFallbackProvider) {
        return new StatisticsNode(getQualityAnalyzer(), columnFilter, statisticsAdapter, rowMetadataFallbackProvider);
    }

    private Node getFrequencyStatisticsNode(final Predicate<ColumnMetadata> columnFilter) {
        return new StatisticsNode(getFrequencyAnalyzer(), columnFilter, statisticsAdapter);
    }
}
