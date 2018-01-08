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

import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.talend.ServiceBaseTest;

@TestPropertySource(properties = "dataprep.io.watch=true")
public class CloseableResourceWatchTest extends ServiceBaseTest {

    @Autowired
    CloseableResourceWatch watcher;

    @Autowired
    CloseableInputStreamComponent component;

    @Test
    public void shouldWrapInput() throws Exception {
        assertCloseable(() -> component.getInput(), true);
    }

    @Test
    public void shouldWrapOutput() throws Exception {
        assertCloseable(() -> component.getOutput(), true);
    }

    @Test
    public void shouldNotWrapNull() throws Exception {
        assertCloseable(() -> component.getNull(), false);
    }

    @Test
    public void shouldNotWrapException() throws Exception {
        assertCloseable(() -> component.getException(), false);
    }

    @Test
    public void shouldNotWrapUnknown() throws Exception {
        assertCloseable(() -> component.getUnknownCloseable(), false);
    }

    @Test
    public void shouldWrapResourceInput() throws Exception {
        assertCloseable(() -> {
            try {
                return component.getDeletableResource().getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, true);
    }

    @Test
    public void shouldWrapResourceOutput() throws Exception {
        assertCloseable(() -> {
            try {
                return component.getDeletableResource().getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, true);
    }

    public void assertCloseable(Supplier<Closeable> closeableSupplier, boolean shouldWrap) throws Exception {
        // Given
        final int previousWatchers = watcher.getEntries().size();

        // When
        Closeable closeable = null;
        try {
            closeable = closeableSupplier.get();
        } catch (Exception e) {
            // Got an exception but continue tests anyway
        }

        // Then
        final Closeable closeableForLookup = closeable;
        if (shouldWrap) {
            assertEquals(previousWatchers + 1, watcher.getEntries().size());
            assertTrue(watcher.getEntries().stream().anyMatch(handler -> handler.getCloseable() == closeableForLookup));
        } else {
            assertEquals(previousWatchers, watcher.getEntries().size());
            assertFalse(watcher.getEntries().stream().anyMatch(handler -> handler.getCloseable() == closeableForLookup));
        }
        watcher.log();

        // When
        if (closeable != null) {
            closeable.close();
        }

        // Then
        assertEquals(previousWatchers, watcher.getEntries().size());
        assertFalse(watcher.getEntries().stream().anyMatch(handler -> handler.getCloseable() == closeableForLookup));
    }


}
