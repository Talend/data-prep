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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * An implementation of {@link LockFactory} that will forcibly unlock all created locks on application shutdown.
 *
 * @see org.talend.dataprep.processor.Wrapper
 */
public class DistributedLockWatcher implements LockFactory, ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockWatcher.class);

    private final Map<String, DistributedLock> locks = new HashMap<>();

    private final LockFactory delegate;

    public DistributedLockWatcher(LockFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public DistributedLock getLock(String id) {
        final DistributedLock lock = delegate.getLock(id);
        final WatchedDistributedLock watchedDistributedLock = new WatchedDistributedLock(lock);
        locks.put(watchedDistributedLock.getKey(), watchedDistributedLock);
        return watchedDistributedLock;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (!locks.isEmpty()) {
            LOGGER.info("Application is being shut down but {} locks remain, releasing them...", locks.size());
            final Collection<DistributedLock> locksToRelease = new ArrayList<>(locks.values());
            for (DistributedLock lock : locksToRelease) {
                LOGGER.info("Releasing lock '{}'", lock.getKey());
                try {
                    lock.unlock();
                } catch (Exception e) {
                    LOGGER.warn("Unable to release lock '{}' due to exception.", e);
                }
            }
            LOGGER.info("Locks released.");
        } else {
            LOGGER.info("No lock to release on shutdown.");
        }
    }

    /**
     * @return Returns an unmodifiable set of {@link DistributedLock} currently watched by this instance.
     */
    public Collection<DistributedLock> getLocks() {
        return Collections.unmodifiableCollection(locks.values());
    }

    class WatchedDistributedLock implements DistributedLock {

        private final DistributedLock lock;

        private WatchedDistributedLock(DistributedLock lock) {
            this.lock = lock;
        }

        @Override
        public void lock() {
            lock.lock();
        }

        @Override
        public void unlock() {
            try {
                lock.unlock();
            } catch (Exception e) {
                LOGGER.debug("Unable to successfully unlock lock '{}'", lock.getKey(), e);
                throw new IllegalStateException("Unable to remove lock", e);
            } finally {
                locks.remove(lock.getKey());
            }
        }

        @Override
        public String getKey() {
            return lock.getKey();
        }
    }
}
