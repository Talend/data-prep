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

package org.talend.dataprep.transformation.pipeline.link;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.talend.dataprep.transformation.pipeline.Signal.CANCEL;

import java.util.ArrayList;
import java.util.HashMap;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.TestNode;

public class CloneLinkTest {
    @Test
    public void should_emit_single_input_row_to_all_targets() {
        // given
        final TestNode target1 = new TestNode();
        final TestNode target2 = new TestNode();
        final CloneLink link = new CloneLink(target1, target2);

        final DataSetRow row = new DataSetRow(new HashMap<>());
        final RowMetadata metadata = new RowMetadata(new ArrayList<>());

        // when
        link.emit(row, metadata);

        // then
        assertThat(target1.getReceivedRows(), hasSize(1));
        assertThat(target1.getReceivedRows(), contains(row));
        assertThat(target2.getReceivedRows(), hasSize(1));
        assertThat(target2.getReceivedRows(), contains(row));

        assertThat(target1.getReceivedMetadata(), hasSize(1));
        assertThat(target1.getReceivedMetadata(), contains(metadata));
        assertThat(target2.getReceivedMetadata(), hasSize(1));
        assertThat(target2.getReceivedMetadata(), contains(metadata));
    }

    @Test
    public void should_emit_the_first_metadata_on_each_row() {
        // given
        final TestNode target1 = new TestNode();
        final TestNode target2 = new TestNode();
        final CloneLink link = new CloneLink(target1, target2);

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = getRowMetadata("0001");

        link.emit(row1, metadata1);
        assertThat(target1.getReceivedMetadata(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(0).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(0).getColumns().get(0).getId(), is("0001"));

        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata2 = getRowMetadata("0002");

        // when
        link.emit(row2, metadata2);

        // then
        assertThat(target1.getReceivedMetadata(), hasSize(2));
        assertThat(target1.getReceivedMetadata().get(1).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(1).getColumns().get(0).getId(), is("0001"));
    }


    @Test
    public void should_emit_multi_input_row_to_all_targets() {
        // given
        final TestNode target1 = new TestNode();
        final TestNode target2 = new TestNode();
        final CloneLink link = new CloneLink(target1, target2);

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = new RowMetadata(new ArrayList<>());
        final RowMetadata metadata2 = new RowMetadata(new ArrayList<>());

        final DataSetRow[] rows = new DataSetRow[] { row1, row2 };
        final RowMetadata[] metadatas = new RowMetadata[] { metadata1, metadata2 };

        // when
        link.emit(rows, metadatas);

        // then
        assertThat(target1.getReceivedRows(), hasSize(2));
        assertThat(target1.getReceivedRows(), contains(rows));
        assertThat(target2.getReceivedRows(), hasSize(2));
        assertThat(target2.getReceivedRows(), contains(rows));

        assertThat(target1.getReceivedMetadata(), hasSize(2));
        assertThat(target1.getReceivedMetadata(), contains(metadatas));
        assertThat(target2.getReceivedMetadata(), hasSize(2));
        assertThat(target2.getReceivedMetadata(), contains(metadatas));
    }

    @Test
    public void should_emit_the_first_multi_metadata_on_each_row() {
        // given
        final TestNode target1 = new TestNode();
        final TestNode target2 = new TestNode();
        final CloneLink link = new CloneLink(target1, target2);

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final DataSetRow row3 = new DataSetRow(new HashMap<>());
        final DataSetRow row4 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = getRowMetadata("0001");
        final RowMetadata metadata2 = getRowMetadata("0002");
        final RowMetadata metadata3 = getRowMetadata("0003");
        final RowMetadata metadata4 = getRowMetadata("0004");

        final DataSetRow[] rows1 = new DataSetRow[] { row1, row2 };
        final RowMetadata[] metadatas1 = new RowMetadata[] { metadata1, metadata2 };
        final DataSetRow[] rows2 = new DataSetRow[] { row3, row4 };
        final RowMetadata[] metadatas2 = new RowMetadata[] { metadata3, metadata4 };

        link.emit(rows1, metadatas1);
        assertThat(target1.getReceivedMetadata(), hasSize(2));
        assertThat(target1.getReceivedMetadata().get(0).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(0).getColumns().get(0).getId(), is("0001"));
        assertThat(target1.getReceivedMetadata().get(1).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(1).getColumns().get(0).getId(), is("0002"));

        // when
        link.emit(rows2, metadatas2);

        // then
        assertThat(target1.getReceivedMetadata(), hasSize(4));
        assertThat(target1.getReceivedMetadata().get(0).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(0).getColumns().get(0).getId(), is("0001"));
        assertThat(target1.getReceivedMetadata().get(1).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(1).getColumns().get(0).getId(), is("0002"));
        assertThat(target1.getReceivedMetadata().get(2).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(2).getColumns().get(0).getId(), is("0001"));
        assertThat(target1.getReceivedMetadata().get(3).getColumns(), hasSize(1));
        assertThat(target1.getReceivedMetadata().get(3).getColumns().get(0).getId(), is("0002"));
    }

    @Test
    public void should_emit_signal_to_all_targets() {
        // given
        final TestNode target1 = new TestNode();
        final TestNode target2 = new TestNode();
        final CloneLink link = new CloneLink(target1, target2);

        // when
        link.signal(CANCEL);

        // then
        assertThat(target1.getReceivedSignals(), hasSize(1));
        assertThat(target1.getReceivedSignals(), contains(CANCEL));
        assertThat(target2.getReceivedSignals(), hasSize(1));
        assertThat(target2.getReceivedSignals(), contains(CANCEL));
    }

    private RowMetadata getRowMetadata(final String colId) {
        final ColumnMetadata colMetadata = new ColumnMetadata();
        colMetadata.setId(colId);
        return new RowMetadata(Lists.newArrayList(colMetadata));
    }
}
