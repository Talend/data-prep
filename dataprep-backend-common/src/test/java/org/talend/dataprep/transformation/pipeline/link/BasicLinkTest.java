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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.TestNode;

public class BasicLinkTest {
    @Test
    public void should_emit_single_input_row() {
        // given
        final TestNode target = new TestNode();
        final BasicLink link = new BasicLink(target);

        final DataSetRow row = new DataSetRow(new HashMap<>());
        final RowMetadata metadata = new RowMetadata(new ArrayList<>());

        // when
        link.emit(row, metadata);

        // then
        assertThat(target.getReceivedRows(), hasSize(1));
        assertThat(target.getReceivedRows(), contains(row));

        assertThat(target.getReceivedMetadata(), hasSize(1));
        assertThat(target.getReceivedMetadata(), contains(metadata));
    }

    @Test
    public void should_emit_multi_input_row() {
        // given
        final TestNode target = new TestNode();
        final BasicLink link = new BasicLink(target);

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = new RowMetadata(new ArrayList<>());
        final RowMetadata metadata2 = new RowMetadata(new ArrayList<>());

        final DataSetRow[] rows = new DataSetRow[] { row1, row2 };
        final RowMetadata[] metadatas = new RowMetadata[] { metadata1, metadata2 };

        // when
        link.emit(rows, metadatas);

        // then
        assertThat(target.getReceivedRows(), hasSize(2));
        assertThat(target.getReceivedRows(), contains(rows));

        assertThat(target.getReceivedMetadata(), hasSize(2));
        assertThat(target.getReceivedMetadata(), contains(metadatas));
    }
}
