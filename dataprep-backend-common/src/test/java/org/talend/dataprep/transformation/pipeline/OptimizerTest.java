package org.talend.dataprep.transformation.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.transformation.pipeline.builder.NodeBuilder.source;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.ApplyToColumn;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.ReactiveTypeDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class OptimizerTest {

    @Test
    public void shouldRemoveBasicNodes() {
        // given
        final Node node = source().to(new BasicNode()).to(new TestNode()).build();

        // when
        final Optimizer optimizer = new Optimizer();
        node.accept(optimizer);
        final Pipeline optimized = optimizer.getOptimized();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        optimized.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        assertEquals(4, traversedClasses.size());
        assertTrue(traversedClasses.contains(Pipeline.class));
        assertTrue(traversedClasses.contains(TestNode.class));
        assertTrue(traversedClasses.contains(SourceNode.class));
        assertTrue(traversedClasses.contains(BasicLink.class));
    }

    @Test
    public void shouldRemoveNodeOnZeroColumns() {
        // given
        final Node node = source().to(new ApplyToColumnTestNode()).build();

        // when
        final Optimizer optimizer = new Optimizer();
        node.accept(optimizer);
        final Pipeline optimized = optimizer.getOptimized();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        optimized.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        assertEquals(2, traversedClasses.size());
        assertTrue(traversedClasses.contains(Pipeline.class));
        assertTrue(traversedClasses.contains(SourceNode.class));
    }

    @Test
    public void shouldRemoveUselessTypeDetection() {
        // given
        final RowMetadata rowMetadata = mock(RowMetadata.class);
        final Predicate<String> filter = columnMetadata -> true;
        final StatisticsAdapter adapter = mock(StatisticsAdapter.class);
        final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer = c -> NullAnalyzer.INSTANCE;
        final ReactiveTypeDetectionNode typeDetectionNode =
                new ReactiveTypeDetectionNode(rowMetadata, filter, adapter, analyzer);
        final Node node = source().to(typeDetectionNode).build();

        // when
        final Optimizer optimizer = new Optimizer();
        node.accept(optimizer);
        final Pipeline optimized = optimizer.getOptimized();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        optimized.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        assertEquals(2, traversedClasses.size());
        assertTrue(traversedClasses.contains(Pipeline.class));
        assertTrue(traversedClasses.contains(SourceNode.class));
    }

    @Test
    public void shouldNotRemoveTypeDetection() {
        // given
        final ColumnMetadata column = mock(ColumnMetadata.class);
        when(column.getId()).thenReturn("0001");
        final RowMetadata rowMetadata = mock(RowMetadata.class);
        when(rowMetadata.getColumns()).thenReturn(Collections.singletonList(column));
        final Predicate<String> filter = "0001"::equals;
        final StatisticsAdapter adapter = mock(StatisticsAdapter.class);
        final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer = c -> NullAnalyzer.INSTANCE;
        final ReactiveTypeDetectionNode typeDetectionNode =
                new ReactiveTypeDetectionNode(rowMetadata, filter, adapter, analyzer);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        actionContext.setParameters(Collections.singletonMap(ImplicitParameters.COLUMN_ID.getKey(), "0001"));
        final RunnableAction runnableAction = mock(RunnableAction.class);
        final Node node = source() //
                .to(new CompileNode(runnableAction, actionContext)) //
                .to(new ActionNode(runnableAction, actionContext)) //
                .to(typeDetectionNode) //
                .build();

        // when
        final Optimizer optimizer = new Optimizer();
        node.accept(optimizer);
        final Pipeline optimized = optimizer.getOptimized();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        optimized.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        assertEquals(8, traversedClasses.size());
        assertTrue(traversedClasses.contains(Pipeline.class));
        assertTrue(traversedClasses.contains(SourceNode.class));
        assertTrue(traversedClasses.contains(BasicLink.class));
        assertTrue(traversedClasses.contains(CompileNode.class));
        assertTrue(traversedClasses.contains(BasicLink.class));
        assertTrue(traversedClasses.contains(ActionNode.class));
        assertTrue(traversedClasses.contains(BasicLink.class));
        assertTrue(traversedClasses.contains(ReactiveTypeDetectionNode.class));
    }

    @Test
    public void shouldRemoveTypeDetectionWithNoModification() {
        // given
        final ColumnMetadata column = mock(ColumnMetadata.class);
        when(column.getId()).thenReturn("0002");
        final RowMetadata rowMetadata = mock(RowMetadata.class);
        when(rowMetadata.getColumns()).thenReturn(Collections.singletonList(column));
        final Predicate<String> filter = "0001"::equals;
        final StatisticsAdapter adapter = mock(StatisticsAdapter.class);
        final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer = c -> NullAnalyzer.INSTANCE;
        final ReactiveTypeDetectionNode typeDetectionNode =
                new ReactiveTypeDetectionNode(rowMetadata, filter, adapter, analyzer);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final RunnableAction runnableAction = mock(RunnableAction.class);
        final Node node = source() //
                .to(new ActionNode(runnableAction, actionContext)) //
                .to(typeDetectionNode) //
                .to(typeDetectionNode.copyShallow()) //
                .build();

        // when
        final Optimizer optimizer = new Optimizer();
        node.accept(optimizer);
        final Pipeline optimized = optimizer.getOptimized();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        optimized.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        assertEquals(8, traversedClasses.size());
        assertTrue(traversedClasses.contains(Pipeline.class));
        assertTrue(traversedClasses.contains(SourceNode.class));
        assertTrue(traversedClasses.contains(BasicLink.class));
        assertTrue(traversedClasses.contains(ActionNode.class));
        assertTrue(traversedClasses.contains(ReactiveTypeDetectionNode.class));
    }

    private static class ApplyToColumnTestNode extends BasicNode implements ApplyToColumn {

        @Override
        public List<String> getColumnNames() {
            return Collections.emptyList();
        }
    }
}