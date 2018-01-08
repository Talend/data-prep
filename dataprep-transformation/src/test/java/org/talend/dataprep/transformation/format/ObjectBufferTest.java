/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.transformation.format;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.row.DataSetRow;

public class ObjectBufferTest {

    @Test
    public void testAdd() throws Exception {
        // Given
        ObjectBuffer<ObjectToBuffer> buffer = new ObjectBuffer<>(ObjectToBuffer.class);
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "Something");
        final DataSetRow dataSetRow = new DataSetRow(values);
        dataSetRow.setTdpId(1L);

        final ObjectToBuffer row = new ObjectToBuffer(dataSetRow);

        // When
        buffer.appendRow(row);

        // Then
        Optional<ObjectToBuffer> first = buffer.readAll().findFirst();
        assertTrue(first.isPresent());
        final String[] actualValues = first.get().getValues();
        final Collection<String> expectedValues = values.values();
        assertEquals(expectedValues.size(), actualValues.length);
        for (String value : actualValues) {
            assertTrue(expectedValues.contains(value));
        }

    }

    @Test
    public void testCloseCleanUp() throws Exception {
        // Given
        ObjectBuffer<ObjectToBuffer> buffer = new ObjectBuffer<>(ObjectToBuffer.class);

        Path tempFile = (Path) ReflectionTestUtils.getField(buffer, "tempFile");

        // When
        assertTrue(Files.exists(tempFile));
        buffer.close();

        // Then
        assertFalse(Files.exists(tempFile));
    }

    private static class ObjectToBuffer {

        private String[] values;

        public ObjectToBuffer() {
        }

        public ObjectToBuffer(DataSetRow dataSetRow) {
            values = dataSetRow.toArray();
        }

        public String[] getValues() {
            return values;
        }
    }
}
