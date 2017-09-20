// ============================================================================
//
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

package org.talend.dataprep.lock.store;

import java.time.Instant;
import java.util.Objects;

import org.talend.dataprep.api.preparation.BasicUserLock;

/**
 * Represents a user locked-Resource. It keeps track of the identifier of the locked resource, the user locking it and
 * the expiration time of the lock. The lock will be released when the expiration time is reached.
 */
public class LockedResource {

    /** The identifier of the user who is locking the resource. */
    private String userId;

    /** The display name of the user who is locking the resource. */
    private String userDisplayName;

    /** The identifier of the locked resource. */
    private String resourceId;

    /** . The time when the lock will be released. */
    private Instant expirationTime;

    /**
     * Constructs a locked resource with the specified lock delay.
     *
     * @param resourceId the specified resource identifier.
     * @param userInfo the specified user info.
     * @param delay the specified lock delay.
     */
    public LockedResource(String resourceId, BasicUserLock userInfo, long delay) {
        this.resourceId = resourceId;
        this.userId = userInfo.getId();
        this.userDisplayName = userInfo.getDisplayName();
        this.expirationTime = Instant.now().plusSeconds(delay);
    }

    /**
     * NO argument constructor for Jackson
     */
    public LockedResource() {
        // needed for json de/serialization
    }

    /**
     *
     * @return the identifier of the user locking the resource
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @return the identifier of the locked resource
     */
    public String getResourceId() {
        return resourceId;
    }

    public Instant getExpirationTime() {
        return expirationTime;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LockedResource that = (LockedResource) o;
        return Objects.equals(userId, that.userId) && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(expirationTime, that.expirationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, resourceId, expirationTime);
    }

    @Override
    public String toString() {
        return "LockedResource{" +
                "userId='" + userId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", expirationTime=" + expirationTime +
                '}';
    }

}
