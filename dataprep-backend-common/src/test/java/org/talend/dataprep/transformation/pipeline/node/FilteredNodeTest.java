package org.talend.dataprep.transformation.pipeline.node;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.TestLink;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.*;

public class FilteredNodeTest {
    @Test
    public void receive_should_filter_with_simple_predicate() throws Exception {
        // given
        final RowMetadata metadata0 =  new RowMetadata();
        final DataSetRow row0 =  new DataSetRow(new HashMap<>());
        row0.setTdpId(0L); // does not pass the predicate

        final RowMetadata metadata1 =  new RowMetadata();
        final DataSetRow row1 =  new DataSetRow(new HashMap<>());
        row1.setTdpId(1L); // pass the predicate

        final TestLink link = new TestLink(new BasicNode());

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 1));
        node.setLink(link);

        // when
        node.receive(row0, metadata0);
        node.receive(row1, metadata1);

        // then
        assertThat(link.getEmittedRows(), hasSize(1));
        assertThat(link.getEmittedRows(), contains(row1));
        assertThat(link.getEmittedMetadata(), hasSize(1));
        assertThat(link.getEmittedMetadata(), contains(metadata1));
    }

    @Test
    public void signal_should_receive_lastRowMetadata() throws Exception {
        // given
        final RowMetadata metadata0 =  new RowMetadata();
        final DataSetRow row0 =  new DataSetRow(new HashMap<>());
        row0.setTdpId(0L); // does not pass the predicate

        final RowMetadata metadata1 =  new RowMetadata();
        final DataSetRow row1 =  new DataSetRow(new HashMap<>());
        row1.setTdpId(1L); // pass the predicate

        final TestLink link = new TestLink(new BasicNode());

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 2));
        node.setLink(link);

        node.receive(row0, metadata0);
        node.receive(row1, metadata1);

        // when
        node.signal(Signal.END_OF_STREAM);

        // then
        assertThat(link.getEmittedRows(), hasSize(1));
        assertTrue(link.getEmittedRows().get(0).isDeleted());
        assertThat(link.getEmittedRows(), contains(row1));
        assertThat(link.getEmittedMetadata(), hasSize(1));
        assertThat(link.getEmittedMetadata(), contains(metadata1));
    }
}
