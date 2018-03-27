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

package org.talend.dataprep.qa.config;

import cucumber.api.java.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.Folder;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

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

        Boolean cleanAfterStepIsOK = true;

        // cleaning stored actions
        context.clearAction();

        // cleaning temporary files
        context.clearTempFile();

        // cleaning application's preparations
        List<String> listPreparationDeletionPb = context.getPreparationIds().stream().filter(preparationDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterStepIsOK = listPreparationDeletionPb.size() == 0;

        // cleaning preparations's related context
        context.clearPreparation();

        // cleaning application's datasets
        List<String> listDatasetDeletionPb = context.getDatasetIds().stream().filter(datasetDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterStepIsOK = cleanAfterStepIsOK && listDatasetDeletionPb.size() == 0;

        // cleaning dataset's related context
        context.clearDataset();

        // cleaning application's folders
        List<Folder> listFolderDeletionPb = context.getFolders().stream().filter(folderDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterStepIsOK = cleanAfterStepIsOK && listFolderDeletionPb.size() == 0;

        // cleaning folder's related context
        context.clearFolders();

        // cleaning all features context object
        context.clearObject();

        // cleaning all export parameters
        context.clearPreparationExportFormat();

        if (cleanAfterStepIsOK) {
            LOGGER.info("The Clean After Step is Ok. All deletion were done.");
        } else {
            for (String prepId:listPreparationDeletionPb) {
                LOGGER.warn("Pb in the deletion of preparation {}.",prepId);
            }
            for (String datasetId:listDatasetDeletionPb) {
                LOGGER.warn("Pb in the deletion of dataset {}.",datasetId);
            }
            for (Folder folder:listFolderDeletionPb) {
                LOGGER.warn("Pb in the deletion of folder {}.",folder.getPath());
            }
            LOGGER.warn("The Clean After Step has failed. All deletion were not done.");
            fail("Fail to delete preparation : go to see the logs to obtain more details. Good luck luke. May the Force (may)be with you");
        }
    }

    private Predicate<String> preparationDeletionIsNotOK() {
        return preparationId -> {
            try {
                return api.deletePreparation(preparationId).getStatusCode() != 200;
            } catch (Exception ex) {
                LOGGER.debug("Error on preparation's suppression {}.", preparationId);
                return true;
            }
        };
    }

    private Predicate<String> datasetDeletionIsNotOK() {
        return datasetId -> {
            try {
                // Even if the dataset doesn't exist, the status is 200
                return api.deleteDataset(datasetId).getStatusCode() != 200;
            } catch (Exception ex) {
                LOGGER.debug("Error on dataset's suppression  {}.", datasetId);
                return true;
            }
        };
    }

    private Predicate<Folder> folderDeletionIsNotOK() {
        return folder -> {
            try {
                return folderUtil.deleteFolder(folder).getStatusCode() != 200;
            } catch (Exception ex) {
                LOGGER.debug("Error on folder's suppression  {}.", folder.getPath());
                return true;
            }
        };
    }


}
