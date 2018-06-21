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

package org.talend.dataprep.qa.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.Folder;

import cucumber.api.java.After;

/**
 * Storage for Before and After actions.
 */
public class GlobalStep extends DataPrepStep {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalStep.class);

    /**
     * Clean the created objects in the test environment.
     * This method must be called on the last scenario of each feature in order to keep the tests reentrant.
     * It also can be called on demand to clean the context for the next scenario.
     */
    @After("@CleanAfter")
    public void cleanAfter() {
        LOGGER.debug("Cleaning IT context.");

        Boolean cleanAfterOSStepIsOK = true;

        // cleaning stored actions
        context.clearAction();

        // cleaning temporary files
        context.clearTempFile();

        // cleaning application's preparations
        List<String> listPreparationDeletionPb =
                context.getPreparationIds().stream().filter(preparationDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterOSStepIsOK = listPreparationDeletionPb.size() == 0;

        // cleaning preparations's related context
        context.clearPreparation();

        // cleaning application's datasets
        List<String> listDatasetDeletionPb =
                context.getDatasetIds().stream().filter(datasetDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterOSStepIsOK &= listDatasetDeletionPb.size() == 0;

        // cleaning dataset's related context
        context.clearDataset();

        // cleaning application's folders
        List<Folder> listFolderDeletionPb =
                context.getFolders().stream().filter(folderDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterOSStepIsOK &= listFolderDeletionPb.size() == 0;

        // cleaning folder's related context
        context.clearFolders();

        // cleaning all features context object
        context.clearObject();

        if (cleanAfterOSStepIsOK) {
            LOGGER.info("The Clean After Step is Ok. All deletion were done.");
        } else {
            for (String prepId : listPreparationDeletionPb) {
                LOGGER.warn("Pb in the deletion of preparation {}.", prepId);
            }
            for (String datasetId : listDatasetDeletionPb) {
                LOGGER.warn("Pb in the deletion of dataset {}.", datasetId);
            }
            for (Folder folder : listFolderDeletionPb) {
                LOGGER.warn("Pb in the deletion of folder {}.", folder.getPath());
            }
            LOGGER.warn("The Clean After Step has failed (OS side). All deletion were not done.");
            throw new CleanAfterException(
                    "Fail to delete some elements : go to see the logs to obtain more details. Good luck luke. May the Force (may)be with you");
        }
    }

}
