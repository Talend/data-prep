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

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.exception.TDPException;

/**
 * Base interface of user locked-resources repositories (mongodb, file-system).
 *
 * A user can lock multiple resources at the same time, whereas a resource can only be locked by a unique user. After a
 * lock on a resource is released by a user, another one can lock it.
 *
 * This repository keeps track of user locked-resources that could be any valid {@link Identifiable} object e.g.
 * {@link Preparation}, {@link DataSetMetadata} but
 *
 */
public interface LockedResourceRepository {

    /**
     * Tries to lock the preparation. If already locked, increments the lock.
     *
     * @throws TDPException PREPARATION_DOES_NOT_EXIST if the preparation does not exists.
     */
    Preparation tryLock(String preparationId, String userId, String displayName);

    /**
     * Unlock the specified preparation. It can only throw an exception if the preparation is held by another user.
     */
    void unlock(String preparationId, String userId);

}
