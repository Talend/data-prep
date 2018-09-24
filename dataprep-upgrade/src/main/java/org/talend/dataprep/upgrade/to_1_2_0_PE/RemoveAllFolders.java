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

package org.talend.dataprep.upgrade.to_1_2_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.USER;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * As folders no longer store datasets but preparations, they need to be all removed but the home one.
 */
@Component
public class RemoveAllFolders implements BaseUpgradeTaskTo_1_2_0_PE {

    /** This class' logger. */
    private static final Logger LOG = getLogger(RemoveAllFolders.class);

    @Autowired
    private FolderRepository folderRepository;

    /**
     * @see UpgradeTask#getOrder()
     */
    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public target getTarget() {
        return USER;
    }

    /**
     * @see UpgradeTask#run()
     */
    @Override
    public void run() {
        folderRepository.clear();
        LOG.debug("All folders were removed but the home one.");
    }

}
