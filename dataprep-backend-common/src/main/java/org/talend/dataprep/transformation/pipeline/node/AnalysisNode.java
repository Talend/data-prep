package org.talend.dataprep.transformation.pipeline.node;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public abstract class AnalysisNode extends BasicNode {

    protected final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    protected final Predicate<ColumnMetadata> filter;

    protected final StatisticsAdapter adapter;

    public AnalysisNode(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer, Predicate<ColumnMetadata> filter,
            StatisticsAdapter adapter) {
        this.analyzer = analyzer;
        this.filter = filter;
        this.adapter = adapter;
    }

}
