package org.talend.dataprep.transformation.pipeline.node;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class InlineAnalysisNode extends AnalysisNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisNode.class);

    private List<ColumnMetadata> previousColumns = Collections.emptyList();

    private Analyzer<Analyzers.Result> inlineAnalyzer = Analyzers.with(NullAnalyzer.INSTANCE);

    private long totalTime;

    private long count;

    public InlineAnalysisNode(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer,
            Predicate<ColumnMetadata> filter, StatisticsAdapter adapter) {
        super(analyzer, filter, adapter);
    }

    @Override
    public void receive(final DataSetRow row, final RowMetadata metadata) {
        // Reuse or re-init previously created analyzer
        final List<ColumnMetadata> rowColumns = metadata.getColumns();
        if (newAnalyzerNeeded(rowColumns)) {
            LOGGER.debug("Need to reconfigure inline analyzer.");
            try {
                if (inlineAnalyzer != null) {
                    inlineAnalyzer.close();
                }
            } catch (Exception e) {
                LOGGER.debug("Unable to close previously initialized analyzer.", e);
            }
            inlineAnalyzer = analyzer.apply(rowColumns);
            inlineAnalyzer.init();
            LOGGER.debug("Inline analyzer reconfigured.");
        }
        // Analyze received row
        final long start = System.currentTimeMillis();
        try {
            // Some clean up before order
            final DataSetRow analysisRow = row.clone();
            Set<String> columnsToRemove = analysisRow.values().entrySet().stream().map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            for (ColumnMetadata column : rowColumns) {
                columnsToRemove.remove(column.getId());
            }
            columnsToRemove.forEach(analysisRow::deleteColumnById);
            // Analyze row
            final DataSetRow orderedRow = analysisRow.order(rowColumns);
            final String[] array = orderedRow.toArray(DataSetRow.SKIP_TDP_ID);
            int filteredOutValues = 0;
            for (int i = 0; i < rowColumns.size(); i++) {
                if (!filter.test(rowColumns.get(i))) { // Removes non needed values from analysis
                    array[i] = null;
                    filteredOutValues++;
                }
            }
            LOGGER.trace("{}/{} value(s) filtered out during analysis (in #{})", filteredOutValues, rowColumns.size(),
                    analysisRow.getTdpId());
            inlineAnalyzer.analyze(array);
            try {
                adapter.adapt(previousColumns, inlineAnalyzer.getResult(), filter);
            } catch (Exception e) {
                // Ignore
                LOGGER.debug("Unable to adapt inline analysis.", e);
            }
        } catch (Exception e) {
            LOGGER.warn("Unexpected exception during on the fly analysis.", e);
        } finally {
            previousColumns = rowColumns;
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
        link.exec().emit(row, metadata);
    }

    private boolean newAnalyzerNeeded(List<ColumnMetadata> rowColumns) {
        if (previousColumns.size() != rowColumns.size()) {
            return true;
        }
        for (int i = 0; i < previousColumns.size(); i++) {
            final ColumnMetadata previousColumn = previousColumns.get(i);
            final ColumnMetadata rowColumn = rowColumns.get(i);
            if (!previousColumn.getType().equals(rowColumn.getType())) {
                return true;
            }
            if (!previousColumn.getDomain().equals(rowColumn.getDomain())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitInlineAnalysis(this);
    }

    @Override
    public void signal(Signal signal) {
        try {
            if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL) {
                adapter.adapt(previousColumns, inlineAnalyzer.getResult(), filter);
            }
        } finally {
            try {
                inlineAnalyzer.close();
            } catch (Exception e) {
                LOGGER.debug("Unable to close inline analyzer.", e);
            }
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
}
