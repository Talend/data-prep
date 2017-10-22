// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
        when(delegate.getLock(eq("1234"))).thenReturn(mock);
        final DistributedLockWatcher watcher = new DistributedLockWatcher(delegate);
        final DistributedLock lock = watcher.getLock("1234");

        // when
        lock.lock();

        // then
        verify(mock, times(1)).lock();
    }


    @Test
    public void shouldReleaseOnClose() throws Exception {
        // given
        final LockFactory delegate = mock(LockFactory.class);
        final DistributedLock mock1 = mock(DistributedLock.class);
        final DistributedLock mock2 = mock(DistributedLock.class);
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
    }
}
