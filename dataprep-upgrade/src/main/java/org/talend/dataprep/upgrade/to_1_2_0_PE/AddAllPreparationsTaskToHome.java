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

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * Add all preparations to the home folder.
 */
@Component
public class AddAllPreparationsTaskToHome implements BaseUpgradeTaskTo_1_2_0_PE {

    /** This class' logger. */
    private static final Logger LOG = getLogger(AddAllPreparationsTaskToHome.class);

    /** The preparation repository. */
    @Autowired
    private PreparationRepository preparationRepository;

    /** The folder repository. */
    @Autowired
    private FolderRepository folderRepository;

    /**
     * @see UpgradeTask#getOrder()
     */
    @Override
    public int getOrder() {
        return 3;
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
        final Stream<Preparation> preparations = preparationRepository.list(Preparation.class);
        final String homeId = folderRepository.getOrCreateHome().getId();

        preparations.forEach(p -> {
            folderRepository.addFolderEntry(new FolderEntry(FolderContentType.PREPARATION, p.id()), homeId);
            LOG.debug("preparation #{} added to home", p.getId());
        });

        LOG.debug("All preparations were added to the home folder.");
    }

}
