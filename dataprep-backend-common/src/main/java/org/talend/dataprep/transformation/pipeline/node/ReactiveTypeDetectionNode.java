package org.talend.dataprep.transformation.pipeline.node;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.ReplayProcessor;

public class ReactiveTypeDetectionNode extends ColumnFilteredNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveTypeDetectionNode.class);

    private final ReplayProcessor<DataSetRow> processor;

    private final BlockingSink<DataSetRow> sink;

    private final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    private final StatisticsAdapter adapter;

    private Analyzer<Analyzers.Result> resultAnalyzer;

    private long totalTime;

    private long count;

    public ReactiveTypeDetectionNode(Predicate<? super ColumnMetadata> filter, StatisticsAdapter adapter,
            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
        super(filter);
        this.analyzer = analyzer;
        this.adapter = adapter;

        this.processor = ReplayProcessor.create(10000, false);
        this.sink = processor.connectSink();
        final Random random = new Random();
        processor.subscribe(row -> {
            performColumnFilter(row, row.getRowMetadata());
            if (random.nextDouble() <= 0.3d) {
                LOGGER.trace("Analyze row: {}", row);
                final long start = System.currentTimeMillis();
                try {
                    analyze(row);
                } finally {
                    totalTime += System.currentTimeMillis() - start;
                }
            } else {
                LOGGER.trace("Skip row (probabilistic analysis skipped row).");
            }
        });
    }

    private void analyze(DataSetRow row) {
        if (!row.isDeleted()) {
            // Lazy initialization of the result analyzer
            if (resultAnalyzer == null) {
                resultAnalyzer = analyzer.apply(filteredColumns);
            }

            final String[] values = row.toArray(DataSetRow.SKIP_TDP_ID.and(e -> getColumnNames().contains(e.getKey())));
            try {
                resultAnalyzer.analyze(values);
            } catch (Exception e) {
                LOGGER.debug("Unable to analyze row '{}'.", Arrays.toString(values), e);
            }

            count++;
        }
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        sink.emit(row);
    }

    @Override
    public void signal(Signal signal) {
        try {
            if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) {
                final long start = System.currentTimeMillis();
                try {
                    if (rowMetadata != null && resultAnalyzer != null) {
                        // Adapt row metadata to infer type (adapter takes care of type-forced columns)
                        resultAnalyzer.end();
                        final List<ColumnMetadata> columns = rowMetadata.getColumns();
                        adapter.adapt(columns, resultAnalyzer.getResult(), (Predicate<ColumnMetadata>) filter);
                        resultAnalyzer.close();
                    }
                } finally {
                    totalTime += System.currentTimeMillis() - start;
                }

                processor.onComplete();
                processor.subscribe(row -> {
                    LOGGER.trace("forward row: {}", row);
                    link.exec().emit(row, rowMetadata);
                });
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to perform delayed analysis.", e);
        }
        super.signal(signal);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public Node copyShallow() {
        return new ReactiveTypeDetectionNode(filter, adapter, analyzer);
    }
}
