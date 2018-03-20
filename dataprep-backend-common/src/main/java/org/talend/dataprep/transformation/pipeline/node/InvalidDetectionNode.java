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

import java.util.function.Predicate;

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

    private long totalTime;

    private long count;

    private transient InvalidMarker invalidMarker;

    private transient Analyzer<Analyzers.Result> configuredAnalyzer;

    private transient AnalyzerService analyzerService;

    public InvalidDetectionNode(final Predicate<? super ColumnMetadata> filter) {
        super(filter);
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            performColumnFilter(row, metadata);
            if (configuredAnalyzer == null) {
                this.configuredAnalyzer = getAnalyzerService().build(filteredColumns, AnalyzerService.Analysis.QUALITY);
                this.invalidMarker = new InvalidMarker(filteredColumns, configuredAnalyzer);
            }
            super.receive(invalidMarker.apply(row), metadata);
        } finally {
            totalTime += System.currentTimeMillis() - start;
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
    public Node copyShallow() {
        return new InvalidDetectionNode(filter);
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
