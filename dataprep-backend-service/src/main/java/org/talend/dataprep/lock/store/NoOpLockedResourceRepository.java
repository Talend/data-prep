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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.BasicUserLock;
import org.talend.dataprep.api.preparation.Preparation;

/**
 * No op implementation of the LockedResourceRepository.
 */
@Component
@ConditionalOnProperty(name = "lock.preparation.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockedResourceRepository implements LockedResourceRepository {

    @Override
    public void tryLock(String preparationId, String userId, String displayName) {
    }

    @Override
    public LockedResource tryUnlock(Preparation resource, BasicUserLock userInfo) {
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
