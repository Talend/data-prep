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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

/**
 * Factory used to generate locks. This class is used to generate prototype DistributedLock and hide the hazelcast
 * implementation.
 */
@Component
@ConditionalOnProperty(value = "hazelcast.enabled", havingValue = "true")
public class HazelcastLockFactory implements LockFactory {

    /** The Hazel cast instance. */
    @Autowired
    private HazelcastInstance hci;

    /**
     * @param id the if where to put the lock.
     * @return a distributed lock.
     */
    @Override
    public DistributedLock getLock(String id) {
        final ILock lock = hci.getLock(id);
        return new HazelcastDistributedLock(id, lock);
    }

}
