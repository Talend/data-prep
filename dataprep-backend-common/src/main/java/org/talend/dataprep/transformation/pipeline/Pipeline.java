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

package org.talend.dataprep.transformation.pipeline;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ApplyDataSetRowAction;
import org.talend.dataprep.transformation.actions.common.CompileDataSetRowAction;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.builder.ActionNodesBuilder;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.FilteredNode;
import org.talend.dataprep.transformation.pipeline.node.LimitNode;

public class Pipeline implements Node, RuntimeNode, Serializable {

    /** This class' logger. */
    private static final Logger LOG = getLogger(Pipeline.class);

    /** For the Serialization interface. */
    private static final long serialVersionUID = 1L;

    private Node node;

    /** Flag used to know if the pipeline is stopped or not. */
    private final AtomicBoolean isStopped = new AtomicBoolean();

    /**
     * Boolean used as semaphore to make the pipeline#signal(Stop) method wait for the pipeline to be finished before
     * returning.
     *
     * @see Pipeline#signal(Signal)
     */
    private final transient Object isFinished = new Object();

    /**
     * Default empty constructor.
     */
    public Pipeline() {
        // needed for Serialization
    }

    /**
     * @param node The source node (the node in the pipeline that submit content to the pipeline).
     * @see Builder to create a new instance of this class.
     */
    public Pipeline(Node node) {
        this.node = node;
    }

    public void execute(DataSet dataSet) {
        final RowMetadata rowMetadata = dataSet.getMetadata().getRowMetadata().clone();
        try (Stream<DataSetRow> records = dataSet.getRecords()) {

            // get the lock on isFinished to make the signal(STOP) method wait for the whole pipeline to finish
            synchronized (isFinished) {

                AtomicLong counter = new AtomicLong();

                // we use map/allMatch to stop the stream when isStopped = true
                // with only forEach((row) -> if(isStopped)) for ex we just stop the processed code
                // but we proceed all the rows of the stream
                // to replace when java introduce more useful functions to stream (ex: takeWhile)
                records //
                        .map(row -> { //
                            node.exec().receive(row, rowMetadata);
                            counter.addAndGet(1L);
                            return row;
                        }) //
                        .allMatch(row -> !isStopped.get());
                LOG.debug("{} rows sent in the pipeline", counter.get());
                node.exec().signal(Signal.END_OF_STREAM);
            }
        }
    }

    public void setNode(Node node) {
        this.node = node;
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
    public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
        throw new UnsupportedOperationException("Pipeline only manage single rows as input");
    }

    @Override
    public Link getLink() {
        return node.getLink();
    }

