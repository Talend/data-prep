/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

    private String id;

    private String displayName;

    private Instant expirationTime;

    /**
     *  State of the lock. 0 mean no lock, above mean the user has reentered the lock.
     *  Name comes from {@link java.util.concurrent.locks.ReentrantLock#getHoldCount } field.
     */
    private int holdCount;

    public BasicUserLock(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
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
}
