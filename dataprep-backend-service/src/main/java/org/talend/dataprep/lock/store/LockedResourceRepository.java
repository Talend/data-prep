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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

/**
 * Base interface of user locked-resources repositories (mongodb, file-system).
 *
 * A user can lock multiple resources at the same time, whereas a resource can only be locked by a unique user. After a
 * lock on a resource is released by a user, another one can lock it.
 *
 * This repository keeps track of user locked-resources that could be any valid {@link Identifiable} object e.g.
 * {@link Preparation}, {@link DataSetMetadata} but
 *
 */
public interface LockedResourceRepository {

    /**
     * Tries to lock the specified resource for the specified user. If no other user has the lock, it is locked and
     * added into this repository. If the resource could not be locked (because it is locked by another user), the
     * locked resource is returned. This locked resource could be used to retrieve the user currently locking the
     * resource.
     * 
     * @param resource the specified identifiable object
     * @param userInfo the specified information about the user who is requesting the lock of the resource
     * @return the locked resource which gives information about the user locking the resource
     */
    LockedResource tryLock(Identifiable resource, LockUserInfo userInfo);

    /**
     * Tries to unlock the specified resource. If no other user has the lock or it is locked by the specified user, then
     * it is unlocked and removed from the repository. Otherwise, (it is locked by another user) the locked resource
     * object is returned
     *
     * @param resource the specified identifiable object
     * @param userInfo the specified information about the user who is requesting the lock of the resource
     * @return either null if the resource has been unlocked or the locked resource which gives information about the
     * user locking the resource otherwise
     */
    LockedResource tryUnlock(Identifiable resource,  LockUserInfo userInfo);

    /**
     * Returns the {@link Identifiable} with the specified identifier and of the specified class.
     * 
     * @param resource the resource
     * @return the {@link Identifiable} with the specified identifier and of the specified class
     */
    LockedResource get(Identifiable resource);

    /**
     * Returns the collection of locked-resources currently managed by the repository.
     * 
     * @return the collection of resources of the specified class managed by the repository
     */
    Collection<LockedResource> listAll();

    /**
     * Returns the collection of resources locked by the user with the specified identifier.
     *
     * @param userId the specified user identifier
     * @return the collection of resources locked by the user with the specified identifier
     */
    Collection<LockedResource> listByUser(String userId);

    /**
     * Clears the locks and their associated resources
     */
    void clear();

    /**
     * Removes locks
     * 
     * @param resource the resource to remove from this repository
     */
    void remove(Identifiable resource);

    /**
     * Returns true if the specified locked resource is owned by the specified user and false otherwise.
     * 
     * @param lockedResource the locked resource object
     * @param userId the user identifier
     * @return true if the specified locked resource is owned by the specified user and false otherwise
     */
    boolean lockOwned(LockedResource lockedResource, String userId);

    /**
     * Returns true if the specified locked resource is released and false otherwise.
     * 
     * @param lockedResource the locked resource object
     * @return true if the specified locked resource is released and false otherwise
     */
    boolean lockReleased(LockedResource lockedResource);

    /**
     * Checks whether or not the provided arguments are legal.
     *
     * @param resource the specified identifiable object
     * @param userInfo the specified user who is requesting the lock of the resource
     */
    default void checkArguments(Identifiable resource, LockUserInfo userInfo) {
        if (resource == null) {
            throw new IllegalArgumentException("A null resource cannot be locked/unlocked");
        }
        if (userInfo == null || StringUtils.isEmpty(userInfo.getId())) {
            throw new IllegalArgumentException("A null user-identifier cannot lock/unlock a resource...");
        }
    }

}
