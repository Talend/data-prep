package org.talend.dataprep.transformation.pipeline.node;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.TestLink;

public class FilteredNodeTest {

    private List<RowMetadata> metadata;

    private List<DataSetRow> rows;

    private TestLink link;

    @Before
    public void setUp() {
        metadata = new ArrayList<>();
        rows = new ArrayList<>();
        link = new TestLink(new BasicNode());
    }

    private void initRowsAndMetadata(long... tdpIds) {
        for (long tdpId : tdpIds) {
            metadata.add(new RowMetadata());
            DataSetRow row = new DataSetRow(new HashMap<>());
            row.setTdpId(tdpId);
            rows.add(row);
        }
    }

    @Test
    public void receive_should_filter_with_simple_predicate() throws Exception {
        // given
        initRowsAndMetadata(0L, 1L); //0L does not pass the predicate, whereas 1L does

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 1));
        node.setLink(link);

        // when
        node.receive(rows.get(0), metadata.get(0));
        node.receive(rows.get(1), metadata.get(1));

        // then
        assertThat(link.getEmittedRows(), hasSize(1));
        assertThat(link.getEmittedRows(), contains(rows.get(1)));
        assertThat(link.getEmittedMetadata(), hasSize(1));
        assertThat(link.getEmittedMetadata(), contains(metadata.get(1)));
    }

    @Test
    public void receive_should_count_total_sample_rows() throws Exception {
        // given
        initRowsAndMetadata(0L, 1L); //0L does not pass the predicate, whereas 1L does

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 1));
        node.setLink(link);

        // when
        node.receive(rows.get(0), metadata.get(0));
        node.receive(rows.get(1), metadata.get(1));

        // then
        assertThat(link.getEmittedMetadata().get(0).getSampleNbRows(), equalTo(2L));
    }

    @Test
    public void should_count_total_sample_rows_even_when_no_row_matches_filter() throws Exception {
        // given
        initRowsAndMetadata(0L, 1L); // None of them pass the predicate

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 2));
        node.setLink(link);

        // when
        node.receive(rows.get(0), metadata.get(0));
        node.receive(rows.get(1), metadata.get(1));
        node.signal(Signal.END_OF_STREAM);

        // then
        assertThat(link.getEmittedMetadata().get(0).getSampleNbRows(), equalTo(2L));
    }

    @Test
    public void signal_should_receive_last_rowMetadata() throws Exception {
        // given
        initRowsAndMetadata(0L, 1L); // None of them pass the predicate
        final DataSetRow lastRow = rows.get(1);
        final RowMetadata lastMetadata = metadata.get(1);

        final FilteredNode node = new FilteredNode((rowMetadata -> (DataSetRow row) -> row.getTdpId() == 2));
        node.setLink(link);

        node.receive(rows.get(0), metadata.get(0));
        node.receive(lastRow, lastMetadata);

        // when
        node.signal(Signal.END_OF_STREAM);

        // then
        assertThat(link.getEmittedRows(), hasSize(1));
        assertThat(link.getEmittedRows(), contains(lastRow));
        assertTrue(link.getEmittedRows().get(0).isDeleted());
        assertThat(link.getEmittedMetadata(), hasSize(1));
        assertThat(link.getEmittedMetadata(), contains(lastMetadata));
    }
}
