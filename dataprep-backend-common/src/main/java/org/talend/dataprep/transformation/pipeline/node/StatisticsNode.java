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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
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
public class StatisticsNode extends ColumnFilteredNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsNode.class);

    private final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    private final StatisticsAdapter adapter;

    private Analyzer<Analyzers.Result> configuredAnalyzer;

    public StatisticsNode(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer,
                          Predicate<? super ColumnMetadata> filter,
                          StatisticsAdapter adapter) {
        super(filter);
        this.analyzer = analyzer;
        this.adapter = adapter;
    }

    /**
     * Construct a statisticsNode performing default analysis which are :
     * quality, cardinality, frequency, patterns, the length, quantiles, summary and histogram analysis.
     *
     * @param analyzerService the analyzer service to use
     * @param filter the filter to apply on values of a column
     * @param adapter the adapter used to retrieve statistical information
     */
    public StatisticsNode(AnalyzerService analyzerService, Predicate<ColumnMetadata> filter, StatisticsAdapter adapter) {
        this(getDefaultAnalyzer(analyzerService), filter, adapter);
    }

    /**
     * Creates a default analyzer with te specified analyzer service.
     * This analyzer performs quality, cardinality, frequency, patterns, the length, quantiles, summary and histogram analysis.
     *
     * @param analyzerService the provided analyzer service
     */
    public static Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> getDefaultAnalyzer(AnalyzerService analyzerService) {
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
        performColumnFilter(row, metadata);
        if (configuredAnalyzer == null) {
            this.configuredAnalyzer = analyzer.apply(filteredColumns);
        }
        if (!row.isDeleted()) {
            configuredAnalyzer.analyze(row.filter(filteredColumns).order(filteredColumns).toArray(DataSetRow.SKIP_TDP_ID));
        }
        super.receive(row, metadata);
    }

    @Override
    public void signal(Signal signal) {
        if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) {
            if (configuredAnalyzer != null) {
                adapter.adapt(filteredColumns, configuredAnalyzer.getResult());
            } else {
                LOGGER.warn("No data received.");
            }
        }
        super.signal(signal);
    }

    @Override
    public Node copyShallow() {
        return new StatisticsNode(analyzer, filter, adapter);
    }
}
