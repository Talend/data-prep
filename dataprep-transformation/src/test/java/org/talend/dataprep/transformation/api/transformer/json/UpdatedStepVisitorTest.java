// ============================================================================
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

package org.talend.dataprep.transformation.api.transformer.json;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
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

public class UpdatedStepVisitorTest {

    private final RowMetadata metadata = new RowMetadata(singletonList(column().type(Type.STRING).name("original").build()));

    private ActionNode entryNode;

    private ActionContext actionContext;

    @Before
    public void setUp() throws Exception {
        actionContext = new ActionContext(new TransformationContext());
        entryNode = new ActionNode(new RunnableAction((row, context) -> row), actionContext);
    }

    @Test
    public void testUpdatedStepsWithOk() throws Exception {
        // Given
        final Step step = new Step(Step.ROOT_STEP, new PreparationActions(), "0.0");
        final Node stepNode = NodeBuilder.from(new StepNode(step, entryNode, new BasicNode())).to(new BasicNode()).build();
        final UpdatedStepVisitor visitor = new UpdatedStepVisitor();
        actionContext.setActionStatus(ActionContext.ActionStatus.OK); // OK action!

        // When
        stepNode.exec().receive(new DataSetRow(metadata), metadata);

        // Then
        stepNode.accept(visitor);
        final List<Step> updatedSteps = visitor.getUpdatedSteps();
        assertEquals(1, updatedSteps.size());
        assertEquals(metadata, updatedSteps.get(0).getRowMetadata());
    }

    @Test
    public void testUpdatedStepsWithKO() throws Exception {
        // Given
        final Step step = new Step(Step.ROOT_STEP, new PreparationActions(), "0.0");
        final Node stepNode = NodeBuilder.from(new StepNode(step, entryNode, new BasicNode())).to(new BasicNode()).build();
        final UpdatedStepVisitor visitor = new UpdatedStepVisitor();
        actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED); // Canceled action!

        // When
        stepNode.exec().receive(new DataSetRow(metadata), metadata);

        // Then
        stepNode.accept(visitor);
        final List<Step> updatedSteps = visitor.getUpdatedSteps();
        assertEquals(1, updatedSteps.size());
        assertNull(updatedSteps.get(0).getRowMetadata());
    }
}
