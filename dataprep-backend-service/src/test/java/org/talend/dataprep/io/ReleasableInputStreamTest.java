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

package org.talend.dataprep.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.input.NullInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReleasableInputStreamTest {

    private final AtomicBoolean wasCalled = new AtomicBoolean();

    private ReleasableInputStream releasableInputStream;

    private ReleasableInputStream failedReleasableInputStream;

    @Before
    public void setUp() throws Exception {
        releasableInputStream = new ReleasableInputStream(new NullInputStream(2048), () -> wasCalled.set(true));
        failedReleasableInputStream = new ReleasableInputStream(new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("Oops");
            }

            @Override
            public int available() throws IOException {
                throw new IOException("Oops");
            }

            @Override
            public synchronized void mark(int readlimit) {
                throw new RuntimeException("Oops");
            }
        }, () -> wasCalled.set(true));
    }

    @After
    public void tearDown() throws Exception {
        releasableInputStream.close();
        wasCalled.set(false);
    }

    @Test
    public void read() throws Exception {
        // When
        releasableInputStream.read();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedRead() throws Exception {
        // When
        failedReleasableInputStream.read();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void read1() throws Exception {
        // When
        releasableInputStream.read(new byte[1024]);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedRead1() throws Exception {
        // When
        failedReleasableInputStream.read(new byte[1024]);

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void read2() throws Exception {
        // When
        releasableInputStream.read(new byte[1024], 0, 1024);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedRead2() throws Exception {
        // When
        failedReleasableInputStream.read(new byte[1024], 0, 1024);

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void skip() throws Exception {
        // When
        releasableInputStream.skip(100);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedSkip() throws Exception {
        // When
        failedReleasableInputStream.skip(100);

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void available() throws Exception {
        // When
        releasableInputStream.available();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedAvailable() throws Exception {
        // When
        failedReleasableInputStream.available();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void close() throws Exception {
        // When
        releasableInputStream.close();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void mark() throws Exception {
        // When
        releasableInputStream.mark(0);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = RuntimeException.class)
    public void failedMark() throws Exception {
        // When
        failedReleasableInputStream.mark(0);

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void reset() throws Exception {
        // When
        releasableInputStream.mark(10);
        releasableInputStream.reset();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test(expected = IOException.class)
    public void failedReset() throws Exception {
        // When
        failedReleasableInputStream.reset();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void markSupported() throws Exception {
        // When
        releasableInputStream.markSupported();

        // Then
        assertFalse(wasCalled.get());
    }
}
