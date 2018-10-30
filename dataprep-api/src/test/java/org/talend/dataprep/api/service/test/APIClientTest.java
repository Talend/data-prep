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

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.RestAssured.with;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.format.CSVFormat;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

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
        CollectionType resultType = //
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
        return createDataset(resourceAsStream, name);
    }

    /**
     * Create a dataset.
     *
     * @param resourceAsStream the stream to upload.
     * @param name the dataset name.
     * @return the dataset id.
     * @throws IOException sh*t happens.
     */
    public String createDataset(final InputStream resourceAsStream, final String name) throws IOException {
        return createDataset(resourceAsStream, MediaType.TEXT_PLAIN, name);
    }

    public String createDataset(final InputStream resourceAsStream, MediaType contentType, final String name)
            throws IOException {
        assertNotNull(resourceAsStream);
        final String datasetContent = IOUtils.toString(resourceAsStream, UTF_8);
        final Response post = given() //
                .contentType(contentType.toString()) //
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

    public DataSetMetadata getDataSetMetadata(String dataSetId) throws IOException {
        return mapper.readerFor(DataSetMetadata.class).readValue(
                get("/api/datasets/{id}/metadata", dataSetId).asInputStream());
    }

    public void setDataSetMetadata(DataSetMetadata dataSetMetadata) throws IOException {
        String metadataAsJsonString = mapper.writerFor(DataSetMetadata.class).writeValueAsString(dataSetMetadata);
        given().body(metadataAsJsonString).put("/api/datasets/{id}/metadata", dataSetMetadata.getId());
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
     */
    public String createPreparationFromDataset(final String dataSetId, final String name, final String folderId) {

        RequestSpecification request = given() //
                .contentType(JSON) //
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\"}");

        if (folderId != null) {
            request = request.queryParam("folder", folderId);
        }

        final Response response = request //
                .when() //
                .expect() //
                .statusCode(200) //
                .log() //
                .ifError() //
                .post("/api/preparations");

        assertThat(response.getStatusCode(), is(200));

        final String preparationId = response.asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    public boolean deletePreparation(String preparationId) {
        Response response = delete("/api/preparations/{id}", preparationId);
        return response.statusCode() == HttpStatus.OK.value();
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

    public void applyAction(final String preparationId, final ActionParameters action) throws IOException {
        PreparationStep step = new PreparationStep();
        step.setActions(Collections.singletonList(action));
        given()
                .contentType(JSON) //
                .body(mapper.writeValueAsString(step)) //
                .when() //
                .post("/api/preparations/{id}/actions", preparationId) //
                .then() //
                .statusCode(is(200));
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param action the content of the step to add.
     */
    public void applyAction(final String preparationId, final String action) {
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
     */
    public void applyAction(final String preparationId, final String actionName, Map<String, String> parameters) {
        org.talend.dataprep.api.preparation.Actions actions = new org.talend.dataprep.api.preparation.Actions();
        Action action = new Action();
        action.setName(actionName);
        action.setParameters(MixedContentMap.convert(parameters));
        actions.setActions(Collections.singletonList(action));
        given() //
                .contentType(JSON.withCharset(UTF_8)) //
                .content(actions) //
                .when() //
                .post("/api/preparations/{id}/actions", preparationId) //
                .then() //
                .statusCode(is(200));
    }

    /**
     * List all preparations.
     *
     * @return list of preparations
     */
    public List<Preparation> listPreparations() throws IOException {
        InputStream inputStream = when().get("/api/preparations").asInputStream();
        return mapper.readerFor(Preparation.class).<Preparation> readValues(inputStream).readAll();
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

    /**
     * Fetch the preparation details with a specific version.
     *
     * @param preparationId id of the preparation to fetch
     * @param versionId id of the preparation version
     * @return the preparation details
     */
    public Response getPreparationDetails(String preparationId, String versionId) {
        return expect() //
                .when() //
                .get("/api/preparations/{id}/versions/{versionId}/details", preparationId, versionId);
    }

    public PreparationExport getPreparationAsObject(String preparationId) throws IOException {
        return getPreparationAsObject(preparationId, "head", "HEAD", "");
    }

    public PreparationExport getPreparationWithFilterAsObject(String preparationId, String filter) throws IOException {
        return getPreparationAsObject(preparationId, "head", "HEAD", filter);
    }

    public PreparationExport getPreparationAsObject(String preparationId, String versionId) throws IOException {
        return getPreparationAsObject(preparationId, versionId, "HEAD", "");
    }

    /**
     * Method handling 202/200 status to get the transformation content
     *
     * @param preparationId is of the preparation
     * @param version version of the preparation
     * @param stepId like HEAD or FILTER, etc.
     * @param filter TQL filter to filter the preparation content
     * @return the content of a preparation
     * @throws IOException
     */
    public PreparationExport getPreparationAsObject(String preparationId, String version, String stepId, String filter)
            throws IOException {
        return mapper.readValue(getPreparation(preparationId, version, stepId, filter).asInputStream(),
                PreparationExport.class);
    }

    public static class PreparationExport {

        public DataSetMetadata metadata;

        public List<Map<String, String>> records;

    }

    public Response getPreparation(String preparationId) throws IOException {
        return getPreparation(preparationId, "head", "HEAD", "");
    }

    public Response getPreparationWithFilter(String preparationId, String filter) throws IOException {
        return getPreparation(preparationId, "head", "HEAD", filter);
    }

    public Response getPreparation(String preparationId, String versionId) throws IOException {
        return getPreparation(preparationId, versionId, "HEAD", "");
    }

    /**
     * Method handling 202/200 status to get the transformation content
     *
     * @param preparationId is of the preparation
     * @param version version of the preparation
     * @param stepId like HEAD or FILTER, etc.
     * @param filter TQL filter to filter the preparation content
     * @return the content of a preparation
     * @throws IOException
     */
    public Response getPreparation(String preparationId, String version, String stepId, String filter)
            throws IOException {
        // when
        Response transformedResponse;
        RequestSpecification initialRequest = given().when();
        if (filter.isEmpty()) {
            transformedResponse = initialRequest //
                    .get("/api/preparations/{prepId}/content?version={version}&from={stepId}", preparationId, version,
                            stepId);
        } else {
            transformedResponse = initialRequest //
                    .get("/api/preparations/{prepId}/content?version={version}&from={stepId}&filter={filter}",
                            preparationId, version, stepId, filter);
        }

        if (ACCEPTED.value() == transformedResponse.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = transformedResponse.getHeader("Location");

            waitForAsyncMethodToFinishWithSuccess(asyncMethodStatusUrl);

            ResponseSpecification contentRequest = given() //
                    .when() //
                    .expect() //
                    .statusCode(200) //
                    .log() //
                    .ifError();
            if (filter.isEmpty()) {
                transformedResponse = contentRequest //
                        .get("/api/preparations/{prepId}/content?version={version}&from={stepId}", preparationId,
                                version, stepId);
            } else {
                transformedResponse = contentRequest //
                        .get("/api/preparations/{prepId}/content?version={version}&from={stepId}&filter={filter}",
                                preparationId, version, stepId, filter);
            }
        }

        return transformedResponse;
    }

    public void waitForAsyncMethodToFinishWithSuccess(String asyncMethodStatusUrl) throws IOException {
        AsyncExecution.Status asyncStatus = waitForAsyncMethodToFinish(asyncMethodStatusUrl);
        assertEquals(AsyncExecution.Status.DONE, asyncStatus);
    }

    /**
     * Ping (100 times max) async method status url in order to wait the end of the execution
     *
     * @param asyncMethodStatusUrl
     * @return the status of the async execution (is likely DONE or FAILED)
     * @throws IOException
     */
    public AsyncExecution.Status waitForAsyncMethodToFinish(String asyncMethodStatusUrl) throws IOException {
        boolean isAsyncMethodRunning = true;
        int nbLoop = 0;
        AsyncExecution.Status asyncStatus = null;
        while (isAsyncMethodRunning && nbLoop < 100) {

            String statusAsyncMethod = given() //
                    .when() //
                    .expect() //
                    .statusCode(200) //
                    .log() //
                    .ifError() //
                    .get(asyncMethodStatusUrl) //
                    .asString();

            AsyncExecutionMessage asyncExecutionMessage =
                    mapper.readerFor(AsyncExecutionMessage.class).readValue(statusAsyncMethod);

            asyncStatus = asyncExecutionMessage.getStatus();
            isAsyncMethodRunning =
                    asyncStatus.equals(AsyncExecution.Status.RUNNING) || asyncStatus.equals(AsyncExecution.Status.NEW);

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                LOGGER.error("cannot sleep", e);
                fail();
            }
            nbLoop++;
        }
        return asyncStatus;
    }

    public Response getFailedPreparationWithFilter(String preparationId, String malformedFilter) throws IOException {
        Response transformedResponse = given() //
                .when() //
                .get("/api/preparations/{prepId}/content?version={version}&from={stepId}&filter={filter}",
                        preparationId, "head", "HEAD", malformedFilter);

        if (ACCEPTED.value() == transformedResponse.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = transformedResponse.getHeader("Location");

            AsyncExecution.Status asyncStatus = waitForAsyncMethodToFinish(asyncMethodStatusUrl);
            assertEquals(AsyncExecution.Status.FAILED, asyncStatus);

            return given()
                    .expect() //
                    .statusCode(200) //
                    .log()
                    .ifError() //
                    .when() //
                    .get(asyncMethodStatusUrl);
        }
        return transformedResponse;
    }

    public Response exportPreparation(String preparationId, String stepId, String csvDelimiter, String fileName)
            throws IOException {
        return export(preparationId, null, stepId, csvDelimiter, fileName);
    }

    public Response exportPreparation(String preparationId, String stepId, String csvDelimiter) throws IOException {
        return export(preparationId, "", stepId, csvDelimiter, null);
    }

    public Response exportPreparation(String preparationId, String stepId) throws IOException {
        return export(preparationId, "", stepId, null, null);
    }

    public Response exportDataset(String datasetId, String stepId) throws IOException {
        return export("", datasetId, stepId, null, null);
    }

    protected Response export(String preparationId, String datasetId, String stepId, String csvDelimiter,
            String fileName) throws IOException {
        // when
        Response export = getExportResponse(preparationId, datasetId, stepId, csvDelimiter, fileName, null);

        if (ACCEPTED.value() == export.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = export.getHeader("Location");

            waitForAsyncMethodToFinishWithSuccess(asyncMethodStatusUrl);

            export = getExportResponse(preparationId, datasetId, stepId, csvDelimiter, fileName, 200);
        }

        return export;
    }

    private Response getExportResponse(String preparationId, String datasetId, String stepId, String csvDelimiter,
            String fileName, Integer expectedStatus) {
        RequestSpecification exportRequest = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE, //
                        CSVFormat.ParametersCSV.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", stepId);

        if (datasetId != null) {
            exportRequest.formParam("datasetId", datasetId);
        }

        if (StringUtils.isNotEmpty(csvDelimiter)) {
            exportRequest.formParam(ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER, csvDelimiter);
        }

        if (StringUtils.isNotEmpty(fileName)) {
            exportRequest.formParam(ExportFormat.PREFIX + "fileName", fileName);
        }

        if (expectedStatus != null) {
            exportRequest //
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

            waitForAsyncMethodToFinishWithSuccess(asyncMethodStatusUrl);

            Response response = given() //
                    .when() //
                    .expect() //
                    .statusCode(200) //
                    .log() //
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

    public static class PreparationStep {

        private List<ActionParameters> actions = new ArrayList<>(1);

        public List<ActionParameters> getActions() {
            return actions;
        }

        public void setActions(List<ActionParameters> actions) {
            this.actions = actions;
        }
    }

    public static class ActionParameters {

        private String action;

        private Map<String, String> parameters;

        public static ActionParameters createAction(String name) {
            ActionParameters actionParameters = new ActionParameters();
            actionParameters.setAction(name);
            return actionParameters;
        }

        public ActionParameters withParameter(String key, String value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(key, value);
            return this;
        }

        public ActionParameters withColumnId(String columnId) {
            return withParameter(ImplicitParameters.COLUMN_ID.getKey(), columnId);
        }

        public ActionParameters withRowId(String rowId) {
            return withParameter(ImplicitParameters.ROW_ID.getKey(), rowId);
        }

        public ActionParameters withScope(ScopeCategory scope) {
            return withParameter(ImplicitParameters.SCOPE.getKey(), scope == null ? null : scope.name());
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
    }

}
