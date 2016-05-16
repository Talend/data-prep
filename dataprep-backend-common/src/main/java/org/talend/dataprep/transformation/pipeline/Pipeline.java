package org.talend.dataprep.transformation.pipeline;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.node.*;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class Pipeline implements Node, RuntimeNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final Node node;

    /**
     * @param node The source node (the node in the pipeline that submit content to the pipeline).
     * @see Builder to create a new instance of this class.
     */
    Pipeline(Node node) {
        this.node = node;
    }

    public void execute(DataSet dataSet) {
        final RowMetadata rowMetadata = dataSet.getMetadata().getRowMetadata().clone();
        try (Stream<DataSetRow> records = dataSet.getRecords()) {
            records.forEach(r -> node.exec().receive(r, rowMetadata));
            node.exec().signal(Signal.END_OF_STREAM);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        accept(new PipelineConsoleDump(builder));
        return builder.toString();
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        node.exec().receive(row, metadata);
    }

    @Override
    public Link getLink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLink(Link link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void signal(Signal signal) {
        node.exec().signal(signal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPipeline(this);
    }

    @Override
    public RuntimeNode exec() {
        return this;
    }

    public Node getNode() {
        return node;
    }

    public static class Builder {

        private final List<Action> actions = new ArrayList<>();

        private final Map<Action, ActionMetadata> actionToMetadata = new HashMap<>();

        private Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> inlineAnalyzer = l -> Analyzers
                .with(NullAnalyzer.INSTANCE);

        private Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> delayedAnalyzer = l -> Analyzers
                .with(NullAnalyzer.INSTANCE);

        private RowMetadata rowMetadata;

        private ActionRegistry actionRegistry;

        private TransformationContext context;

        private StatisticsAdapter adapter;

        private Supplier<Node> outputSupplier = () -> NullNode.INSTANCE;

        public static Builder builder() {
            return new Builder();
        }

        public Builder withStatisticsAdapter(StatisticsAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder withContext(TransformationContext context) {
            this.context = context;
            return this;
        }

        public Builder withActionRegistry(ActionRegistry actionRegistry) {
            this.actionRegistry = actionRegistry;
            return this;
        }

        public Builder withInitialMetadata(RowMetadata rowMetadata) {
            this.rowMetadata = rowMetadata;
            return this;
        }

        public Builder withActions(List<Action> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public Builder withInlineAnalysis(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
            this.inlineAnalyzer = analyzer;
            return this;
        }

        public Builder withDelayedAnalysis(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
            this.delayedAnalyzer = analyzer;
            return this;
        }

        public Builder withOutput(Supplier<Node> outputSupplier) {
            this.outputSupplier = outputSupplier;
            return this;
        }

        private ActionAnalysis analyzeActions() {
            // Compile actions
            final Set<String> readOnlyColumns = rowMetadata.getColumns().stream().map(ColumnMetadata::getId)
                    .collect(Collectors.toSet());
            final Set<String> modifiedColumns = new HashSet<>();
            int createColumnActions = 0;
            ActionAnalysis analysisResult = new ActionAnalysis();
            if (actionRegistry != null) {
                for (Action action : actions) {
                    final ActionMetadata actionMetadata = actionRegistry.get(action.getName());
                    actionToMetadata.put(action, actionMetadata);
                }
                // Analyze what columns to look at during analysis
                for (Map.Entry<Action, ActionMetadata> entry : actionToMetadata.entrySet()) {
                    final ActionMetadata actionMetadata = entry.getValue();
                    final Action action = entry.getKey();
                    Set<ActionMetadata.Behavior> behavior = actionMetadata.getBehavior();
                    for (ActionMetadata.Behavior currentBehavior : behavior) {
                        switch (currentBehavior) {
                        case VALUES_ALL:
                            // All values are going to be changed, and all original columns are going to be modified.
                            modifiedColumns.addAll(readOnlyColumns);
                            readOnlyColumns.clear();
                            break;
                        case METADATA_CHANGE_TYPE:
                        case VALUES_COLUMN:
                            final String modifiedColumnId = action.getParameters().get(ImplicitParameters.COLUMN_ID.getKey());
                            modifiedColumns.add(modifiedColumnId);
                            break;
                        case METADATA_COPY_COLUMNS:
                            // TODO Ignore column copy from analysis (metadata did not change)
                            break;
                        case METADATA_CREATE_COLUMNS:
                            createColumnActions++;
                            break;
                        case METADATA_DELETE_COLUMNS:
                        case METADATA_CHANGE_NAME:
                            // Do nothing: no need to re-analyze where only name was changed.
                            break;
                        default:
                            break;
                        }
                    }
                }
                analysisResult.filter = c -> modifiedColumns.contains(c.getId()) || !readOnlyColumns.contains(c.getId());
                analysisResult.needDelayedAnalysis = !modifiedColumns.isEmpty() || createColumnActions > 0;
            } else {
                LOGGER.warn("Unable to statically analyze actions (no action registry defined).");
                analysisResult.filter = c -> true;
                analysisResult.needDelayedAnalysis = true;
            }
            return analysisResult;
        }

        public Pipeline build() {
            final ActionAnalysis analysis = analyzeActions();
            final NodeBuilder current = NodeBuilder.source();
            // Apply actions
            for (Action action : actions) {
                current.to(new CompileNode(action, context.create(action.getRowAction())));
                if (action.getParameters().containsKey(ImplicitParameters.FILTER.getKey())) {
                    // action has a filter, to cover cases where filters are on invalid values
                    final String filterAsString = action.getParameters().get(ImplicitParameters.FILTER.getKey());
                    // TODO Perform static analysis of filter to discover if filter holds conditions that needs up-to-date
                    // statistics
                    if (StringUtils.contains(filterAsString, "valid") || StringUtils.contains(filterAsString, "invalid")) {
                        // TODO Perform static analysis of filter to discover which column is the filter on.
                        current.to(new InlineAnalysisNode(inlineAnalyzer, c -> true, adapter));
                    }
                }
                current.to(new ActionNode(action, context.in(action.getRowAction())));
            }
            // Analyze (delayed)
            if (analysis.needDelayedAnalysis) {
                current.to(new InlineAnalysisNode(inlineAnalyzer, analysis.filter, adapter));
                current.to(new DelayedAnalysisNode(delayedAnalyzer, analysis.filter, adapter));
            }
            // Output
            current.to(new CleanUpNode(context));
            current.to(outputSupplier.get());
            // Finally build pipeline
            return new Pipeline(current.build());
        }

        private class ActionAnalysis {

            private boolean needDelayedAnalysis;

            private Predicate<ColumnMetadata> filter = c -> true;
        }

    }

}
