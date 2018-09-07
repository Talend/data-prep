package org.talend.dataprep.transformation.pipeline.node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.talend.dataprep.transformation.pipeline.node.ReservoirNode.convertToReservoir;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.RuntimeNode;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;

public class ReservoirNodeTest {

    @Test
    public void shouldRetainAndReplayRows() {
        // given
        final Node targetNode = mock(Node.class);
        when(targetNode.copyShallow()).thenReturn(targetNode);
        final RuntimeNode targetRuntimeNode = mock(RuntimeNode.class);
        when(targetNode.exec()).thenReturn(targetRuntimeNode);

        final Node node = NodeBuilder
                .source() //
                .to(convertToReservoir(new BasicNode())) //
                .to(targetNode) //
                .build();

        final DataSetRow row = new DataSetRow(Collections.emptyMap());

        // when
        node.exec().receive(row, row.getRowMetadata());

        // then
        verify(targetRuntimeNode, never()).receive(any(DataSetRow.class), any(RowMetadata.class));
        verify(targetRuntimeNode, never()).signal(any());

        // when
        node.exec().signal(Signal.END_OF_STREAM);

        // then
        verify(targetRuntimeNode, times(1)).receive(any(DataSetRow.class), any(RowMetadata.class));
        verify(targetRuntimeNode, times(1)).signal(eq(Signal.END_OF_STREAM));
    }

    @Test
    public void shouldCallWrappedColumnNames() {
        // given
        final Node node = mock(Node.class, withSettings().extraInterfaces(ApplyToColumn.class, RuntimeNode.class));
        when(node.copyShallow()).thenReturn(node);
        when(((ApplyToColumn) node).getColumnNames()).thenReturn(Collections.singletonList("my-column"));
        final ReservoirNode reservoir = (ReservoirNode) convertToReservoir(node);

        // when
        final List<String> columnNames = reservoir.getColumnNames();

        // then
        assertEquals(1, columnNames.size());
        assertEquals("my-column", columnNames.get(0));
    }

    @Test
    public void shouldNotCallWrappedColumnNames() {
        // given
        final Node node = mock(Node.class);
        final ReservoirNode reservoir = (ReservoirNode) convertToReservoir(node);

        // when
        final List<String> columnNames = reservoir.getColumnNames();

        // then
        assertEquals(0, columnNames.size());
    }
}