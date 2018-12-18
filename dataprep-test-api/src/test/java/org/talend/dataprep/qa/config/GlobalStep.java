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

import static org.talend.dataprep.qa.config.FeatureContext.setUseSuffix;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.Folder;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * Storage for Before and After actions.
 */
public class GlobalStep extends DataPrepStep {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalStep.class);

    /**
     * Suffix deactivation for a scenario.
     * First @Before to be executed in OS context.
     * Second @Before to be executed in EE context.
     */
    @Before(value = "@SuffixOff", order = 200)
    public void deactivateSuffix() {
        setUseSuffix(false);
    }

    /**
     * Suffix reactivation after an explicit deactivation in a scenario.
     * Second @After to be executed in OS context
     * Third @After to be executed in EE context
     */
    @After(value = "@SuffixOff", order = 200)
    public void reactivateSuffix() {
        setUseSuffix(true);
    }

    /**
     * Clean the created objects in the test environment.
     * This method must be called on the last scenario of each feature in order to keep the tests reentrant.
     * It also can be called on demand to clean the context for the next scenario.
     * First @After to be executed in OS context
     * Second @After to be executed in EE context
     */
    @After(value = "@CleanAfter", order = 300)
    public void cleanAfter() {
        LOGGER.debug("Cleaning IT context.");

        Boolean cleanAfterOSStepIsOK = true;

        // cleaning stored actions
        context.clearAction();

        // cleaning temporary files
        context.getTempFiles().forEach(filename -> {
            File file = context.getTempFile(filename);
            deleteFile(file);
        });
        context.clearTempFile();

        // cleaning application's preparations
        List<String> listPreparationDeletionPb = context
                .getPreparationIdsToDelete()
                .stream()
                .filter(preparationDeletionIsNotOK())
                .collect(Collectors.toList());
        cleanAfterOSStepIsOK = listPreparationDeletionPb.size() == 0;

        // cleaning preparations's related context
        context.clearPreparationLists();

        // cleaning application's datasets
        List<String> listDatasetDeletionPb =
                context.getDatasetIdsToDelete().stream().filter(datasetDeletionIsNotOK()).collect(Collectors.toList());
        cleanAfterOSStepIsOK &= listDatasetDeletionPb.size() == 0;

        // cleaning dataset's related context
        context.clearDatasetLists();

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

    /**
     * Delete a file that can represent a directory
     * 
     * @param file
     */
    private void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f != null && file.exists()) {
                    deleteFile(f);
                }
            }
        }
    }
}
