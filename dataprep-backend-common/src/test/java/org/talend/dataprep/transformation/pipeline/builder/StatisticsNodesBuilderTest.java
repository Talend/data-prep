package org.talend.dataprep.transformation.pipeline.builder;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CHANGE_NAME;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_DELETE_COLUMNS;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.NEED_STATISTICS_INVALID;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.VALUES_COLUMN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.NodeClassVisitor;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.InvalidDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.ReactiveTypeDetectionNode;
import org.talend.dataprep.transformation.pipeline.node.StatisticsNode;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsNodesBuilderTest {

    private StatisticsNodesBuilder builder;

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private AnalyzerService analyzerService;

    @Mock
    private StatisticsAdapter statisticsAdapter;

    private List<ColumnMetadata> columns;

    @Before
    public void setUp() throws Exception {
        columns = new ArrayList<>();
        columns.add(ColumnMetadata.Builder.column().computedId("0001").type(Type.STRING).build());
        columns.add(ColumnMetadata.Builder.column().computedId("0002").type(Type.STRING).build());

        builder = StatisticsNodesBuilder
                .builder() //
                .actionRegistry(actionRegistry) //
                .analyzerService(analyzerService) //
                .statisticsAdapter(statisticsAdapter)
                .columns(columns);
    }

    @Test
    public void shouldNotBuildPostStatisticsIfOnlyMetadataActions() {
        // given
        final RunnableAction action1 = configureAction("metadata-column-rename", METADATA_CHANGE_NAME);
        final RunnableAction action2 = configureAction("metadata-column-delete", METADATA_DELETE_COLUMNS);
        builder.actions(asList(action1, action2));

        // when
        final Node node = builder.buildPostStatistics();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        node.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        final List<Class> expected = singletonList(BasicNode.class);
        assertEquals(expected, traversedClasses);
    }

    @Test
    public void shouldBuildPostStatisticsIfNotOnlyMetadataActions() {
        // given
        final RunnableAction action1 = configureAction("metadata-column-rename", METADATA_CHANGE_NAME);
        final RunnableAction action2 = configureAction("values-modification", VALUES_COLUMN);
        builder.actions(asList(action1, action2));

        // when
        final Node node = builder.buildPostStatistics();

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        node.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        final List<Class> expected = asList(ReactiveTypeDetectionNode.class, //
                BasicLink.class, //
                InvalidDetectionNode.class, //
                BasicLink.class, //
                StatisticsNode.class);
        assertEquals(expected, traversedClasses);
    }

    @Test
    public void shouldBuildIntermediateStatisticsOnNeededColumns() throws InterruptedException {
        // given
        final RunnableAction action1 = configureAction("modify-column", VALUES_COLUMN);
        final RunnableAction action2 = configureAction("need-invalid", NEED_STATISTICS_INVALID);
        when(action1.getParameters())
                .thenReturn(Collections.singletonMap(ImplicitParameters.COLUMN_ID.getKey(), "0001"));
        when(action2.getParameters())
                .thenReturn(Collections.singletonMap(ImplicitParameters.COLUMN_ID.getKey(), "0001"));
        builder.actions(asList(action1, action2));

        // when
        final Node node = builder.buildIntermediateStatistics(action2);

        // then
        final NodeClassVisitor nodeClassVisitor = new NodeClassVisitor();
        node.accept(nodeClassVisitor);
        final List<Class> traversedClasses = nodeClassVisitor.getTraversedClasses();
        final List<Class> expected = asList(ReactiveTypeDetectionNode.class, //
                BasicLink.class, InvalidDetectionNode.class);
        assertEquals(expected, traversedClasses);

        // then
        final RowMetadata metadata = new RowMetadata(columns);
        node.exec().receive(new DataSetRow(metadata), metadata);
        node.exec().signal(Signal.END_OF_STREAM);
        node.accept(new Visitor() {

            @Override
            public void visitInvalidDetection(InvalidDetectionNode invalidDetectionNode) {
                super.visitInvalidDetection(invalidDetectionNode);
                final List<String> columnsForInvalidDetection = invalidDetectionNode.getColumnNames();
                assertNotNull(columnsForInvalidDetection);
                assertEquals(1, columnsForInvalidDetection.size());
                assertEquals("0001", columnsForInvalidDetection.iterator().next());
            }
        });

    }

    private RunnableAction configureAction(String name, ActionDefinition.Behavior behavior) {
        final RunnableAction action = mock(RunnableAction.class);
        when(action.getName()).thenReturn(name);
        final ActionDefinition actionDefinition = mock(ActionDefinition.class);
        when(actionDefinition.adapt(any(ScopeCategory.class))).thenReturn(actionDefinition);
        when(actionDefinition.adapt(any(ColumnMetadata.class))).thenReturn(actionDefinition);
        when(actionDefinition.getBehavior(any())).thenReturn(Collections.singleton(behavior));
        when(actionRegistry.get(eq(name))).thenReturn(actionDefinition);
        return action;
    }
}