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

package org.talend.dataprep.transformation.pipeline.node;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

/**
 * <p>
 * This node performs statistical analysis.
 * </p>
 * <p>
 * Please note this class does not perform invalid values detection (see {@link InvalidDetectionNode} for this).
 * </p>
 */
public class StatisticsNode extends ColumnFilteredNode implements Monitored {

    private final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    private final StatisticsAdapter adapter;

    private final Analyzer<Analyzers.Result> configuredAnalyzer;

    private long totalTime;

    private long count;

    private RowMetadata workingMetadata;

    public StatisticsNode(RowMetadata initialRowMetadata, //
                          Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer, //
                          Predicate<String> filter, //
                          StatisticsAdapter adapter) {
        super(filter, initialRowMetadata);
        this.analyzer = analyzer;
        this.adapter = adapter;
        this.workingMetadata = initialRowMetadata.clone();

        final List<ColumnMetadata> filteredColumns = getFilteredColumns(workingMetadata).collect(Collectors.toList());
        this.configuredAnalyzer = analyzer.apply(filteredColumns);
    }

    /**
     * Construct a statisticsNode performing default analysis which are :
     * quality, cardinality, frequency, patterns, the length, quantiles, summary and histogram analysis.
     *
     * @param analyzerService the analyzer service to use
     * @param filter the filter to apply on values of a column
     * @param adapter the adapter used to retrieve statistical information
     */
    public StatisticsNode(RowMetadata initialRowMetadata, //
                          AnalyzerService analyzerService, //
                          Predicate<String> filter, StatisticsAdapter adapter) {
        this(initialRowMetadata, getDefaultAnalyzer(analyzerService), filter, adapter);
    }

    /**
     * Creates a default analyzer with te specified analyzer service.
     * This analyzer performs quality, cardinality, frequency, patterns, the length, quantiles, summary and histogram
     * analysis.
     *
     * @param analyzerService the provided analyzer service
     */
    public static Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>>
            getDefaultAnalyzer(AnalyzerService analyzerService) {
        return c -> analyzerService.build(c, //
                AnalyzerService.Analysis.QUALITY, //
                AnalyzerService.Analysis.CARDINALITY, //
                AnalyzerService.Analysis.FREQUENCY, //
                AnalyzerService.Analysis.PATTERNS, //
                AnalyzerService.Analysis.LENGTH, //
                AnalyzerService.Analysis.QUANTILES, //
                AnalyzerService.Analysis.SUMMARY, //
                AnalyzerService.Analysis.HISTOGRAM);
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        this.workingMetadata = metadata;
        final long start = System.currentTimeMillis();
        try {
            if (!row.isDeleted()) {
                final List<String> columnNames = getColumnNames();
                String[] values = new String[columnNames.size()];
                int i = 0;
                for (String columnName : columnNames) {
                    final String rowValue = row.get(columnName);
                    values[i++] = rowValue == null ? StringUtils.EMPTY : rowValue;
                }
                configuredAnalyzer.analyze(values);
            }
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
        super.receive(row, metadata);
    }

    @Override
    public void signal(Signal signal) {
        switch (signal) {
        case END_OF_STREAM:
        case STOP:
        case CANCEL:
        default:
            final long start = System.currentTimeMillis();
            try {
                final List<ColumnMetadata> filteredColumns = getFilteredColumns(workingMetadata) //
                        .collect(Collectors.toList());
                adapter.adapt(filteredColumns, configuredAnalyzer.getResult());
            } finally {
                totalTime += System.currentTimeMillis() - start;
            }
        }
        if (signal == Signal.END_OF_STREAM || signal == Signal.STOP) {
            // In the end of records the row metaData are correct, so we set this metaData as fallback
            // To send number of records to front we use the number of count of statistic.
            if (workingMetadata.getColumns().get(0).getStatistics() != null) {
                workingMetadata.setSampleNbRows(workingMetadata.getColumns().get(0).getStatistics().getCount());
            }
        }
        super.signal(signal);
    }

    @Override
    public Node copyShallow() {
        return new StatisticsNode(workingMetadata, analyzer, filter, adapter);
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
    public List<String> getColumnNames() {
        return getFilteredColumns(workingMetadata) //
                .map(ColumnMetadata::getId) //
                .collect(Collectors.toList());
    }

    private Stream<ColumnMetadata> getFilteredColumns(RowMetadata metadata) {
        return metadata
                .getColumns()
                .stream() //
                .filter(c -> filter.test(c.getId()));
    }

}
