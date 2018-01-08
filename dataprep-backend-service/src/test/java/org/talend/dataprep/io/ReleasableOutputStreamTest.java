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

import org.apache.commons.io.output.NullOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ReleasableOutputStreamTest {

    private final AtomicBoolean wasCalled = new AtomicBoolean();

    private ReleasableOutputStream releasableOutputStream;

    private ReleasableOutputStream failedReleasableOutputStream;

    @Before
    public void setUp() throws Exception {
        releasableOutputStream = new ReleasableOutputStream(new NullOutputStream(), () -> wasCalled.set(true));
        failedReleasableOutputStream = new ReleasableOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Oops");
            }

            @Override
            public void flush() throws IOException {
                throw new IOException("Oops");
            }
        }, () -> wasCalled.set(true));
    }

    @After
    public void tearDown() throws Exception {
        releasableOutputStream.close();
        wasCalled.set(false);
    }

    @Test
    public void write() throws Exception {
        // When
        releasableOutputStream.write('a');

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedWrite() throws Exception {
        // When
        try {
            failedReleasableOutputStream.write('a');
        } catch (IOException e) {
            // Ignored
        }

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void write1() throws Exception {
        // When
        releasableOutputStream.write(new byte[] {'a', 'b'});

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedWrite1() throws Exception {
        // When
        try {
            failedReleasableOutputStream.write(new byte[] {'a', 'b'});
        } catch (IOException e) {
            // Ignored
        }

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void write2() throws Exception {
        // When
        releasableOutputStream.write(new byte[] {'a', 'b'}, 0, 2);

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedWrite2() throws Exception {
        // When
        try {
            failedReleasableOutputStream.write(new byte[] {'a', 'b'}, 0, 2);
        } catch (IOException e) {
            // Ignored
        }

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void flush() throws Exception {
        // When
        releasableOutputStream.flush();

        // Then
        assertFalse(wasCalled.get());
    }

    @Test
    public void failedFlush() throws Exception {
        // When
        failedReleasableOutputStream.flush();

        // Then
        assertTrue(wasCalled.get());
    }

    @Test
    public void close() throws Exception {
        // When
        assertFalse(wasCalled.get());
        releasableOutputStream.close();

        // Then
        assertTrue(wasCalled.get());
    }

}
