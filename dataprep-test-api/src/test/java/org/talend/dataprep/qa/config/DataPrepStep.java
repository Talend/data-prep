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
import cucumber.api.DataTable;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
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
import org.talend.dataprep.qa.dto.ContentMetadataColumn;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.PreparationContent;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.dto.Statistics;
import org.talend.dataprep.qa.util.OSIntegrationTestUtil;
import org.talend.dataprep.qa.util.folder.FolderUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

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

    protected static final String HEAD_ID = "HEAD";

    protected static final String VERSION_HEAD = "head";

    /**
     * This class' logger.
     */
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

    protected class CleanAfterException extends RuntimeException {

        CleanAfterException(String s) {
            super(s);
        }
    }

    protected void checkColumnNames(String datasetOrPreparationName, List<String> expectedColumnNames,
            List<String> actual) {
        assertNotNull("The returned columns' list on \"" + datasetOrPreparationName + "\"  is null.", actual);
        assertFalse("No columns in \"" + datasetOrPreparationName + "\".", actual.isEmpty());
        assertEquals("Not the expected number of columns in \"" + datasetOrPreparationName + "\".",
                expectedColumnNames.size(), actual.size());
        assertTrue(
                "\"" + datasetOrPreparationName + "\" doesn't contain all expected columns : \""
                        + CollectionUtils.disjunction(expectedColumnNames, actual).toString() + "\".",
                actual.containsAll(expectedColumnNames));
    }

    /**
     * Returns the dataset content, once all DQ analysis are done and so all fields are up-to-date.
     *
     * @param datasetId the id of the dataset
     * @param tql       the TQL filter to apply to the dataset
     * @return the up-to-date dataset content
     */
    protected DatasetContent getDatasetContent(String datasetId, String tql) throws Exception {
        AtomicReference<DatasetContent> datasetContentReference = new AtomicReference<>();
        // TODO I guess this wait is useless since we use {DataPrepStep#checkDatasetMetadataStatus} before
        api.waitResponse("Waiting frequency table from dataset metadata of " + datasetId).until(() -> {
            Response response = api.getDataset(datasetId, tql);
            response.then().statusCode(200);

            DatasetContent datasetContent = response.as(DatasetContent.class);
            datasetContentReference.set(datasetContent);
            return datasetContent.metadata.columns //
                    .stream() //
                    .findFirst() //
                    .orElse(new ContentMetadataColumn()).statistics.frequencyTable;
        }, is(not(empty())));

        return datasetContentReference.get();
    }

    // FixMe : same thing as the other one because DatasetContent seems to be the same thing as PreparationContent
    protected PreparationContent getPreparationContent(String preparationName, String tql) throws IOException {
        String preparationId = context.getPreparationId(suffixName(preparationName));
        Response response = api.getPreparationContent(preparationId, VERSION_HEAD, HEAD_ID, tql);
        response.then().statusCode(200);

        return response.as(PreparationContent.class);
    }

    protected void checkSampleRecordsCount(String actualRecordsCount, String expectedRecordsCount) {
        if (expectedRecordsCount == null) {
            return;
        }
        Assert.assertEquals("The count records " + expectedRecordsCount + "is wrong: " + actualRecordsCount,
                expectedRecordsCount, actualRecordsCount);
    }

    protected void checkRecords(List<Object> actualRecords, String expectedRecordsFilename) throws Exception {
        if (expectedRecordsFilename == null) {
            return;
        }
        InputStream expectedRecordsFileStream = DataPrepStep.class.getResourceAsStream(expectedRecordsFilename);
        List<Object> expectedRecords = objectMapper.readValue(expectedRecordsFileStream, DatasetContent.class).records;

        Assert.assertEquals(expectedRecords.size(), actualRecords.size());
        Assert.assertTrue(
                "Difference between expected records and actual records:" //
                        + CollectionUtils.disjunction(expectedRecords, actualRecords).toString(),
                actualRecords.containsAll(expectedRecords));
    }

    protected void checkQualityPerColumn(List<ContentMetadataColumn> columns, String expectedQualityFilename)
            throws Exception {
        if (expectedQualityFilename == null) {
            return;
        }
        InputStream expectedQualityFileStream = DataPrepStep.class.getResourceAsStream(expectedQualityFilename);
        List<ContentMetadataColumn> expectedQualityPerColumn =
                objectMapper.readValue(expectedQualityFileStream, DatasetContent.class).metadata.columns;

        Assert.assertEquals(expectedQualityPerColumn.size(), columns.size());
        Collections.sort(columns);
        Collections.sort(expectedQualityPerColumn);
        for (int i = 0; i < expectedQualityPerColumn.size(); i++) {
            ContentMetadataColumn expectedColumn = expectedQualityPerColumn.get(i);
            ContentMetadataColumn column = columns.get(i);
            Assert.assertEquals(expectedColumn.id, column.id);
            Assert.assertEquals(expectedColumn.name, column.name);
            Assert.assertEquals(expectedColumn.type, column.type);
            Assert.assertEquals(expectedColumn.domain, column.domain);
            Map<String, Integer> expectedQuality = expectedColumn.quality;
            Statistics expectedStatistics = expectedColumn.statistics;

            Map<String, Integer> quality = column.quality;
            Assert.assertEquals(
                    "The valid records count " + expectedQuality.get("valid") + "is wrong: " + quality.get("valid"),
                    expectedQuality.get("valid"), quality.get("valid"));
            Assert.assertEquals(
                    "The valid records count " + expectedQuality.get("empty") + "is wrong: " + quality.get("empty"),
                    expectedQuality.get("empty"), quality.get("empty"));
            Assert.assertEquals(
                    "The valid records count " + expectedQuality.get("invalid") + "is wrong: " + quality.get("invalid"),
                    expectedQuality.get("invalid"), quality.get("invalid"));

            Statistics statistics = column.statistics;
            if (expectedStatistics != null && statistics != null) {
                Assert.assertTrue(
                        "Difference between expected records and actual records:" + //
                                CollectionUtils
                                        .disjunction(expectedStatistics.patternFrequencyTable,
                                                statistics.patternFrequencyTable)
                                        .toString(),
                        expectedStatistics.patternFrequencyTable.containsAll(statistics.patternFrequencyTable));
                Assert.assertTrue(expectedStatistics.frequencyTable.containsAll(statistics.frequencyTable));
            }
        }
    }

    public void checkContent(PreparationContent preparation, DataTable dataTable) throws Exception {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        checkRecords(preparation.records, expected.get("records"));
        checkQualityPerColumn(preparation.metadata.columns, expected.get("quality"));
        checkSampleRecordsCount(preparation.metadata.records, expected.get("sample_records_count"));
    }

}
