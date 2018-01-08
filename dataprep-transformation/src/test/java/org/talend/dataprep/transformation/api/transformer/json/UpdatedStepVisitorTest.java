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

package org.talend.dataprep.transformation.api.transformer.json;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;
import org.talend.dataprep.transformation.service.StepMetadataRepository;

@RunWith(MockitoJUnitRunner.class)
public class UpdatedStepVisitorTest {

    private final RowMetadata metadata = new RowMetadata(singletonList(column().type(Type.STRING).name("original").build()));

    private ActionNode entryNode;

    private ActionContext actionContext;

    @Mock
    StepMetadataRepository stepMetadataRepository;

    @Before
    public void setUp() throws Exception {
        actionContext = new ActionContext(new TransformationContext());
        entryNode = new ActionNode(new RunnableAction((row, context) -> row), actionContext);
    }

    @Test
    public void testUpdatedStepsWithOk() throws Exception {
        // Given
        final Step step = new Step(ROOT_STEP.id(), new PreparationActions().id(), "0.0");
        final RowMetadata stepRowMetadata = new RowMetadata();
        final Node stepNode = NodeBuilder.from(new StepNode(step, stepRowMetadata, entryNode, new BasicNode())).to(new BasicNode()).build();
        final UpdatedStepVisitor visitor = new UpdatedStepVisitor(stepMetadataRepository);
        actionContext.setActionStatus(ActionContext.ActionStatus.OK); // OK action!

        // When
        stepNode.exec().receive(new DataSetRow(metadata), metadata);

        // Then
        stepNode.accept(visitor);
        verify(stepMetadataRepository).update(step.id(), stepRowMetadata);
    }

    @Test
    public void testUpdatedStepsWithKO() throws Exception {
        // Given
        final Step step = new Step(ROOT_STEP.id(), new PreparationActions().id(), "0.0");
        final RowMetadata stepRowMetadata = new RowMetadata();
        final Node stepNode = NodeBuilder.from(new StepNode(step, stepRowMetadata, entryNode, new BasicNode())).to(new BasicNode()).build();
        final UpdatedStepVisitor visitor = new UpdatedStepVisitor(stepMetadataRepository);
        actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED); // Canceled action!

        // When
        stepNode.exec().receive(new DataSetRow(metadata), metadata);

        // Then
        stepNode.accept(visitor);
        verify(stepMetadataRepository).update(step.id(), stepRowMetadata);
    }
}
