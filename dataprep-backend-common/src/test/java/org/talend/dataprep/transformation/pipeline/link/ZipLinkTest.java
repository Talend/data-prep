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
import static org.talend.dataprep.transformation.pipeline.Signal.END_OF_STREAM;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.TestNode;
import org.talend.dataprep.transformation.pipeline.node.ZipLink;

public class ZipLinkTest {
    @Test
    public void should_emit_single_input_row_when_all_source_has_emitted_one() {
        // given
        final TestNode source1 = new TestNode();
        final TestNode source2 = new TestNode();
        final TestNode target = new TestNode();
        ZipLink.zip(new Node[]{ source1, source2 }, target);

        final DataSetRow row = new DataSetRow(new HashMap<>());
        final RowMetadata metadata = new RowMetadata(new ArrayList<>());

        // when
        source1.receive(row, metadata);

        // then
        assertThat(target.getReceivedRows(), hasSize(0));
        assertThat(target.getReceivedMetadata(), hasSize(0));

        // when
        source2.receive(row, metadata);

        // then
        assertThat(target.getReceivedRows(), hasSize(2));
        assertThat(target.getReceivedMetadata(), hasSize(2));
    }

    @Test
    public void should_emit_signal_when_all_source_has_emitted_one() {
        // given
        final TestNode source1 = new TestNode();
        final TestNode source2 = new TestNode();
        final TestNode target = new TestNode();
        ZipLink.zip(new Node[]{ source1, source2 }, target);

        // when
        source1.signal(END_OF_STREAM);

        // then
        assertThat(target.getReceivedSignals(), hasSize(0));

        // when
        source2.signal(END_OF_STREAM);

        // then
        assertThat(target.getReceivedSignals(), hasSize(1));
        assertThat(target.getReceivedSignals(), contains(END_OF_STREAM));
    }
}
