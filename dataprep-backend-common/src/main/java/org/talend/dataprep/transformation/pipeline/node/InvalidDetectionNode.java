package org.talend.dataprep.transformation.pipeline.node;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

/**
 * Allow to mark (and unmark) invalid values in rows that go through the pipeline.
 */
public class InvalidDetectionNode extends ColumnFilteredNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidDetectionNode.class);

    private final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    private InvalidMarker invalidMarker;

    private Analyzer<Analyzers.Result> configuredAnalyzer;

    private long totalTime;

    private long count;

    public InvalidDetectionNode(final AnalyzerService analyzerService, final Predicate<? super ColumnMetadata> filter) {
        super(filter);
        this.analyzer = c -> analyzerService.build(c, AnalyzerService.Analysis.QUALITY);
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            performColumnFilter(row, metadata);
            if (configuredAnalyzer == null) {
                this.configuredAnalyzer = analyzer.apply(filteredColumns);
                this.invalidMarker = new InvalidMarker(filteredColumns, configuredAnalyzer);
            }
            configuredAnalyzer.analyze(
                    row.filter(filteredColumns).order(filteredColumns).toArray(DataSetRow.SKIP_TDP_ID));
            super.receive(invalidMarker.apply(row), metadata);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    @Override
    public void signal(Signal signal) {
        try {
            if (configuredAnalyzer != null) {
                configuredAnalyzer.close();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to close analyzer.", e);
        }
        super.signal(signal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
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
