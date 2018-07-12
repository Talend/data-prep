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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class InvalidDetectionNode extends ColumnFilteredNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidDetectionNode.class);

    private transient final InvalidMarker invalidMarker;

    private transient final Analyzer<Analyzers.Result> configuredAnalyzer;

    private transient AnalyzerService analyzerService;

    private long totalTime;

    private long count;

    public InvalidDetectionNode(RowMetadata initialRowMetadata, Predicate<String> filter) {
        super(filter, initialRowMetadata);

        final List<ColumnMetadata> filteredColumns = getFilteredColumns().collect(Collectors.toList());
        this.configuredAnalyzer = getAnalyzerService().build(filteredColumns, AnalyzerService.Analysis.QUALITY);
        this.invalidMarker = new InvalidMarker(filteredColumns, configuredAnalyzer);
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {

            final DataSetRow markedRow = invalidMarker.apply(row);
            totalTime += System.currentTimeMillis() - start;
            super.receive(markedRow, metadata);
        } finally {
            count++;
        }
    }

    private AnalyzerService getAnalyzerService() {
        if (analyzerService == null) {
            this.analyzerService = Providers.get(AnalyzerService.class);
        }
        return analyzerService;
    }

    @Override
    public void signal(Signal signal) {
        try {
            configuredAnalyzer.close();
        } catch (Exception e) {
            LOGGER.error("Unable to close analyzer.", e);
        }
        super.signal(signal);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitInvalidDetection(this);
    }

    @Override
    public Node copyShallow() {
        return new InvalidDetectionNode(initialRowMetadata, filter);
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
        return getFilteredColumns() //
                .map(ColumnMetadata::getId) //
                .collect(Collectors.toList());
    }

    private Stream<ColumnMetadata> getFilteredColumns() {
        return initialRowMetadata
                .getColumns()
                .stream() //
                .filter(c -> filter.test(c.getId()));
    }
}
