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

package org.talend.dataprep.transformation.pipeline.node;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.HashMap;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.TestLink;

public class FilterNodeTest {
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

        final FilterNode node = new FilterNode((row, metadata) -> row.getTdpId() == 1);
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
    public void receive_multi_should_filter_with_multi_predicate() throws Exception {
        // given
        final RowMetadata metadata0 =  new RowMetadata();
        final DataSetRow row0 =  new DataSetRow(new HashMap<>());
        row0.setTdpId(0L); // does not pass the predicate

        final RowMetadata metadata1 =  new RowMetadata();
        final DataSetRow row1 =  new DataSetRow(new HashMap<>());
        row1.setTdpId(1L); // pass the predicate

        final RowMetadata metadata2 =  new RowMetadata();
        final DataSetRow row2 =  new DataSetRow(new HashMap<>());
        row2.setTdpId(2L); // does not pass the predicate

        final RowMetadata metadata3 =  new RowMetadata();
        final DataSetRow row3 =  new DataSetRow(new HashMap<>());
        row3.setTdpId(3L); // pass the predicate

        final RowMetadata[] metadataGroup0 = new RowMetadata[] { metadata0, metadata1 };
        final DataSetRow[] rowGroup0 = new DataSetRow[]{ row0, row1 }; // pass the predicate

        final RowMetadata[] metadataGroup1 = new RowMetadata[] { metadata2, metadata3 };
        final DataSetRow[] rowGroup1 = new DataSetRow[]{ row2, row3 }; // des not pass the predicate

        final TestLink link = new TestLink(new BasicNode());
        final FilterNode node = new FilterNode((row, metadata) -> true, (row, metadata) -> row.getTdpId() == 1);
        node.setLink(link);

        // when
        node.receive(rowGroup0, metadataGroup0);
        node.receive(rowGroup1, metadataGroup1);

        // then
        assertThat(link.getEmittedRows(), hasSize(2));
        assertThat(link.getEmittedRows(), contains(row0, row1));
        assertThat(link.getEmittedMetadata(), hasSize(2));
        assertThat(link.getEmittedMetadata(), contains(metadata0, metadata1));
    }

}
