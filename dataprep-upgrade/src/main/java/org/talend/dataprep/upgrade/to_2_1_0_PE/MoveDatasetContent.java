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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MoveDatasetContent implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(MoveDatasetContent.class);

    protected String oldRoot;

    private String tmp;

    protected String newRoot;

    @Value("${content-service.store.local.path}")
    public void setOldRoot(String basePath) {
        this.oldRoot = StringUtils.endsWith(basePath, "/") ? basePath + "store/datasets/content/"
                : basePath + "/store/datasets/content/";
    }

    @Value("${content-service.store.local.path}")
    public void setTmp(String basePath) {
        this.tmp = StringUtils.endsWith(basePath, "/") ? basePath + "store/datasets/tmp"
                : basePath + "/store/datasets/tmp";
    }

    @Value("${content-service.store.local.path}")
    public void setNewRoot(String basePath) {
        this.newRoot = StringUtils.endsWith(basePath, "/") ? basePath + "store/datasets/content/dataset/"
                : basePath + "/store/datasets/content/dataset/";
    }

    @Override
    public void run() {
        if (this.haveToBeApply()) {
            LOGGER.info("Moving dataset content from from {} to {}.", oldRoot, newRoot);
            try {
                FileUtils.moveDirectory(new File(oldRoot), new File(tmp));
                FileUtils.moveDirectory(new File(tmp), new File(newRoot));
                LOGGER.info("Dataset content moved from {} to {}.", oldRoot, newRoot);
            } catch (IOException e) {
                LOGGER.error("Unable to move Dataset content from {} to {}.", oldRoot, newRoot);
            }
        } else {
            LOGGER.info("Dataset folder is already compatible with this new version. We don't need to upgrade it");
        }
    }

    /**
     * Check if we need to apply this upgrade service. We need to apply it if the newRoot folder do not exist already
     *
     * @return true if we need to apply this upgrade service, false otherwise
     */
    public boolean haveToBeApply() {
        File newRootFolder = new File(newRoot);
        return !newRootFolder.exists();
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public target getTarget() {
        return VERSION;
    }

}
