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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.util.OSIntegrationTestUtil;
import org.talend.dataprep.qa.util.folder.FolderUtil;

import java.util.function.Predicate;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepStep.class);

    /**
     * {@link cucumber.api.DataTable} key for origin folder.
     */
    protected static final String ORIGIN = "origin";

    /**
     * {@link cucumber.api.DataTable} key for preparationName value.
     */
    protected static final String PREPARATION_NAME = "preparationName";

    @Autowired
    public FeatureContext context;

    @Autowired
    protected OSDataPrepAPIHelper api;

    @Autowired
    protected OSIntegrationTestUtil util;

    @Autowired
    protected FolderUtil folderUtil;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieve the details of a preparation from its id.
     *
     * @param preparationId the preparation id.
     * @return the preparation details.
     */
    protected PreparationDetails getPreparationDetails(String preparationId) {
        Response response = api.getPreparationDetails(preparationId);
        response.then()
                .statusCode(200);

        return response.as(PreparationDetails.class);
    }

    protected class CleanAfterException extends RuntimeException {
        CleanAfterException(String s) {
            super(s);
        }
    }

    protected Predicate<String> preparationDeletionIsNotOK() {
        return preparationId -> {
            try {
                return api.deletePreparation(preparationId).getStatusCode() != 200;
            } catch (Exception ex) {
                LOGGER.debug("Error on preparation's suppression {}.", preparationId);
                return true;
            }
        };
    }

    protected Predicate<String> datasetDeletionIsNotOK() {
        return datasetId -> {
            try {
                // Even if the dataset doesn't exist, the status is 200
                return api.deleteDataset(datasetId).getStatusCode() != 200;
            } catch (Exception ex) {
                LOGGER.debug("Error on Dataset's suppression  {}.", datasetId);
                return true;
            }
        };
    }

    protected Predicate<Folder> folderDeletionIsNotOK() {
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
