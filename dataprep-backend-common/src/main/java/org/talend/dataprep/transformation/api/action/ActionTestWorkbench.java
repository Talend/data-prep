// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.api.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.talend.dataprep.api.dataset.*;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.node.TerminalNode;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class ActionTestWorkbench {

    private ActionTestWorkbench() {
    }

    public static void test(RowMetadata rowMetadata, Action... actions) {
        test(new DataSetRow(rowMetadata), actions);
    }

    public static void test(DataSetRow input, Action... actions) {
        test(Collections.singletonList(input), actions);
    }

    public static void test(Collection<DataSetRow> input, Action... actions) {
        test(input, c -> Analyzers.with(NullAnalyzer.INSTANCE), c -> Analyzers.with(NullAnalyzer.INSTANCE), actions);
    }

    public static void test(Collection<DataSetRow> input,
                            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> inlineAnalysis,
                            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> delayedAnalysis,
                            Action... actions) {
        TransformationContext context = new TransformationContext();
        final List<Action> allActions = new ArrayList<>();
        Collections.addAll(allActions, actions);

        final DataSet dataSet = new DataSet();
        final RowMetadata rowMetadata = input.iterator().next().getRowMetadata();
        final DataSetMetadata dataSetMetadata = new DataSetMetadata();
        dataSetMetadata.setRowMetadata(rowMetadata);
        dataSet.setMetadata(dataSetMetadata);
        dataSet.setRecords(input.stream());
        final TestOutputNode outputNode = new TestOutputNode();
        Pipeline pipeline = Pipeline.Builder.builder() //
                .withInitialMetadata(rowMetadata) //
                .withActions(allActions) //
                .withContext(context) //
                .withInlineAnalysis(inlineAnalysis)
                .withDelayedAnalysis(delayedAnalysis)
                .withStatisticsAdapter(new StatisticsAdapter()) //
                .withOutput(() -> outputNode) //
                .build();
        pipeline.execute(dataSet);

        // Some tests rely on the metadata changes in the provided metadata so set back modified columns in row metadata
        // (although this should be avoided in tests).
        // TODO Make this method return the modified metadata iso. setting modified columns.
        rowMetadata.setColumns(outputNode.getMetadata().getColumns());
    }

    private static class TestOutputNode extends TerminalNode {

        private RowMetadata metadata;

        public RowMetadata getMetadata() {
            return metadata;
        }

        @Override
        public void receive(DataSetRow row, RowMetadata metadata) {
            this.metadata = metadata;
            row.setRowMetadata(this.metadata);
        }

    }
}
