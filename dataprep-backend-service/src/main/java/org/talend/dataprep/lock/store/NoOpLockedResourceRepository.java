// ============================================================================
//
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

package org.talend.dataprep.lock.store;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * No op implementation of the LockedResourceRepository.
 */
@Component
@ConditionalOnProperty(name = "lock.preparation.store", havingValue = "none", matchIfMissing = true)
public class NoOpLockedResourceRepository implements LockedResourceRepository {

    private static final Logger LOGGER = getLogger(NoOpLockedResourceRepository.class);

    @Autowired
    private PreparationRepository preparationRepository;

    public NoOpLockedResourceRepository() {
        LOGGER.info("Preparation lock engine: none");
    }

    @Override
    public Preparation tryLock(String preparationId, String userId, String displayName) {
        Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        if (preparation == null) {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", preparationId));
        }
        return preparation;
    }

    @Override
    public void unlock(String preparationId, String userId) {}

}
