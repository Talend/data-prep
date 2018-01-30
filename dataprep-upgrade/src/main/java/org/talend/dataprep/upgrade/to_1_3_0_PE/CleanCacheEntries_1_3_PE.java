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

package org.talend.dataprep.upgrade.to_1_3_0_PE;

import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.upgrade.common.CacheCleaner;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * Since invalid values management changed in 1.3, all cache entries should be removed.
 */
@Component
public class CleanCacheEntries_1_3_PE implements BaseUpgradeTaskTo_1_3_0_PE {

    @Autowired
    private CacheCleaner cacheCleaner;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }

    /**
     * @see UpgradeTask#run()
     */
    @Override
    public void run() {
        cacheCleaner.cleanCache();
    }

}
