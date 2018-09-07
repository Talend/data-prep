package org.talend.dataprep.transformation.pipeline.node;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private RowMetadata workingMetadata;

    public ReactiveTypeDetectionNode(RowMetadata initialRowMetadata, //
            Predicate<String> filter, //
            StatisticsAdapter adapter, //
            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
        super(filter, initialRowMetadata);
        this.analyzer = analyzer;
        this.adapter = adapter;
        this.workingMetadata = initialRowMetadata;

        this.processor = ReplayProcessor.create(30000, false);
        this.sink = processor.connectSink();
        processor.subscribe(row -> {
            LOGGER.trace("Analyze row: {}", row);
            final long start = System.currentTimeMillis();
            try {
                analyze(row);
            } finally {
                totalTime += System.currentTimeMillis() - start;
            }
        });
    }

    private void initAnalyzer() {
        if (resultAnalyzer == null) {
            final List<ColumnMetadata> filteredColumns =
                    getFilteredColumns(workingMetadata).collect(Collectors.toList());
            resultAnalyzer = analyzer.apply(filteredColumns);
        }
    }

    private void analyze(DataSetRow row) {
        if (!row.isDeleted()) {
            final List<String> columnNames = getColumnNames();
            final String[] values = row.toArray(DataSetRow.SKIP_TDP_ID.and(e -> columnNames.contains(e.getKey())));
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
        this.workingMetadata = metadata;
        initAnalyzer();
        sink.emit(row);
    }

    @Override
    public void signal(Signal signal) {
        try {
            switch (signal) {
            case END_OF_STREAM:
            case STOP:
            case CANCEL:
            default:
                final long start = System.currentTimeMillis();
                try {
                    initAnalyzer();

                    // Adapt row metadata to infer type (adapter takes care of type-forced columns)
                    resultAnalyzer.end();
                    final List<ColumnMetadata> columns =
                            getFilteredColumns(workingMetadata).collect(Collectors.toList());
                    adapter.adapt(columns, resultAnalyzer.getResult(), filter);
                } finally {
                    totalTime += System.currentTimeMillis() - start;
                    resultAnalyzer.close();
                }

                processor.onComplete();
                processor.subscribe(row -> {
                    LOGGER.trace("forward row: {}", row);
                    link.exec().emit(row, workingMetadata);
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
        return new ReactiveTypeDetectionNode(workingMetadata, filter, adapter, analyzer);
    }

    @Override
    public List<String> getColumnNames() {
        return getFilteredColumns(workingMetadata).map(ColumnMetadata::getId).collect(Collectors.toList());
    }

    private Stream<ColumnMetadata> getFilteredColumns(RowMetadata metadata) {
        return metadata.getColumns().stream().filter(c -> filter.test(c.getId()) && !c.isTypeForced());
    }
}
