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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * With 1.2.0-PE, datasets are not stored within folders anymore, they need to be renamed with their folder path :
 * &lt;dataset name&gt; - &lt;folder structure&gt;
 */
@Component
public class RenameDataSetsWithFolderPath implements BaseUpgradeTaskTo_1_2_0_PE {

    /** This class' logger. */
    private static final Logger LOG = getLogger(RenameDataSetsWithFolderPath.class);

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    private FolderRepository folderRepository;

    /**
     * @see UpgradeTask#getOrder()
     */
    @Override
    public int getOrder() {
        return 0;
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

        final Stream<DataSetMetadata> datasets = dataSetMetadataRepository.list();
        datasets.forEach(dataset -> {
            final Folder folder = folderRepository.locateEntry(dataset.getId(), FolderContentType.DATASET);
            // skip home folder
            if (folder != null && !StringUtils.equals("/", folder.getPath())) {
                String newName = dataset.getName() + " - " + StringUtils.strip(folder.getPath(), "/");
                dataset.setName(newName);
                dataSetMetadataRepository.save(dataset);
                LOG.debug("dataset #{} renamed to {}", dataset.getId(), newName);
            } else {
                LOG.debug("dataset #{} not renamed since it's in the user home folder", dataset.getId());
            }
        });
    }

}
