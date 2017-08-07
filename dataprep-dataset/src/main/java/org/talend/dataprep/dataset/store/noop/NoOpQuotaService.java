// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.store.noop;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.store.QuotaService;

/**
 * [Personal & Enterprise Edition] Does nothing.
 */
@Component
@Primary
public class NoOpQuotaService implements QuotaService {

    @Override
    public void checkIfAddingSizeExceedsAvailableStorage(long size) {
        // Do nothing
    }
}
