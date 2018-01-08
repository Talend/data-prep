// ============================================================================
//
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

package org.talend.dataprep.dataset.store.content;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StrictlyBoundedInputStreamTest {

    @Test
    public void should_be_read_when_maximum_size_is_less_or_equal_to_zero() throws Exception {
        // Given
        byte[] bytes = { 0, 1, 2 };
        InputStream inputStream = new StrictlyBoundedInputStream(new ByteArrayInputStream(bytes), -1);
        byte[] result = new byte[3];

        // When
        inputStream.read(result, 0, 3);

        // Then
        assertArrayEquals(bytes, result);
    }

    @Test
    public void should_read_exactly_the_maximum_size() throws Exception {
        // Given
        byte[] bytes = { 0, 1, 2 };
        StrictlyBoundedInputStream inputStream = new StrictlyBoundedInputStream(new ByteArrayInputStream(bytes), 3);
        byte[] result = new byte[3];

        // When
        inputStream.read(result, 0, 3);

        assertArrayEquals(bytes, result);
        assertTrue(inputStream.getMaxSize() == inputStream.getTotal());
    }

    @Test(expected = StrictlyBoundedInputStream.InputStreamTooLargeException.class)
    public void should_read_throw_an_exception() throws Exception {
        // Given
        byte[] bytes = { 0, 1, 2 };
        StrictlyBoundedInputStream inputStream = new StrictlyBoundedInputStream(new ByteArrayInputStream(bytes), 2);

        byte[] result = new byte[3];
        inputStream.read(result, 0, 3);

        assertArrayEquals(bytes, result);
        assertTrue(2 == inputStream.getTotal());

    }

    @Test
    public void exceptionShouldHaveMaxValue() throws IOException {
        // Given
        byte[] bytes = { 0, 1, 2 };
        StrictlyBoundedInputStream inputStream = new StrictlyBoundedInputStream(new ByteArrayInputStream(bytes), 2);

        // when
        try {
            byte[] result = new byte[3];
            inputStream.read(result, 0, 3);
        } catch (StrictlyBoundedInputStream.InputStreamTooLargeException e) {

            // then
            assertEquals(2, e.getMaxSize());
        }

    }
}
