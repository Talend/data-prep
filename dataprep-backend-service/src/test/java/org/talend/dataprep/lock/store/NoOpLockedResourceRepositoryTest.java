// ============================================================================
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

import static org.junit.Assert.*;
import static org.talend.dataprep.lock.store.LockedResourceTestUtils.getFirstResourceType;
import static org.talend.dataprep.lock.store.LockedResourceTestUtils.randomLockUserInfo;

import org.junit.Test;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

public class NoOpLockedResourceRepositoryTest {

    private final NoOpLockedResourceRepository repository = new NoOpLockedResourceRepository();

    @Test
    public void should_lock_resource_lock() {
        LockUserInfo owner = randomLockUserInfo();
        LockUserInfo preEmpter = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, owner);
        LockedResource lockedByPreempter = repository.tryLock(resource, preEmpter);

        assertNotNull(lockedResource);
        assertTrue(repository.isLockOwned(resource, owner.getId()));
        assertTrue(repository.isLockOwned(resource, preEmpter.getId()));
        assertNotEquals(lockedByPreempter, lockedResource);
    }

    @Test
    public void should_always_unlock() {
        LockUserInfo owner = randomLockUserInfo();
        LockUserInfo preEmpter = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, owner);
        LockedResource lockedByPreEmpter = repository.tryUnlock(resource, preEmpter);

        assertNotNull(lockedResource);
        assertNull(lockedByPreEmpter);
        assertTrue(repository.isLockReleased(resource));
    }

    @Test
    public void should_unlock_unlocked_resource() {
        LockUserInfo owner = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        final LockedResource mustBeNull = repository.tryUnlock(resource, owner);

        assertNull(mustBeNull);
    }

    @Test
    public void lock_should_be_reentrant() {
        LockUserInfo owner = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource, owner);
        LockedResource lockedResource2 = repository.tryLock(resource, owner);

        assertNotNull(lockedResource);
        assertNotNull(lockedResource2);
        assertEquals(lockedResource.getUserId(), lockedResource2.getUserId());
        assertEquals(lockedResource.getResourceId(), lockedResource2.getResourceId());
        assertTrue(lockedResource.getExpirationTime().isBefore(lockedResource2.getExpirationTime())
                || lockedResource.getExpirationTime().equals(lockedResource2.getExpirationTime()));
    }

}
