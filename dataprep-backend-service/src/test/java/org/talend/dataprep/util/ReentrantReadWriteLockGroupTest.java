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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;

public class ReentrantReadWriteLockGroupTest {

    @Test
    public void ensure_that_consecutive_calls_on_same_string_return_the_same_lock() {
        // given
        ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 3);

        // when
        ReentrantReadWriteLock lock1 = locks.getLock("1");
        ReentrantReadWriteLock lock2 = locks.getLock("1");
        ReentrantReadWriteLock lock3 = locks.getLock("1");

        // then
        assertEquals(lock1, lock2);
        assertEquals(lock1, lock3);
    }

    @Test
    public void ensure_that_calls_with_different_strings_returns_different_locks() {
        // given
        ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 3);

        // when
        ReentrantReadWriteLock lock1 = locks.getLock("1");
        ReentrantReadWriteLock lock2 = locks.getLock("2");
        ReentrantReadWriteLock lock3 = locks.getLock("3");

        // then
        assertNotEquals(lock1, lock2);
        assertNotEquals(lock1, lock3);
        assertNotEquals(lock2, lock3);
    }

    @Test
    public void ensure_that_a_lock_will_not_be_removed_if_it_is_the_latest_returned_one() {
        // given
        ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 1);

        // when
        ReentrantReadWriteLock lock1 = locks.getLock("1");
        ReentrantReadWriteLock lock2 = locks.getLock("1");

        // then
        assertEquals(lock1, lock2);
    }

    @Test
    public void ensure_that_two_different_locks_are_returned_when_threshold_is_reached() {
        // given
        ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 1);

        // when
        ReentrantReadWriteLock firstLockForOneValue = locks.getLock("1");
        ReentrantReadWriteLock firstLockForTwoValue = locks.getLock("2");
        ReentrantReadWriteLock secondLockForOneValue = locks.getLock("1");
        ReentrantReadWriteLock secondLockForTwoValue = locks.getLock("2");

        // then
        assertNotEquals(firstLockForOneValue, secondLockForOneValue);
        assertNotEquals(firstLockForTwoValue, secondLockForTwoValue);
    }

    @Test
    public void ensure_that_two_different_lock_are_returned_when_threshold_is_reached() {
        // given
        ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 1);

        // when
        ReentrantReadWriteLock firstLockForOneValue = locks.getLock("1");
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                firstLockForOneValue.writeLock().lock();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ReentrantReadWriteLock firstLockForTwoValue = locks.getLock("2");
        ReentrantReadWriteLock secondLockForOneValue = locks.getLock("1");

        // then
        assertEquals(firstLockForOneValue, secondLockForOneValue);
    }
}