    @Override
    public void setLink(Link link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void signal(Signal signal) {

        // stop the pipeline and swallows the signal (Signal.END_OF_STREAM will be sent by the execute method)
        if (signal == Signal.STOP) {
            isStopped.set(true);
            waitForPipelineToFinish();
        } else if (signal == Signal.CANCEL) {
            isStopped.set(true);
            node.exec().signal(signal);
        } else {
            node.exec().signal(signal);
        }
    }

    /**
     * Simply wait for the pipeline to finish via the isFinished lock.
     */
    private void waitForPipelineToFinish() {
        synchronized (isFinished) {
            LOG.debug("pipeline is finished");
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitPipeline(this);
    }

    @Override
    public RuntimeNode exec() {
        return this;
    }

    @Override
    public Node copyShallow() {
        return new Pipeline(node);
    }

    public Node getNode() {
        return node;
    }

    public static class Builder {

        private final List<RunnableAction> actions = new ArrayList<>();

        private RowMetadata rowMetadata;

        private boolean completeMetadata;

        private ActionRegistry actionRegistry;

        private StatisticsAdapter adapter;

        private Supplier<Node> monitorSupplier = BasicNode::new;

        private Supplier<Node> outputSupplier = null;

        private boolean allowMetadataChange = true;

        private Predicate<DataSetRow> inFilter;

        private Function<RowMetadata, Predicate<DataSetRow>> outFilter;

        private boolean needGlobalStatistics = true;

        private AnalyzerService analyzerService;

        private PreparationMessage preparation;

        private Function<Step, RowMetadata> rowMetadataSupplier = s -> null;

        private Long limit = null;

        public static Builder builder() {
            return new Builder();
        }

        public Builder withStepMetadataSupplier(Function<Step, RowMetadata> rowMetadataSupplier) {
            this.rowMetadataSupplier = rowMetadataSupplier;
            return this;
        }

        public Builder withAnalyzerService(AnalyzerService analyzerService) {
            this.analyzerService = analyzerService;
            return this;
        }

        public Builder withStatisticsAdapter(StatisticsAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder withActionRegistry(ActionRegistry actionRegistry) {
            this.actionRegistry = actionRegistry;
            return this;
        }

        public Builder withInitialMetadata(RowMetadata rowMetadata, boolean completeMetadata) {
            this.rowMetadata = rowMetadata;
            this.completeMetadata = completeMetadata;
            return this;
        }

        public Builder withPreparation(PreparationMessage preparation) {
            this.preparation = preparation;
            return this;
        }

        public Builder withActions(List<RunnableAction> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public Builder withMonitor(Supplier<Node> monitorSupplier) {
            this.monitorSupplier = monitorSupplier;
            return this;
        }

        public Builder withOutput(Supplier<Node> outputSupplier) {
            this.outputSupplier = outputSupplier;
            return this;
        }

        public Builder withGlobalStatistics(boolean needGlobalStatistics) {
            this.needGlobalStatistics = needGlobalStatistics;
            return this;
        }

        public Builder allowMetadataChange(boolean allowMetadataChange) {
            this.allowMetadataChange = allowMetadataChange;
            return this;
        }

        public Builder withFilter(Predicate<DataSetRow> filter) {
            this.inFilter = filter;
            return this;
        }

        public Builder withFilterOut(Function<RowMetadata, Predicate<DataSetRow>> outFilter) {
            this.outFilter = outFilter;
            return this;
        }

        public Builder withLimit(Long limit) {
            this.limit = limit;
            return this;
        }

        public Pipeline build() {
            final NodeBuilder current;
            if (inFilter != null) {
                current = NodeBuilder.filteredSource(inFilter);
            } else {
                current = NodeBuilder.source();
            }

            // Apply actions
            final List<RunnableAction> runnableActions;
            if (preparation != null) {
                LOG.debug("Running using preparation #{} ({} step(s))", preparation.getId(), preparation.getSteps().size());
                runnableActions = actions.stream() //
                        .map(a -> {
                            // gather all info for creating runnable actions
                            final Map<String, String> parameters = a.getParameters();
                            final ScopeCategory scope = ScopeCategory.from(parameters.get(ImplicitParameters.SCOPE.getKey()));
                            final ActionDefinition actionDefinition = actionRegistry.get(a.getName());
                            final CompileDataSetRowAction compile = new CompileDataSetRowAction(parameters, actionDefinition, scope);
                            final ApplyDataSetRowAction apply = new ApplyDataSetRowAction(actionDefinition, parameters, scope);

                            // Create runnable action
                            return RunnableAction.Builder.builder() //
                                    .withCompile(compile) //
                                    .withRow(apply) //
                                    .withName(a.getName()) //
                                    .withParameters(new HashMap<>(parameters)) //
                                    .build();
                        }) //
                        .collect(Collectors.toList());
            } else {
                LOG.debug("Running using submitted actions ({} action(s))", actions.size());
                runnableActions = actions;
            }

            // Build nodes for actions
            final Node actionsNode = ActionNodesBuilder.builder() //
                    .initialMetadata(rowMetadata) //
                    .actions(runnableActions) //
                    // statistics requests
                    .needStatisticsBefore(!completeMetadata) //
                    .needStatisticsAfter(needGlobalStatistics) //
                    .allowSchemaAnalysis(allowMetadataChange) //
                    // statistics dependencies/arguments
                    .actionRegistry(actionRegistry) //
                    .analyzerService(analyzerService) //
                    .statisticsAdapter(adapter) //
                    .build();

            if (preparation != null) {
                LOG.debug("Applying step node transformations...");
                actionsNode.logStatus(LOG, "Before transformation\n{}");
                final Node node = StepNodeTransformer.transform(actionsNode, preparation.getSteps(), rowMetadataSupplier);
                current.to(node);
                node.logStatus(LOG, "After transformation\n{}");
            } else {
                current.to(actionsNode);
            }

            // Output
            if (outFilter != null) {
                current.to(new FilteredNode(outFilter));
            }

            Pipeline pipeline = new Pipeline();

            // set the maximum number of output rows
            if (limit != null && limit > 0) {
                current.to(new LimitNode(limit, () -> pipeline.signal(Signal.STOP))); // limit the output
            }

            current.to(outputSupplier.get());
            current.to(monitorSupplier.get());

            pipeline.setNode(current.build());

            // Finally build pipeline
            return pipeline;
        }
    }
}
