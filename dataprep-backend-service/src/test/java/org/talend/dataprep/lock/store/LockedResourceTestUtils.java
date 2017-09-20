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

import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.lock.store.LockedResource.LockUserInfo;

public class LockedResourceTestUtils {

    public static Preparation getFirstResourceType(String id) {
        return new Preparation(id, "1.2.3");
    }

    public static Preparation getSecondResourceType(String id) {
        return getFirstResourceType(id);
    }

    public static LockUserInfo randomLockUserInfo() {
        final Random random = new Random();
        final String userId = UUID.randomUUID().toString();
        final String displayName = "display name for " + random.nextInt(100) + 1;

        return new LockUserInfo(userId, displayName);
    }
}
