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

package org.talend.dataprep.transformation.pipeline;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

public class StepNodeTransformerTest {

    private static final Step STEP = new Step(null, null, "");

    private static final Step INVALID_STEP = new Step(null, null, "");

    private static final Step ROOT = new Step(null, null, "");

    @Before
    public void setUp() throws Exception {
        STEP.setRowMetadata("rowMetadata");
        INVALID_STEP.setRowMetadata(null);
    }

    @Test
    public void shouldNotCreateStepNode() throws Exception {
        // given
        Node node = NodeBuilder.from(new TestNode()).build();

        // when
        final Node processed = StepNodeTransformer.transform(node, emptyList(), s -> null);

        // then
        assertEquals(SourceNode.class, processed.getClass());
        assertEquals(TestNode.class, processed.getLink().getTarget().getClass());
    }

    @Test
    public void shouldCreateStepNode() throws Exception {
        // given
        Node node = NodeBuilder //
                .from(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .build();

        // when
        final Node processed = StepNodeTransformer.transform(node, asList(ROOT, STEP), s -> null);

        // then
        final Class[] expectedClasses = { SourceNode.class, StepNode.class };
        final NodeClassVisitor visitor = new NodeClassVisitor();
        processed.accept(visitor);
        assertThat(visitor.traversedClasses, hasItems(expectedClasses));

    }

    @Test
    public void shouldCreateStepNodeWhenSurrounded() throws Exception {
        // given
        Node node = NodeBuilder //
                .from(new TestNode()) //
                .to(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .to(new BasicNode()) //
                .build();

        // when
        final Node processed = StepNodeTransformer.transform(node, asList(ROOT, STEP), s -> null);

        // then
        final Class[] expectedClasses = { SourceNode.class, TestNode.class, StepNode.class, BasicNode.class };
        final NodeClassVisitor visitor = new NodeClassVisitor();
        processed.accept(visitor);
        assertThat(visitor.traversedClasses, hasItems(expectedClasses));
    }

    @Test
    public void shouldCreateStepNodesWhenSurrounded() throws Exception {
        // given
        Node node = NodeBuilder //
                .from(new TestNode()) //
                .to(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .to(new BasicNode()) //
                .to(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .build();

        // when
        final Node processed = StepNodeTransformer.transform(node, asList(ROOT, STEP, STEP), s -> null);

        // then
        final AtomicInteger stepNodeCount = new AtomicInteger();
        processed.accept(new Visitor() {

            @Override
            public void visitStepNode(StepNode stepNode) {
                stepNodeCount.incrementAndGet();
                super.visitStepNode(stepNode);
            }
        });
        assertEquals(2, stepNodeCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailCreateStepNode() throws Exception {
        // given
        Node node = NodeBuilder //
                .from(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .build();

        // then
        StepNodeTransformer.transform(node, emptyList(), s -> null);
    }

    @Test
    public void shouldCreateStepNodeWithTooManySteps() throws Exception {
        // given
        Node node = NodeBuilder //
                .from(new CompileNode(null, null)) //
                .to(new ActionNode(null, null)) //
                .build();

        // when
        final Node processed = StepNodeTransformer.transform(node, asList(ROOT, STEP, STEP), s -> null);

        // then
        final Class[] expectedClasses = { SourceNode.class, StepNode.class };
        final NodeClassVisitor visitor = new NodeClassVisitor();
        processed.accept(visitor);
        assertThat(visitor.traversedClasses, hasItems(expectedClasses));
    }

}
