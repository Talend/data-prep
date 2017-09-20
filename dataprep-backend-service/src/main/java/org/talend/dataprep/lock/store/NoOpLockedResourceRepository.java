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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

/**
 * No op implementation of the LockedResourceRepository.
 */
@Component
@ConditionalOnProperty(name = "lock.preparation.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockedResourceRepository implements LockedResourceRepository {

    @Override
    public LockedResource tryLock(Preparation resource, LockUserInfo userInfo) {
        notNull(resource, "A null resource cannot be locked/unlocked");
        notEmpty(userInfo.getId(), "A null user-identifier cannot lock/unlock a resource...");
        return new LockedResource(resource.getId(), userInfo, 0);
    }

    @Override
    public LockedResource tryUnlock(Preparation resource, LockUserInfo userInfo) {
        return null;
    }

    @Override
    public boolean isLockOwned(Preparation lockedResource, String userId) {
        return true;
    }

    public boolean isLockReleased(Preparation lockedResource) {
        return true;
    }
}
