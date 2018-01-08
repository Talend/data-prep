// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "lock.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockFactory implements LockFactory {

    @Override
    public DistributedLock getLock(String id) {
        return new NoOpDistributedLock(id);
    }

    private static class NoOpDistributedLock implements DistributedLock {

        private final String id;

        private NoOpDistributedLock(String id) {
            this.id = id;
        }

        @Override
        public void lock() {
            // No op: this class is used when no lock is needed.
        }

        @Override
        public void unlock() {
            // No op: this class is used when no lock is needed.
        }

        @Override
        public String getKey() {
            return id;
        }

    }
}
