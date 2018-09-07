package org.talend.dataprep.transformation.pipeline.builder;

import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CHANGE_ROW;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_COPY_COLUMNS;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_DELETE_COLUMNS;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.ReactiveTypeDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.StatisticsNode;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class StatisticsNodesBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsNodesBuilder.class);

    private static final Set<ActionDefinition.Behavior> BEHAVIORS = Stream
            .of(NEED_STATISTICS_PATTERN, NEED_STATISTICS_INVALID, NEED_STATISTICS_QUALITY, NEED_STATISTICS_FREQUENCY)
            .collect(Collectors.toSet());

    private static final Predicate<String> ALL_COLUMNS = c -> true;

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

    public Node buildPreStatistics() {
        performActionsProfiling();
        return getTypeDetectionNode(ALL_COLUMNS);
    }

    public Node buildPostStatistics() {
        performActionsProfiling();
        // Handle case where only no type impact modification was done.
        boolean containsOnlyMetadataAction = true;
        for (RunnableAction action : actions) {
            final ActionDefinition actionDefinition = actionsProfile.getMetadataByAction().get(action);
            if (actionDefinition != null) {
                containsOnlyMetadataAction &= actionDefinition
                        .getBehavior(action) //
                        .stream() //
                        .allMatch(b -> b == METADATA_CHANGE_ROW // .
                                || b == METADATA_COPY_COLUMNS //
                                || b == METADATA_DELETE_COLUMNS);
            }
        }
        if (containsOnlyMetadataAction) {
            LOGGER.debug(
                    "Only metadata actions detected in pipeline, no need to compute end of transformation statistics.");
            return new BasicNode();
        }

        // Continue (seems there are actions worth post statistics
        if (actionsProfile.needFullAnalysis()) {
            return NodeBuilder
                    .from(getTypeDetectionNode(actionsProfile.getFilterForFullAnalysis()))
                    .to(getInvalidDetectionNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .to(getFullStatisticsNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .build();
        }

        if (actionsProfile.needOnlyInvalidAnalysis()) {
            return NodeBuilder
                    .from(getInvalidDetectionNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .to(getQualityStatisticsNode(actionsProfile.getFilterForInvalidAnalysis()))
                    .build();
        }
        return new BasicNode();
    }

    /**
     * Insert statistics computing nodes before the supplied action node if needed.
     * Will try each case one by one.
     *
     * @param nextAction action needing
     * @return
     */
    public Node buildIntermediateStatistics(final Action nextAction) {
        performActionsProfiling();
        if (needIntermediateStatistics(nextAction)) {
            // Profile *only* the next action
            final ActionsStaticProfiler profiler = new ActionsStaticProfiler(actionRegistry);
            final ActionsProfile intermediateActionProfile =
                    profiler.profile(columns, Collections.singletonList(nextAction));
            final Set<ActionDefinition.Behavior> behavior = actionToMetadata.get(nextAction).getBehavior(nextAction);
            final NodeBuilder nodeBuilder =
                    NodeBuilder.from(getTypeDetectionNode(intermediateActionProfile.getFilterForFullAnalysis()));

            if (behavior.contains(NEED_STATISTICS_PATTERN)) {
                // the type detection is needed by some actions : see bug TDP-4926
                // this modification needs performance analysis
                nodeBuilder.to(getPatternDetectionNode(intermediateActionProfile.getFilterForPatternAnalysis()));
            }
            if (behavior.contains(NEED_STATISTICS_QUALITY)) {
                // the quality of the dataset is needed by some actions : see DeleteAllEmptyColumns
                nodeBuilder.to(getQualityStatisticsNode(intermediateActionProfile.getFilterForPatternAnalysis()));
            }
            if (behavior.contains(NEED_STATISTICS_FREQUENCY)) {
                // the frequency of each pattern is needed by some actions : see DeleteAllEmptyColumns
                nodeBuilder.to(getFrequencyStatisticsNode(intermediateActionProfile.getFilterForPatternAnalysis()));
            }
            if (nextAction.getParameters().containsKey(FILTER.getKey()) || behavior.contains(NEED_STATISTICS_INVALID)) {
                // 2 cases remain as this point: action needs invalid values or filter attached to action does
                // equivalent to the default case
                nodeBuilder.to(getInvalidDetectionNode(intermediateActionProfile.getFilterForInvalidAnalysis()));
            }
            return nodeBuilder.build();
        } else {
            return new BasicNode();
        }
    }

    public boolean needIntermediateStatistics(final Action nextAction) {
        performActionsProfiling();
        // next action indicates that it need fresh statistics
        final ActionDefinition actionDefinition = actionToMetadata.get(nextAction);
        if (actionDefinition != null) {
            final Set<ActionDefinition.Behavior> behavior = actionDefinition.getBehavior(nextAction);
            if (!Collections.disjoint(behavior, BEHAVIORS)) {
                return true;
            }

            // action has filter that is on valid/invalid
            if (nextAction.getParameters().containsKey(FILTER.getKey())) {
                // action has a filterForFullAnalysis, to cover cases where filters are on invalid values
                final String filterAsString = nextAction.getParameters().get(FILTER.getKey());
                return StringUtils.contains(filterAsString, "valid") || StringUtils.contains(filterAsString, "invalid");
            }
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

    private Node getTypeDetectionNode(final Predicate<String> columnFilter) {
        return allowSchemaAnalysis ? new ReactiveTypeDetectionNode(new RowMetadata(columns), columnFilter,
                statisticsAdapter, analyzerService::schemaAnalysis) : new BasicNode();
    }

    private Node getPatternDetectionNode(final Predicate<String> columnFilter) {
        return allowSchemaAnalysis ? new ReactiveTypeDetectionNode(new RowMetadata(columns), columnFilter,
                statisticsAdapter, c -> analyzerService.build(c, AnalyzerService.Analysis.PATTERNS)) : new BasicNode();
    }

    private Node getInvalidDetectionNode(final Predicate<String> columnFilter) {
        return new InvalidDetectionNode(new RowMetadata(columns), columnFilter);
    }

    private Node getQualityStatisticsNode(final Predicate<String> columnFilter) {
        return new StatisticsNode(new RowMetadata(columns), getQualityAnalyzer(), columnFilter, statisticsAdapter);
    }

    private Node getFullStatisticsNode(final Predicate<String> columnFilter) {
        return new StatisticsNode(new RowMetadata(columns), getFullAnalyzer(), columnFilter, statisticsAdapter);
    }

    private Node getFrequencyStatisticsNode(final Predicate<String> columnFilter) {
        return new StatisticsNode(new RowMetadata(columns), getFrequencyAnalyzer(), columnFilter, statisticsAdapter);
    }

    public Node buildTypeDetection() {
        performActionsProfiling();
        return getTypeDetectionNode(actionsProfile.getFilterForFullAnalysis());
    }
}
