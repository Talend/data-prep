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
import java.util.Collections;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

/**
 * No op implementation of the LockedResourceRepository.
 */
@Component
@ConditionalOnProperty(name = "lock.preparation.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockedResourceRepository implements LockedResourceRepository {

    /**
     * @see LockedResourceRepository#tryLock(Identifiable, LockUserInfo)
     */
    @Override
    public LockedResource tryLock(Identifiable resource, LockUserInfo userInfo) {
        checkArguments(resource, userInfo);
        return new LockedResource(resource.getId(), userInfo, 0);
    }

    /**
     * @see LockedResourceRepository#tryUnlock(Identifiable, LockUserInfo)
     */
    @Override
    public LockedResource tryUnlock(Identifiable resource, LockUserInfo userInfo) {
        return null;
    }

    /**
     * @see LockedResourceRepository#get(Identifiable)
     */
    @Override
    public LockedResource get(Identifiable resource) {
        return null;
    }

    /**
     * @see LockedResourceRepository#listAll()
     */
    @Override
    public Collection<LockedResource> listAll() {
        return Collections.emptyList();
    }

    /**
     * @see LockedResourceRepository#listByUser(String)
     */
    @Override
    public Collection<LockedResource> listByUser(String userId) {
        return Collections.emptyList();
    }

    /**
     * @see LockedResourceRepository#clear()
     */
    @Override
    public void clear() {
        // Does nothing
    }

    /**
     * @see LockedResourceRepository#remove(Identifiable)
     */
    @Override
    public void remove(Identifiable resource) {
        // Does nothing
    }

    /**
     * @see LockedResourceRepository#lockOwned(LockedResource, String)
     */
    @Override
    public boolean lockOwned(LockedResource lockedResource, String userId) {
        return true;
    }

    /**
     * @see LockedResourceRepository#lockReleased(LockedResource)
     */
    @Override
    public boolean lockReleased(LockedResource lockedResource) {
        return true;
    }
}
