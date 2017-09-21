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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * No op implementation of the LockedResourceRepository.
 */
@Component
@ConditionalOnProperty(name = "lock.preparation.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockedResourceRepository implements LockedResourceRepository {

    @Autowired
    private PreparationRepository preparationRepository;

    @Override
    public Preparation tryLock(String preparationId, String userId, String displayName) {
        return preparationRepository.get(preparationId, Preparation.class);
    }

    @Override
    public void unlock(String preparationId, String userId) {}

}
