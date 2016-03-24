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
import org.talend.dataprep.transformation.api.action.metadata.column.Swap;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.model.*;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;

public class Pipeline implements Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

    private final Node node;

    /**
     * @param node The source node (the node in the pipeline that submit content to the pipeline).
     * @see Builder to create a new instance of this class.
     */
    private Pipeline(Node node) {
        this.node = node;
    }

    public void execute(DataSet dataSet) {
        try (Stream<DataSetRow> records = dataSet.getRecords()) {
            final RowMetadata rowMetadata = dataSet.getMetadata().getRowMetadata();
            records.forEach(r -> node.receive(r, rowMetadata));
            node.signal(Signal.END_OF_STREAM);
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
        node.receive(row, metadata);
    }

    @Override
    public void setLink(Link link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link getLink() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void signal(Signal signal) {
        node.signal(signal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPipeline(this);
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

        private Predicate<ColumnMetadata> filter = c -> true;

        private TransformationContext context;

        private StatisticsAdapter adapter;

        private Supplier<Node> outputSupplier = () -> NullNode.INSTANCE;

        public static Builder builder() {
            return new Builder();
        }

        private Node buildApplyActions(Node node, Function<Action, Node> nodeFunction) {
            final Iterator<Action> compileIterator = actions.iterator();
            Node lastNode = node;
            while (compileIterator.hasNext()) {
                final Action action = compileIterator.next();
                // Check if action needs for up-to-date statistics
                // TODO To enable needs further testing (see TDP-1543).
                /*
                if (hasBehavior(action, ActionMetadata.Behavior.NEED_STATISTICS)) {
                    Node newNode = new InlineAnalysisNode(inlineAnalyzer, filter, adapter);
                    Link link = new BasicLink(newNode);
                    lastNode.setLink(link);
                    lastNode = newNode;
                }
                */
                // Adds new action
                Node newNode = nodeFunction.apply(action);
                Link link = new BasicLink(newNode);
                lastNode.setLink(link);
                lastNode = newNode;
            }
            return lastNode;
        }

        private boolean hasBehavior(Action action, ActionMetadata.Behavior behavior) {
            return actionToMetadata.get(action) != null && actionToMetadata.get(action).getBehavior().contains(behavior);
        }

        private Node buildCompileActions(Node node, Function<Action, Node> nodeFunction) {
            final Iterator<Action> compileIterator = actions.iterator();
            Node lastNode = node;
            while (compileIterator.hasNext()) {
                final Action action = compileIterator.next();
                Node newNode = nodeFunction.apply(action);
                Link link = new BasicLink(newNode);
                lastNode.setLink(link);
                lastNode = newNode;
            }
            return lastNode;
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

        public Pipeline build() {
            // Source
            final SourceNode sourceNode = new SourceNode();
            // Compile actions
            final Set<String> readOnlyColumns = rowMetadata.getColumns().stream().map(ColumnMetadata::getId)
                    .collect(Collectors.toSet());
            final Set<String> modifiedColumns = new HashSet<>();
            int createOrModifyColumnActions = 0;
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
                            // TODO ideally we'd like to be able to pass a collection of ids
                            // TODO something more generic than relying on the action name
                            if (StringUtils.equals(action.getName(), Swap.SWAP_COLUMN_ACTION_NAME) //
                                    && StringUtils.isNotBlank(action.getParameters().get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER))){
                                modifiedColumns.add(action.getParameters().get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
                            }

                            break;
                        case METADATA_COPY_COLUMNS:
                            // TODO Ignore column copy from analysis (metadata did not change)
                            break;
                        case METADATA_CREATE_COLUMNS:
                            createOrModifyColumnActions++;
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
            } else {
                LOGGER.warn("Unable to statically analyze actions (no action registry defined).");
            }
            filter = c -> modifiedColumns.contains(c.getId()) || !readOnlyColumns.contains(c.getId());
            // Compile actions
            Node current = buildCompileActions(sourceNode, a -> new CompileNode(a, context.create(a.getRowAction())));
            current = buildApplyActions(current, a -> new ActionNode(a, context.in(a.getRowAction())));
            // Analyze (delayed)
            if (!modifiedColumns.isEmpty() || createOrModifyColumnActions > 0) {
                // Inline analysis
                Node inlineAnalysisNode = new InlineAnalysisNode(inlineAnalyzer, filter, adapter);
                current.setLink(new BasicLink(inlineAnalysisNode));
                current = inlineAnalysisNode;
                // Delayed analysis
                Node delayedAnalysisNode = new DelayedAnalysisNode(delayedAnalyzer, filter, adapter);
                current.setLink(new BasicLink(delayedAnalysisNode));
                current = delayedAnalysisNode;
            }
            // Output
            final Node outputNode = outputSupplier.get();
            current.setLink(new BasicLink(outputNode));
            // Finally build pipeline
            return new Pipeline(sourceNode);
        }

    }

}
