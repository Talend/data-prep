//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class containing a group of reentrantReadWriteLock.
 */
public class ReentrantReadWriteLockGroup {

    /**
     * The fairness policy of the reentrantReadWriteLock that will be generated
     */
    private final boolean fairness;

    /**
     * The threshold used to launch a cleanup when it is reached
     */
    private final int CLEANUP_THRESHOLD;

    /**
     * The internal counter used to compare with threshold
     */
    private int counter = 0;

    /**
     * A map of reentrant lock associating to each string a reentrant lock.
     */
    private ConcurrentHashMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public ReentrantReadWriteLockGroup() {
        this.fairness = false;
        this.CLEANUP_THRESHOLD = 100;
    }

    public ReentrantReadWriteLockGroup(int counter) {
        this.fairness = false;
        this.CLEANUP_THRESHOLD = counter;
    }

    public ReentrantReadWriteLockGroup(boolean fairness) {
        this.fairness = fairness;
        this.CLEANUP_THRESHOLD = 100;
    }

    public ReentrantReadWriteLockGroup(boolean fairness, int counter) {
        this.fairness = fairness;
        this.CLEANUP_THRESHOLD = counter;
    }

    /**
     * Returns the ReentrantReadWriteLock associated with the specified string <tt>id</tt>.
     * 
     * @param id the specified string
     * @return the ReentrantReadWriteLock associated with the specified string <tt>id</tt>
     */
    public ReentrantReadWriteLock getLock(String id) {
        ReentrantReadWriteLock lock = locks.get(id);
        if (lock == null) {
            locks.putIfAbsent(id, new ReentrantReadWriteLock(fairness));
        }
        lock = locks.get(id);

        if (CLEANUP_THRESHOLD <= ++counter) {
            cleanUp(lock);
            counter = 0;
        }
        return lock;
    }

    /**
     * Removes locks from the map if their no thread asking from the lock. If a lock is held by any thread (including
     * the current one) it is not removed.
     * 
     * @param doNotRemove the specified lock that must not be removed from the map
     */
    private void cleanUp(ReentrantReadWriteLock doNotRemove) {
        synchronized (locks) {
            ReentrantReadWriteLock lock = null;
            for (Iterator<Map.Entry<String, ReentrantReadWriteLock>> iterator = locks.entrySet().iterator(); iterator
                    .hasNext();) {
                lock = iterator.next().getValue();
                if (lock != doNotRemove && !lockHeldOrAsked(lock)) {
                    iterator.remove();
                }

            }
        }
    }

    /**
     * Returns <tt>true</tt> if the specified lock is held by a thread or is asked (some threads are blocking and
     * waiting to acquire the lock) and <tt>otherwise</tt>.
     * 
     * @param lock the specified lock
     * @return <tt>true</tt> if the specified lock is held by a thread or is asked (some threads are blocking and
     * waiting to acquire the lock) and <tt>otherwise</tt>.
     */
    private boolean lockHeldOrAsked(ReentrantReadWriteLock lock) {
        if (lock.hasQueuedThreads() || lock.getReadLockCount() > 0 || lock.isWriteLocked()) {
            return true;
        }
        return false;
    }

}
