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

import java.util.concurrent.locks.Lock;

/**
 * Basic distributed Lock implementation for locking. This implementation relies on Hazelcast but it was created to
 * avoid Hazelcast dependencies in all the Talend classes. Use LockFactory.getLock(String) to get a new instance.
 *
 * @see LockFactory#getLock(String)
 */
public interface DistributedLock {

    /**
     * @see Lock#lock().
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

}
