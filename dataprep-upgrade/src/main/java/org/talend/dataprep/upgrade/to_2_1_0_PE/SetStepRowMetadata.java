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

import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.service.TransformationService;
import org.talend.dataprep.upgrade.model.UpgradeTask;

/**
 * Set the RowMetadata for every step.
 */
@Component
public class SetStepRowMetadata implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetStepRowMetadata.class);

    /** The preparation repository. */
    @Autowired
    private PreparationRepository repository;

    /** The transformation service. */
    @Autowired
    private TransformationService service;

    @Autowired
    private FolderRepository folderRepository;

    @Override
    public void run() {

        final AtomicLong current = new AtomicLong(0);
        final long count = repository.list(PersistentPreparation.class).count();
        LOGGER.info("Starting a potentially long migration task, there are {} preparations to process", count);
        repository.list(PersistentPreparation.class).forEach(p -> setStepRowMetadata(p, current, count));
        LOGGER.info("Finished setting up row metadata in preparation steps");
    }

    /**
     * Update the given preparation's steps with row metadata.
     *
     * @param preparation the preparation.
     * @param currentProcessingNumber the number of the current preparation.
     * @param total the total number of preparation.
     */
    private void setStepRowMetadata(PersistentPreparation preparation, AtomicLong currentProcessingNumber, long total) {
        LOGGER.info("[{}/{}] preparation #{} migration starting...", currentProcessingNumber.addAndGet(1), total,
                preparation.getId());

        // Check if preparation is accessible to end user
        final Folder folder = folderRepository.locateEntry(preparation.id(), PREPARATION);
        if (folder == null) {
            LOGGER.warn("Preparation {} does not belong to a folder, skip migration (not accessible to user).",
                    preparation.getName());
            return;
        }

        // Run preparation
        final ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation.getId());
        exportParameters.setDatasetId(preparation.getDataSetId());
        exportParameters.setExportType("JSON");

        // just process the preparation, the transformation service will automatically update the steps with row metadata
        try (NullOutputStream outputStream = new NullOutputStream()) {
            service.execute(exportParameters).writeTo(outputStream);
        } catch (Throwable e) {
            LOGGER.warn(
                    "Error processing preparation {} (#{}), semantic categories are not properly stored and may change if you change them using the data quality command line",
                    preparation.getName(), preparation.getId());
            LOGGER.debug("Here is the stacktrace", e);
        }

        LOGGER.info("[{}/{}] preparation #{} done", currentProcessingNumber.get(), total, preparation.getId());
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public UpgradeTask.target getTarget() {
        return VERSION;
    }
}
