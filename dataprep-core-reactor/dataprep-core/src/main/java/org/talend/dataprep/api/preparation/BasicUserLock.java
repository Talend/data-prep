/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.api.preparation;

import java.time.Instant;

/**
 * Basic user info to duplicate user data in mongo. For now it serve to store locking user in {@link Preparation}.
 */
public class BasicUserLock {

    private String userId;

    private String userDisplayName;

    private Instant expirationTime;

    /**
     *  State of the lock. 0 mean no lock, above mean the user has reentered the lock.
     *  Name comes from {@link java.util.concurrent.locks.ReentrantLock#getHoldCount } field.
     */
    private int holdCount;

    // For Jackson/Mongo de/serialization
    public BasicUserLock() {
    }

    /**
     * Init a new lock to store in Preparation with a 0 hold count.
     *
     * @param userId ID of the user locking
     * @param userDisplayName a end-user displayable name of the locking user
     * @param expirationTime the date this lock will no longer be valid
     */
    public BasicUserLock(String userId, String userDisplayName, Instant expirationTime) {
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.expirationTime = expirationTime;
        this.holdCount = 0;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getHoldCount() {
        return holdCount;
    }

    public void setHoldCount(int holdCount) {
        this.holdCount = holdCount;
    }

    public int decrementHoldCount() {
        return --holdCount;
    }

    public int incrementHoldCount() {
        return ++holdCount;
    }
}
