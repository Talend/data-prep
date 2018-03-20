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

package org.talend.dataprep.transformation.pipeline.builder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.util.function.Predicate;

import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.TestLink;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;
import org.talend.dataprep.transformation.pipeline.node.FilteredSourceNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;
import org.talend.dataprep.transformation.pipeline.node.ZipLink;

public class NodeBuilderTest {
    @Test
    public void default_first_node_should_be_SourceNode() {
        // when
        final Node node = NodeBuilder.source().build();

        // then
        assertThat(node, instanceOf(SourceNode.class));
    }

    @Test
    public void should_take_provided_node_as_source() {
        // given
        final Node source = new BasicNode();

        // when
        final Node node = NodeBuilder.from(source).build();

        // then
        assertThat(node, is(source));
    }

    @Test
    public void should_create_filtered_node_as_source() {
        // given
        final Predicate<DataSetRow> predicate = (dataSetRow) -> true;

        // when
        final Node node = NodeBuilder.filteredSource(predicate).build();

        // then
        assertThat(node, instanceOf(FilteredSourceNode.class));
    }

    @Test
    public void should_append_node_with_basic_link() {
        // given
        final Node nextNode = new BasicNode();

        // when
        final Node node = NodeBuilder
                .source()
                .to(nextNode)
                .build();

        // then
        assertThat(node.getLink(), instanceOf(BasicLink.class));
        assertThat(node.getLink().getTarget(), is(nextNode));
    }

    @Test
    public void should_append_node_with_provided_link() {
        // given
        final Node nextNode = new BasicNode();

        // when
        final Node node = NodeBuilder
                .source()
                .to((n) -> new TestLink(n[0]), nextNode)
                .build();

        // then
        assertThat(node.getLink(), instanceOf(TestLink.class));
        assertThat(node.getLink().getTarget(), is(nextNode));
    }

    @Test
    public void should_append_a_pipeline() {
        // given
        final Node firstNode = new BasicNode();
        final Node secondNode = new BasicNode();
        final Node nodeToAppend = new BasicNode();

        final Node pipeline = NodeBuilder
                .from(firstNode)
                .to(secondNode)
                .build();

        // when
        final Node node = NodeBuilder
                .source()
                .to(pipeline)
                .to(nodeToAppend)
                .build();

        // then
        assertThat(node.getLink().getTarget(), is(firstNode));
        assertThat(node.getLink().getTarget().getLink().getTarget(), is(secondNode));
        assertThat(node.getLink().getTarget().getLink().getTarget().getLink().getTarget(), is(nodeToAppend));
    }

    @Test
    public void should_append__clone_link_then_zip_to_a_common_node() {
        // given
        final Node firstNode = new BasicNode();
        final Node branch1 = new BasicNode();
        final Node branch2 = new BasicNode();
        final Node zipTarget = new BasicNode();

        // when
        final Node node = NodeBuilder
                .from(firstNode)
                .dispatchTo(branch1, branch2)
                .zipTo(zipTarget)
                .build();

        // then
        assertThat(node, is(firstNode));
        assertThat(node.getLink(), instanceOf(CloneLink.class));
        assertThat(((CloneLink)node.getLink()).getNodes(), arrayContaining(branch1, branch2));
        assertThat(branch1.getLink(), instanceOf(ZipLink.Zipper.class));
        assertThat(branch2.getLink(), instanceOf(ZipLink.Zipper.class));
        assertThat(branch1.getLink().getTarget(), is(zipTarget));
        assertThat(branch2.getLink().getTarget(), is(zipTarget));
    }
}
