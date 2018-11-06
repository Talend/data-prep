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

import static junit.framework.TestCase.assertTrue;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.awaitility.core.ConditionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.helper.VerboseMode;
import org.talend.dataprep.qa.SpringContextConfiguration;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.util.OSIntegrationTestUtil;
import org.talend.dataprep.qa.util.folder.FolderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    /**
     * {@link cucumber.api.DataTable} key for origin folder.
     */
    protected static final String ORIGIN = "origin";

    /**
     * {@link cucumber.api.DataTable} key for preparationName value.
     */
    protected static final String PREPARATION_NAME = "preparationName";

    protected static final String DATASET_NAME_KEY = "name";

    protected static final String DATASET_ID_KEY = "dataSetId";

    private static final int TIME_OUT = 20;

    private static final int POLL_DELAY = 1;

    private static final int POLL_INTERVAL = 1;

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPrepStep.class);

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FeatureContext context;

    @Autowired
    protected OSDataPrepAPIHelper api;

    @Autowired
    protected OSIntegrationTestUtil util;

    @Autowired
    protected FolderUtil folderUtil;

    @Value("${restassured.debug:NONE}")
    private VerboseMode restAssuredDebug;

    @PostConstruct
    public void init() {
        api.setRestAssuredDebug(restAssuredDebug);
    }

    /**
     * Retrieve the details of a preparation from its id.
     *
     * @param preparationId the preparation id.
     * @return the preparation details.
     */
    protected PreparationDetails getPreparationDetails(String preparationId) {
        Response response = api.getPreparationDetails(preparationId);
        response.then().statusCode(HttpStatus.OK.value());

        return response.as(PreparationDetails.class);
    }

    protected Predicate<String> preparationDeletionIsNotOK() {
        return preparationId -> {
            try {
                return api.deletePreparation(preparationId).getStatusCode() != OK.value();
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
                return api.deleteDataset(datasetId).getStatusCode() != OK.value();
            } catch (Exception ex) {
                LOGGER.debug("Error on Dataset's suppression  {}.", datasetId);
                return true;
            }
        };
    }

    protected Predicate<Folder> folderDeletionIsNotOK() {
        return folder -> {
            try {
                return !HttpStatus.valueOf(folderUtil.deleteFolder(folder).getStatusCode()).is2xxSuccessful();
            } catch (Exception ex) {
                LOGGER.debug("Error on folder's suppression  {}.", folder.getPath());
                return true;
            }
        };
    }

    protected void checkColumnNames(String datasetOrPreparationName, List<String> expectedColumnNames,
            List<String> actual) {
        assertNotNull("No columns in \"" + datasetOrPreparationName + "\".", actual);
        assertFalse("No columns in \"" + datasetOrPreparationName + "\".", actual.isEmpty());
        assertEquals("Not the expected number of columns in \"" + datasetOrPreparationName + "\".",
                expectedColumnNames.size(), actual.size());
        assertTrue("\"" + datasetOrPreparationName + "\" doesn't contain all expected columns.",
                actual.containsAll(expectedColumnNames));
    }

    protected ConditionFactory waitResponse(String message) {
        return waitResponse(message, TIME_OUT);
    }

    protected ConditionFactory waitResponse(String message, int timeOut) {
        return waitResponse(message, timeOut, POLL_DELAY, POLL_INTERVAL);
    }

    protected ConditionFactory waitResponse(String message, int timeOut, int pollInterval) {
        return waitResponse(message, timeOut, POLL_DELAY, pollInterval);
    }

    protected ConditionFactory waitResponse(String message, int timeOut, int pollDelay, int pollInterval) {
        return with() //
                .pollInterval(pollInterval, TimeUnit.SECONDS) //
                .and() //
                .with() //
                .pollDelay(pollDelay, TimeUnit.SECONDS) //
                .await(message) //
                .atMost(timeOut, TimeUnit.SECONDS);
    }

    protected class CleanAfterException extends RuntimeException {

        CleanAfterException(String s) {
            super(s);
        }
    }
}
