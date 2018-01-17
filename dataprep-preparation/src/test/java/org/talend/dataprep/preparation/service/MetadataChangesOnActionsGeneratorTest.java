// ============================================================================
//
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

package org.talend.dataprep.preparation.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.preparation.service.MetadataChangesOnActionsGeneratorTest.CompileAnswer.answer;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.DisposableBean;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@RunWith(MockitoJUnitRunner.class)
public class MetadataChangesOnActionsGeneratorTest {

    private MetadataChangesOnActionsGenerator onActionsGenerator = new MetadataChangesOnActionsGenerator();

    @Mock
    private RunnableAction firstAction;
    @Mock
    private DataSetRowAction firstRowAction;

    @Mock
    private DisposableBean firstActionStuffInActionContext;

    @Mock
    private RunnableAction secondAction;
    @Mock
    private DataSetRowAction secondRowAction;

    @Mock
    private DisposableBean secondActionStuffInActionContext;

    @Before
    public void setUp() {
        when(firstAction.getRowAction()).thenReturn(firstRowAction);
        when(secondAction.getRowAction()).thenReturn(secondRowAction);
    }

    @Test
    public void getActionCreatedColumns_firstActionCreatedColumnDoesNotCount() throws Exception {
        // given
        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createColumnNamed("do"));
        rowMetadata.addColumn(createColumnNamed("ré"));
        rowMetadata.addColumn(createColumnNamed("mi"));

        // when
        doAnswer(answer(newArrayList("foo", "bar"), emptyList(), firstActionStuffInActionContext)) //
                .when(firstRowAction) //
                .compile(any(ActionContext.class) //
        );
        doAnswer(answer(newArrayList("beer"), newArrayList("do", "ré"), secondActionStuffInActionContext)) //
                .when(secondRowAction) //
                .compile(any(ActionContext.class) //
        );

        StepDiff diff = onActionsGenerator.computeCreatedColumns(rowMetadata, newArrayList(firstAction),
                newArrayList(secondAction));

        // then
        assertEquals(newArrayList("0005"), diff.getCreatedColumns());
    }

    @Test
    public void getActionCreatedColumns_should_return_created_columns_for_multiple_diffs() throws Exception {

        // given
        RowMetadata workingMetadata = new RowMetadata();
        workingMetadata.addColumn(createColumnNamed("do"));
        workingMetadata.addColumn(createColumnNamed("ré"));
        workingMetadata.addColumn(createColumnNamed("mi"));

        // when
        doAnswer(answer(newArrayList("foo", "bar"), emptyList(), null)) //
                .when(firstRowAction) //
                .compile(any(ActionContext.class) //
        );
        doAnswer(answer(emptyList(), newArrayList("do", "ré"), null)) //
                .when(secondRowAction) // s
                .compile(any(ActionContext.class) //
        );

        StepDiff stepDiff = onActionsGenerator.computeCreatedColumns(newArrayList(firstAction, secondAction), workingMetadata);

        // then
        assertEquals(newArrayList("0003", "0004"), stepDiff.getCreatedColumns());
    }

    @Test
    public void getActionCreatedColumns_should_cleanup_transformation_context() throws Exception {

        // given
        RowMetadata workingMetadata = new RowMetadata();
        workingMetadata.addColumn(createColumnNamed("do"));

        // when
        doAnswer(answer(emptyList(), emptyList(), firstActionStuffInActionContext)) //
                .when(firstRowAction) //
                .compile(any(ActionContext.class) //
        );
        doAnswer(answer(emptyList(), emptyList(), secondActionStuffInActionContext)) //
                .when(secondRowAction) //
                .compile(any(ActionContext.class) //
        );

        onActionsGenerator.computeCreatedColumns(newArrayList(firstAction, secondAction), workingMetadata);

        // then
        verify(firstActionStuffInActionContext).destroy();
        verify(secondActionStuffInActionContext).destroy();
    }

    private static ColumnMetadata createColumnNamed(String name) {
        ColumnMetadata firstCol = new ColumnMetadata();
        firstCol.setName(name);
        return firstCol;
    }

    static class CompileAnswer implements org.mockito.stubbing.Answer<Void> {

        private final List<String> columnsToAdd;
        private final List<String> columnsToRemove;

        private final DisposableBean stuffForActionContext;

        /** For the sake of fluency! Yay! **/
        static CompileAnswer answer(List<String> columnsToAdd, List<String> columnsToRemove,
                DisposableBean stuffForActionContext) {
            return new CompileAnswer(columnsToAdd, columnsToRemove, stuffForActionContext);
        }

        private CompileAnswer(List<String> columnsToAdd, List<String> columnsToRemove, DisposableBean stuffForActionContext) {
            this.columnsToAdd = columnsToAdd;
            this.columnsToRemove = columnsToRemove;
            this.stuffForActionContext = stuffForActionContext;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final ActionContext actionContext = (ActionContext) invocation.getArguments()[0];

            // update the row metadata with the created / removed columns
            RowMetadata rowMetadata = actionContext.getRowMetadata();
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

            // add stuff in the action context
            if (stuffForActionContext != null) {
                actionContext.get("rowMatcher", p -> stuffForActionContext);
            }

            return null;
        }
    }

}
