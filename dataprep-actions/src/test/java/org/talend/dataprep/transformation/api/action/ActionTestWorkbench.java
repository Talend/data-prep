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

package org.talend.dataprep.transformation.api.action;

import java.util.*;

import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

public class ActionTestWorkbench {

    private ActionTestWorkbench() {
    }

    public static void test(RowMetadata rowMetadata, ActionRegistry actionRegistry, RunnableAction... actions) {
        test(new DataSetRow(rowMetadata), actionRegistry, actions);
    }

    public static void test(DataSetRow input, ActionRegistry actionRegistry, RunnableAction... actions) {
        test(Collections.singletonList(input), actionRegistry, actions);
    }

    public static void test(Collection<DataSetRow> input, ActionRegistry actionRegistry, RunnableAction... actions) {
        test(input, null, actionRegistry, actions);
    }

    public static void test(Collection<DataSetRow> input,
                            AnalyzerService analyzerService,
                            ActionRegistry actionRegistry,
                            RunnableAction... actions) {
        final List<RunnableAction> allActions = new ArrayList<>();
        Collections.addAll(allActions, actions);

        final DataSet dataSet = new DataSet();
        final RowMetadata rowMetadata = input.iterator().next().getRowMetadata();
        final DataSetMetadata dataSetMetadata = new DataSetMetadata();
        dataSetMetadata.setRowMetadata(rowMetadata);
        dataSet.setMetadata(dataSetMetadata);
        dataSet.setRecords(input.stream());
        final TestOutputNode outputNode = new TestOutputNode(input);
        Pipeline pipeline = Pipeline.Builder.builder() //
                .withActionRegistry(actionRegistry)
                .withInitialMetadata(rowMetadata, true) //
                .withActions(allActions) //
                .withAnalyzerService(analyzerService)
                .withStatisticsAdapter(new StatisticsAdapter(40)) //
                .withOutput(() -> outputNode) //
                .build();
        pipeline.execute(dataSet);

        // Some tests rely on the metadata changes in the provided metadata so set back modified columns in row metadata
        // (although this should be avoided in tests).
        // TODO Make this method return the modified metadata iso. setting modified columns.
        rowMetadata.setColumns(outputNode.getMetadata().getColumns());
        for (DataSetRow dataSetRow : input) {
            dataSetRow.setRowMetadata(rowMetadata);
        }
    }

    private static class TestOutputNode extends BasicNode {

        private final Iterator<DataSetRow> input;

        private RowMetadata metadata;

        public TestOutputNode(Collection<DataSetRow> input) {
            this.input = input.iterator();
        }

        public RowMetadata getMetadata() {
            return metadata;
        }

        @Override
        public void receive(DataSetRow row, RowMetadata metadata) {
            if (input.hasNext()) {
                final DataSetRow next = input.next();
                next.values().clear();
                next.values().putAll(row.values());
                for (Map.Entry<String, String> entry : row.getInternalValues().entrySet()) {
                    next.set(entry.getKey(), entry.getValue());
                }
            }
            if (!row.isDeleted() || this.metadata == null) {
                this.metadata = metadata;
                row.setRowMetadata(this.metadata);
            }
        }

    }
}
