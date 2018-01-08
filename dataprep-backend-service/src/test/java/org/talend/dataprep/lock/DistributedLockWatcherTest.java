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

package org.talend.dataprep.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

public class DistributedLockWatcherTest {

    @Test
    public void shouldWatchOnLock() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock = mock(DistributedLock.class);
        when(mock.getKey()).thenReturn("1234");
        when(delegate.getLock(eq("1234"))).thenReturn(mock);
        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);

        // when
        final DistributedLock lock = watcher.getLock("1234");

        // then
        assertEquals(DistributedLockWatcher.WatchedDistributedLock.class, lock.getClass());
    }

    @Test
    public void shouldLock() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock = mock(DistributedLock.class);
        when(mock.getKey()).thenReturn("1234");
        when(delegate.getLock(eq("1234"))).thenReturn(mock);
        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);
        final DistributedLock lock = watcher.getLock("1234");

        // when
        lock.lock();

        // then
        verify(mock, times(1)).lock();
        assertEquals(1, watcher.getLocks().size());
    }

    @Test
    public void shouldRemoveWatchOnUnlock() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock = mock(DistributedLock.class);
        when(mock.getKey()).thenReturn("1234");
        when(delegate.getLock(eq("1234"))).thenReturn(mock);
        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);
        final DistributedLock lock = watcher.getLock("1234");

        // when
        lock.lock();
        lock.unlock();

        // then
        verify(mock, times(1)).lock();
        verify(mock, times(1)).unlock();
        assertEquals(0, watcher.getLocks().size());
    }

    @Test
    public void shouldRemoveWatchOnUnlockError() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock = mock(DistributedLock.class);
        doAnswer(invocation -> {
            throw new RuntimeException("Unpurpose unchecked exception");
        }).when(mock).unlock();
        when(mock.getKey()).thenReturn("1234");
        when(delegate.getLock(eq("1234"))).thenReturn(mock);
        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);
        final DistributedLock lock = watcher.getLock("1234");

        // when
        lock.lock();
        try {
            lock.unlock();
            fail("Expected an unlock failure.");
        } catch (Exception e) {
            // Expected.
        }

        // then
        verify(mock, times(1)).lock();
        verify(mock, times(1)).unlock();
        assertEquals(0, watcher.getLocks().size());
    }


    @Test
    public void shouldReleaseOnClose() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock1 = mock(DistributedLock.class);
        when(mock1.getKey()).thenReturn("1234");
        final DistributedLock mock2 = mock(DistributedLock.class);
        when(mock2.getKey()).thenReturn("5678");
        when(delegate.getLock(eq("1234"))).thenReturn(mock1);
        when(delegate.getLock(eq("5678"))).thenReturn(mock2);
        doAnswer(invocation -> {
            throw new RuntimeException(); // on purpose unchecked exception
        }).when(mock2).unlock();

        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);
        watcher.getLock("1234");
        watcher.getLock("5678");

        // when
        watcher.onApplicationEvent(new ContextClosedEvent(new AnnotationConfigApplicationContext()));

        // then
        verify(mock1, times(1)).unlock();
        verify(mock2, times(1)).unlock();
        assertEquals(0, watcher.getLocks().size());
    }
}
