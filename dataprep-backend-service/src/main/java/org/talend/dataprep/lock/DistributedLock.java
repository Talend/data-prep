//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.lock;

import java.util.concurrent.locks.Lock;

/**
 * Basic distributed Lock implementation for locking. This implementation relies on Hazelcast but it was created to
 * avoid Hazelcast dependencies in all the Talend classes. Use LockFactory.getLock(String) to get a new instance.
 *
 * @see LockFactory#getLock(String)
 */
public interface DistributedLock {

    /**
     * Acquires the lock.
     * 
     * If the lock is not available then the current thread becomes disabled for thread scheduling purposes and lies
     * dormant until the lock has been acquired.
     * 
     * Implementation Considerations
     * 
     * A Lock implementation may be able to detect erroneous use of the lock, such as an invocation that would cause
     * deadlock, and may throw an (unchecked) exception in such circumstances. The circumstances and the exception type
     * must be documented by that Lock implementation.
     * 
     * Specified by: lock() in Lock
     */
    void lock();

    /**
     * Releases the lock.
     */
    void unlock();

    /**
     * Getter for key used for the lock.
     * 
     * @return the key used for the lock
     */
    String getKey();

    /**
     *
     * Acquires the lock only if it is free at the time of invocation.
     * @return {@code true} if the lock was acquired and {@code false} otherwise.
     * @see Lock#tryLock()
     */
    boolean tryLock();

}
