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
import org.talend.dataprep.api.preparation.BasicUserLock;
import org.talend.dataprep.api.preparation.Preparation;

public class NoOpLockedResourceRepositoryTest {

    private final NoOpLockedResourceRepository repository = new NoOpLockedResourceRepository();

    @Test
    public void should_lock_resource_lock() {
        BasicUserLock owner = randomLockUserInfo();
        BasicUserLock preEmpter = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource.getId(), owner.getId(), owner.getDisplayName());
        LockedResource lockedByPreempter = repository.tryLock(resource.getId(), preEmpter.getId(), preEmpter.getDisplayName());

        assertNotNull(lockedResource);
        assertTrue(repository.isLockOwned(resource, owner.getId()));
        assertTrue(repository.isLockOwned(resource, preEmpter.getId()));
        assertNotEquals(lockedByPreempter, lockedResource);
    }

    @Test
    public void should_always_unlock() {
        BasicUserLock owner = randomLockUserInfo();
        BasicUserLock preEmpter = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource.getId(), owner.getId(), owner.getDisplayName());
        LockedResource lockedByPreEmpter = repository.tryUnlock(resource, preEmpter);

        assertNotNull(lockedResource);
        assertNull(lockedByPreEmpter);
        assertTrue(repository.isLockReleased(resource));
    }

    @Test
    public void should_unlock_unlocked_resource() {
        BasicUserLock owner = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        final LockedResource mustBeNull = repository.tryUnlock(resource, owner);

        assertNull(mustBeNull);
    }

    @Test
    public void lock_should_be_reentrant() {
        BasicUserLock owner = randomLockUserInfo();
        Preparation resource = getFirstResourceType("1");

        LockedResource lockedResource = repository.tryLock(resource.getId(), owner.getId(), owner.getDisplayName());
        LockedResource lockedResource2 = repository.tryLock(resource.getId(), owner.getId(), owner.getDisplayName());

        assertNotNull(lockedResource);
        assertNotNull(lockedResource2);
        assertEquals(lockedResource.getUserId(), lockedResource2.getUserId());
        assertEquals(lockedResource.getResourceId(), lockedResource2.getResourceId());
        assertTrue(lockedResource.getExpirationTime().isBefore(lockedResource2.getExpirationTime())
                || lockedResource.getExpirationTime().equals(lockedResource2.getExpirationTime()));
    }

}
