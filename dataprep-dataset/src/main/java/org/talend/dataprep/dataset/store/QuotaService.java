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

package org.talend.dataprep.dataset.store;

/**
 * Checks and notifies changes on a tenant's available storage.
 */
public interface QuotaService {

    /**
     * Checks if a newly created data set can fit inside the tenant's available storage.
     *
     * @param size In bytes, size of the newly created data set
     * @throws org.talend.dataprep.exception.TDPException if size exceeds available storage
     */
    void checkIfAddingSizeExceedsAvailableStorage(long size);

    /**
     * @return the available space.
     */
    long getAvailableSpace();
}
