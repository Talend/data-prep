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

package org.talend.dataprep.api.service.test;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.http.ContentType.TEXT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.MappingIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.MixedContentMap;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.PreparationAPITest;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.format.CSVFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Test client for data-prep API.
 */
@Component
public class APIClientTest {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(APIClientTest.class);

    @Autowired
    protected ObjectMapper mapper;

    public List<UserDataSetMetadata> listDataSets(String name) throws IOException {
        InputStream result;
        if (name == null) {
            result = when().get("/api/datasets").asInputStream();
        } else {
            result = when().get("/api/datasets?name={name}", name).asInputStream();
        }
        CollectionType resultType =
                TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, UserDataSetMetadata.class);
        return mapper.readValue(result, resultType);
    }

    public Folder getFolderByPath(String path) throws IOException {
        InputStream inputStream = with().queryParam("path", path).get("/api/folders/search").asInputStream();
        MappingIterator<Folder> foldersIterator = mapper.readerFor(Folder.class).readValues(inputStream);
        List<Folder> folders = foldersIterator.readAll();
        assertTrue(folders.size() == 1);
        return folders.iterator().next();
    }

    /**
     * Create a dataset.
     *
     * @param file the classpath of the file to upload.
     * @param name the dataset name.
     * @return the dataset id.
     * @throws IOException sh*t happens.
     */
    public String createDataset(final String file, final String name) throws IOException {
        final InputStream resourceAsStream = PreparationAPITest.class.getResourceAsStream(file);
        assertNotNull(resourceAsStream);
        final String datasetContent = IOUtils.toString(resourceAsStream, UTF_8);
        final Response post = given() //
                .contentType(TEXT) //
                .body(datasetContent) //
                .queryParam("name", name) //
                .when() //
                .post("/api/datasets");

        final int statusCode = post.getStatusCode();
        if (statusCode != 200) {
            LOGGER.error("Unable to create dataset (HTTP " + statusCode + "). Error: {}", post.asString());
        }
        assertThat(statusCode, is(200));
        final String dataSetId = post.asString();
        assertNotNull(dataSetId);
        assertThat(dataSetId, not(""));

        return dataSetId;
    }

    /**
     * Create an empty preparation from a dataset.
     *
     * @param file the dataset classpath file to upload.
     * @param name the preparation name.
     * @param folderId where to create the preparation.
     * @return the preparation id.
     * @throws IOException sh*i happens.
     */
    public String createPreparationFromFile(final String file, final String name, final String folderId)
            throws IOException {
        final String dataSetId = createDataset(file, "testDataset-" + UUID.randomUUID());
        return createPreparationFromDataset(dataSetId, name, folderId);
    }

    /**
     * Create an empty preparation from a dataset.
     *
     * @param dataSetId the dataset id to create the preparation from.
     * @param name the preparation name.
     * @param folderId where to create the preparation.
     * @return the preparation id.
     * @throws IOException sh*t happens.
     */
    public String createPreparationFromDataset(final String dataSetId, final String name, final String folderId)
            throws IOException {

        RequestSpecification request = given() //
                .contentType(JSON) //
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\"}");

        if (folderId != null) {
            request = request.queryParam("folder", folderId);
        }

        final Response response = request //
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .post("/api/preparations");

        assertThat(response.getStatusCode(), is(200));

        final String preparationId = response.asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param actionFile the classpath of the step to add.
     * @throws IOException sh*t happens.
     */
    public void applyActionFromFile(final String preparationId, final String actionFile) throws IOException {
        final String action = IOUtils.toString(PreparationAPITest.class.getResourceAsStream(actionFile), UTF_8);
        applyAction(preparationId, action);
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param action the content of the step to add.
     * @throws IOException sh*t happens.
     */
    public void applyAction(final String preparationId, final String action) throws IOException {
        given()
                .contentType(JSON)
                .body(action)
                .when()
                .post("/api/preparations/{id}/actions", preparationId)
                .then()
                .statusCode(is(200));
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param actionName action name
     * @param parameters action parameters
     * @throws IOException sh*t happens.
     */
    public void applyAction(final String preparationId, final String actionName, Map<String, String> parameters)
            throws IOException {
        org.talend.dataprep.api.preparation.Actions actions = new org.talend.dataprep.api.preparation.Actions();
        Action action = new Action();
        action.setName(actionName);
        action.setParameters(MixedContentMap.convert(parameters));
        actions.setActions(Collections.singletonList(action));
        given()
                .contentType(JSON.withCharset(UTF_8))
                .content(actions) //
                .when()
                .post("/api/preparations/{id}/actions", preparationId) //
                .then()
                .statusCode(is(200));
    }

    /**
     * Fetch the preparation metadata.
     *
     * @param preparationId id of the preparation to fetch
     * @return the preparation details
     * @throws IOException if a connexion or parsing error happen
     */
    public Preparation getPreparationDetails(String preparationId) throws IOException {
        String json = //
                expect() //
                        .statusCode(200)
                        .log()
                        .ifValidationFails() //
                        .when() //
                        .get("/api/preparations/{id}/details", preparationId)
                        .asString();
        return mapper.readerFor(Preparation.class).readValue(json);
    }

    public Response getPreparation(String preparationId) throws IOException {
        return getPreparation(preparationId, "head", "HEAD");
    }

    public Response getPreparation(String preparationId, String versionId) throws IOException {
        return getPreparation(preparationId, versionId, "HEAD");
    }

    /**
     * Method handling 202/200 status to get the transformation content
     *
     * @param preparationId prepartionId
     * @return the content of a preparation
     * @throws IOException
     */
    public Response getPreparation(String preparationId, String version, String stepId) throws IOException {
        // when
        Response transformedResponse = given()
                .when() //
                .get("/api/preparations/{prepId}/content?version={version}&from={stepId}", preparationId, version,
                        stepId);

        if (ACCEPTED.value() == transformedResponse.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = transformedResponse.getHeader("Location");

            waitForAsyncMethodTofinish(asyncMethodStatusUrl);

            transformedResponse = given()
                    .when() //
                    .expect()
                    .statusCode(200)
                    .log()
                    .ifError() //
                    .get("/api/preparations/{prepId}/content?version={version}&from={stepId}", preparationId, version,
                            stepId);
        }

        return transformedResponse;
    }

    /**
     * Ping (100 times max) async method status url in order to wait the end of the execution
     *
     * @param asyncMethodStatusUrl
     * @throws IOException
     */
    public void waitForAsyncMethodTofinish(String asyncMethodStatusUrl) throws IOException {
        boolean isAsyncMethodRunning = true;
        int nbLoop = 0;

        while (isAsyncMethodRunning && nbLoop < 100) {

            String statusAsyncMethod = given()
                    .when() //
                    .expect()
                    .statusCode(200)
                    .log()
                    .ifError() //
                    .get(asyncMethodStatusUrl)
                    .asString();

            AsyncExecutionMessage asyncExecutionMessage =
                    mapper.readerFor(AsyncExecutionMessage.class).readValue(statusAsyncMethod);

            AsyncExecution.Status asyncStatus = asyncExecutionMessage.getStatus();
            isAsyncMethodRunning =
                    asyncStatus.equals(AsyncExecution.Status.RUNNING) || asyncStatus.equals(AsyncExecution.Status.NEW);

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                LOGGER.error("cannot sleep", e);
                Assert.fail();
            }
            nbLoop++;
        }
    }

    public Response exportPreparation(String preparationId, String stepId, String csvDelimiter, String fileName)
            throws IOException, InterruptedException {
        return export(preparationId, "", stepId, csvDelimiter, fileName);
    }

    public Response exportPreparation(String preparationId, String stepId, String csvDelimiter)
            throws IOException, InterruptedException {
        return export(preparationId, "", stepId, csvDelimiter, null);
    }

    public Response exportPreparation(String preparationId, String stepId) throws IOException, InterruptedException {
        return export(preparationId, "", stepId, null, null);
    }

    public Response exportDataset(String datasetId, String stepId) throws IOException, InterruptedException {
        return export("", datasetId, stepId, null, null);
    }

    protected Response export(String preparationId, String datasetId, String stepId, String csvDelimiter,
            String fileName) throws IOException, InterruptedException {
        // when
        Response export = getExportResponse(preparationId, datasetId, stepId, csvDelimiter, fileName, null);

        if (ACCEPTED.value() == export.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = export.getHeader("Location");

            waitForAsyncMethodTofinish(asyncMethodStatusUrl);

            export = getExportResponse(preparationId, datasetId, stepId, csvDelimiter, fileName, 200);
        }

        return export;
    }

    private Response getExportResponse(String preparationId, String datasetId, String stepId, String csvDelimiter,
            String fileName, Integer expectedStatus) {
        RequestSpecification exportRequest = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE,
                        CSVFormat.ParametersCSV.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", stepId) //
                .formParam("datasetId", datasetId); //

        if (StringUtils.isNotEmpty(csvDelimiter)) {
            exportRequest.formParam(ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER, csvDelimiter);
        }

        if (StringUtils.isNotEmpty(fileName)) {
            exportRequest.formParam(ExportFormat.PREFIX + "fileName", fileName);
        }

        if (expectedStatus != null) {
            exportRequest
                    .when() //
                    .expect() //
                    .statusCode(expectedStatus) //
                    .log() //
                    .ifError();
        }

        return exportRequest.get("/api/export");
    }

    public DataSetMetadata getPrepMetadata(String preparationId) throws IOException {
        DataSetMetadata metadata;

        // when
        Response transformedResponse = given().when().get("/api/preparations/{id}/metadata", preparationId);

        HttpStatus responseStatus = HttpStatus.valueOf(transformedResponse.getStatusCode());
        if (ACCEPTED.equals(responseStatus)) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = transformedResponse.getHeader("Location");

            waitForAsyncMethodTofinish(asyncMethodStatusUrl);

            Response response = given()
                    .when()
                    .expect()
                    .statusCode(200)
                    .log()
                    .ifError() //
                    .get("/api/preparations/{id}/metadata", preparationId);
            metadata = mapper.readValue(response.asInputStream(), DataSetMetadata.class);
        } else if (OK.equals(responseStatus)) {
            metadata = mapper.readValue(transformedResponse.asInputStream(), DataSetMetadata.class);
        } else {
            throw new RuntimeException(
                    "Could not get preparation metadata. Response was: " + transformedResponse.print());
        }
        return metadata;
    }

}
