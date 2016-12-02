package org.talend.dataprep.preparation.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.preparation.service.StepDiffDelegateTest.CompileAnswer.answer;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@RunWith(MockitoJUnitRunner.class)
public class StepDiffDelegateTest {

    private StepDiffDelegate stepDiffDelegate = new StepDiffDelegate();

    @Mock
    private RunnableAction firstAction;

    @Mock
    private DataSetRowAction firstActionCompiler;

    @Mock
    private RunnableAction secondAction;

    @Mock
    private DataSetRowAction secondActionCompiler;

    @Before
    public void setUp() {
        when(firstAction.getRowAction()).thenReturn(firstActionCompiler);
        when(secondAction.getRowAction()).thenReturn(secondActionCompiler);
    }

    @Test
    public void getActionCreatedColumns_firstActionCreatedColumnDoesNotCount() throws Exception {
        RowMetadata workingMetadata = new RowMetadata();
        workingMetadata.addColumn(createColumnNamed("do"));
        workingMetadata.addColumn(createColumnNamed("ré"));
        workingMetadata.addColumn(createColumnNamed("mi"));
        doAnswer(answer(newArrayList("foo", "bar"), emptyList())).when(firstActionCompiler).compile(any(ActionContext.class));
        doAnswer(answer(newArrayList("beer"), newArrayList("do", "ré"))).when(secondActionCompiler).compile(any(ActionContext.class));

        StepDiff stepDiff = stepDiffDelegate.getActionCreatedColumns(workingMetadata, newArrayList(firstAction),
                newArrayList(secondAction));

        assertEquals(newArrayList("0005"), stepDiff.getCreatedColumns());
    }

    @Test
    public void getActionCreatedColumns_should_return_created_columns_for_multiple_diffs() throws Exception {
        RowMetadata workingMetadata = new RowMetadata();
        workingMetadata.addColumn(createColumnNamed("do"));
        workingMetadata.addColumn(createColumnNamed("ré"));
        workingMetadata.addColumn(createColumnNamed("mi"));
        doAnswer(answer(newArrayList("foo", "bar"), emptyList())).when(firstActionCompiler).compile(any(ActionContext.class));
        doAnswer(answer(emptyList(), newArrayList("do", "ré"))).when(secondActionCompiler).compile(any(ActionContext.class));

        StepDiff stepDiff = stepDiffDelegate.getActionCreatedColumns(workingMetadata, newArrayList(firstAction, secondAction));

        assertEquals(newArrayList("0003", "0004"), stepDiff.getCreatedColumns());
    }

    private static ColumnMetadata createColumnNamed(String toto) {
        ColumnMetadata firstCol = new ColumnMetadata();
        firstCol.setName(toto);
        return firstCol;
    }

    static class CompileAnswer implements org.mockito.stubbing.Answer<Void> {

        private final List<String> columnsToAdd;

        private final List<String> columnsToRemove;

        /** For the sake of fluency! Yay! **/
        static CompileAnswer answer(List<String> columnsToAdd, List<String> columnsToRemove) {
            return new CompileAnswer(columnsToAdd, columnsToRemove);
        }

        private CompileAnswer(List<String> columnsToAdd, List<String> columnsToRemove) {
            this.columnsToAdd = columnsToAdd;
            this.columnsToRemove = columnsToRemove;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            RowMetadata rowMetadata = ((ActionContext) invocation.getArguments()[0]).getRowMetadata();
            for (String addedColumn : columnsToAdd) {
                ColumnMetadata columnMetadata = createColumnNamed(addedColumn);
                rowMetadata.addColumn(columnMetadata);
            }
            for (String columnToRemove : columnsToRemove) {
                Optional<ColumnMetadata> matchingColumn = rowMetadata.getColumns() //
                        .stream() //
                        .filter(c -> columnToRemove.equals(c.getName())) //
                        .findAny();
                matchingColumn.ifPresent(columnMetadata -> rowMetadata.deleteColumnById(columnMetadata.getId()));
            }

            return null;
        }
    }

}
