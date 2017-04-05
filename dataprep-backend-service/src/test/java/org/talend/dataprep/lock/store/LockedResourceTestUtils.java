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

import java.util.Random;
import java.util.UUID;

import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

public abstract class LockedResourceTestUtils extends ServiceBaseTest {

    protected Identifiable getFirstResourceType(String id) {
        return new Resource(id);
    }

    protected Identifiable getSecondResourceType(String id) {
        return new SecondResource(id);
    }

    protected class Resource extends Identifiable {

        Resource(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }

    protected class SecondResource extends Resource {

        SecondResource(String id) {
            super(id);
        }
    }

    protected LockUserInfo randomLockUserInfo() {
        final Random random = new Random();
        final String userId = UUID.randomUUID().toString();
        final String displayName = "display name for " + random.nextInt(100) + 1;

        return new LockUserInfo(userId, displayName);
    }
}
