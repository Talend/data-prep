//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.configuration.HazelcastSetup;

@Component
@ConditionalOnMissingBean(HazelcastSetup.class)
public class NoOpLockFactory implements LockFactory {

    @Override
    public DistributedLock getLock(String id) {
        return new NoOpDistributedLock(id);
    }

    private static class NoOpDistributedLock implements DistributedLock {

        private final String id;

        public NoOpDistributedLock(String id) {
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

        @Override
        public boolean tryLock() {
            return true;
        }
    }
}
