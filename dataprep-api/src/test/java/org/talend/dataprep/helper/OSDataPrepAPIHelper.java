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

package org.talend.dataprep.helper;

import static com.jayway.restassured.http.ContentType.JSON;
import static org.talend.dataprep.async.AsyncExecution.Status.FAILED;
import static org.talend.dataprep.async.AsyncExecution.Status.NEW;
import static org.talend.dataprep.async.AsyncExecution.Status.RUNNING;
import static org.talend.dataprep.helper.VerboseMode.NONE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionRequest;
import org.talend.dataprep.helper.api.Aggregate;
import org.talend.dataprep.helper.api.PreparationRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Utility class to allow dataprep-api integration tests.
 */
@Component
public class OSDataPrepAPIHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSDataPrepAPIHelper.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String FOLDER = "folder";

    private static final String VERSION = "version";

    private static final String FROM = "from";

    private static final String PARENT_ID = "parentId";

    private static final String NEW_NAME = "newName";

    private static final String DESTINATION = "destination";

    private static final String PARENT_STEP_ID = "parentStepId";

    private static final String NAME = "name";

    private static final String PATH = "path";

    private VerboseMode restAssuredDebug = NONE;

    @Value("${backend.api.url:http://localhost:8888}")
    private String apiBaseUrl;

    @Value("${execution.context:CLOUD}")
    private ITExecutionContext executionContext;

    @PostConstruct
    public void initExecutionContext() {
        LOGGER.info("Start Integration Test on '{}'",
                executionContext == ITExecutionContext.CLOUD ? "Cloud" : "On Premise");
    }

    /**
     * Wraps the {@link RestAssured#given()} method so that we can add behavior
     *
     * @return the request specification to use.
     */
    public RequestSpecification given() {
        RequestSpecification given = RestAssured.given().baseUri(apiBaseUrl);
        // just to add a line separator before log the method and the path
        RestAssured.config().getLogConfig().defaultStream().append(System.lineSeparator());
        switch (restAssuredDebug) {
        case ALL:
            given = given.log().all(true);
            break;
        case REQUESTS_ONLY:
            given = given.log().method().log().path();
            break;
        case NONE:
        default:
            break;
        }
        return given;
    }

    /**
     * Create a preparation from a dataset and a home folder.
     *
     * @param datasetID the dataset id to create the preparation from.
     * @param preparationName name for the new preparation.
     * @param homeFolderId new preparation folder.
     * @return the response.
     */
    public Response createPreparation(String datasetID, String preparationName, String homeFolderId) {
        return given() //
                .contentType(JSON) //
                .when() //
                .body(new PreparationRequest(datasetID, preparationName)) //
                .urlEncodingEnabled(false) //
                .queryParam(FOLDER, homeFolderId) //
                .post("/api/preparations");
    }

    /**
     * Get the preparation details.
     *
     * @param preparationId the preparation Id.
     * @return the response.
     */
    public Response getPreparationDetails(String preparationId) {
        return given() //
                .when() //
                .get("/api/preparations/{preparationId}/details", preparationId);
    }

    /**
     * Add an action to the end of a preparation.
     *
     * @param preparationId the preparation id.
     * @param action the action to add as a step.
     * @return the response.
     */
    public Response addAction(String preparationId, Action action) {
        return given() //
                .contentType(JSON) //
                .when() //
                .body(new ActionRequest(action)) //
                .post("/api/preparations/{preparationId}/actions", preparationId);
    }

    /**
     * Update an action within a preparation.
     *
     * @param preparationId the preparation id.
     * @param stepId the step to modify.
     * @param action the new parameters.
     * @return the response.
     */
    public Response updateAction(String preparationId, String stepId, Action action) {
        return given() //
                .contentType(JSON) //
                .when() //
                .body(new ActionRequest(action)) //
                .put("/api/preparations/{preparationId}/actions/{stepId}", preparationId, stepId);
    }

    /**
     * Move an action inside the prepration order.
     *
     * @param preparationId the preparation id.
     * @param stepId the step id.
     * @param parentStepId the wanted parent steo id.
     * @return the response.
     */
    public Response moveAction(String preparationId, String stepId, String parentStepId) {
        return given() //
                .contentType(JSON) //
                .when() //
                .queryParam(PARENT_STEP_ID, parentStepId)
                .post("/api/preparations/{preparationId}/steps/{stepId}/order", preparationId, stepId);
    }

    /**
     * Remove an action within a preparation.
     *
     * @param preparationId the preparation id.
     * @param actionId the id of the action to delete.
     * @return the response.
     */
    public Response deleteAction(String preparationId, String actionId) {
        return given() //
                .when() //
                .delete("/api/preparations/{preparationId}/actions/{actionId}", preparationId, actionId);
    }

    /**
     * Upload a text dataset into dataprep.
     *
     * @param filename the file to upload
     * @param datasetName the dataset basename
     * @return the response
     * @throws java.io.IOException if creation isn't possible
     */
    public Response uploadTextDataset(String filename, String datasetName) throws java.io.IOException {
        return given() //
                .header(new Header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")) //
                .body(IOUtils.toString(OSDataPrepAPIHelper.class.getResourceAsStream(filename),
                        Charset.defaultCharset())) //
                .queryParam(NAME, datasetName) //
                .when() //
                .post("/api/datasets");
    }

    /**
     * Upload a binary dataset into dataprep.
     *
     * @param filename the file to upload
     * @param datasetName the dataset basename
     * @return the response
     * @throws java.io.IOException if creation isn't possible
     */
    public Response uploadBinaryDataset(String filename, String datasetName) throws java.io.IOException {
        return given() //
                .header(new Header(HttpHeaders.CONTENT_TYPE, HTTP.PLAIN_TEXT_TYPE)) //
                .body(IOUtils.toByteArray(OSDataPrepAPIHelper.class.getResourceAsStream(filename))) //
                .when() //
                .queryParam(NAME, datasetName) //
                .post("/api/datasets");
    }

    /**
     * Update a existing dataset with current file
     *
     * @param datasetName the dataset name to update
     * @param filename the file to use to update the dataset
     * @return the response
     */
    public Response updateDataset(String filename, String datasetName, String datasetId) throws IOException {
        return given() //
                .header(new Header(HttpHeaders.CONTENT_TYPE, HTTP.PLAIN_TEXT_TYPE)) //
                .body(IOUtils.toString(OSDataPrepAPIHelper.class.getResourceAsStream(filename),
                        Charset.defaultCharset())) //
                .when() //
                .queryParam(NAME, datasetName) //
                .put("/api/datasets/{datasetId}", datasetId);
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     * @return the response
     */
    public Response deleteDataset(String dataSetId) {
        return given() //
                .when() //
                .delete("/api/datasets/" + dataSetId);
    }

    /**
     * List all dataset with basic meta information.
     *
     * @return the response.
     */
    public Response listDatasetDetails() {
        return given() //
                .when() //
                .get("/api/datasets/summary");
    }

    /**
     * Get preparation content by id and at a given version.
     *
     * @param preparationId the preparation id.
     * @param version version of the preparation
     * @param from Where to get the data from (HEAD if no value)
     * @param tql The TQL filter to apply (pass null if you want the non-filtered preparation content)
     * @return the response.
     */
    public Response getPreparationContent(String preparationId, String version, String from, String tql)
            throws IOException {
        RequestSpecification given = given() //
                .queryParam(VERSION, version) //
                .queryParam(FROM, from);
        if (tql != null) {
            given.queryParam("filter", tql);
        }
        Response response = given
                .when() //
                .get("/api/preparations/{preparationId}/content", preparationId);

        if (HttpStatus.ACCEPTED.value() == response.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = response.getHeader(HttpHeaders.LOCATION);

            waitForAsyncMethodToFinish(asyncMethodStatusUrl);

            response = given() //
                    .queryParam(VERSION, version) //
                    .queryParam(FROM, from) //
                    .queryParam("filter", tql) //
                    .when() //
                    .get("/api/preparations/{preparationId}/content", preparationId);
        }
        return response;
    }

    /**
     * List all preparations.
     *
     * @return the response.
     */
    public Response listAllPreparation() {
        return given() //
                .when() //
                .get("/api/folders/preparations");
    }

    /**
     * List all preparations in a specified folder.
     *
     * @param folder the folder where to search preparations.
     * @return the response.
     */
    public Response listPreparations(String folder) {
        return given() //
                .urlEncodingEnabled(false) //
                .when() //
                .get("/api/folders/{folder}/preparations", encode64(folder));
    }

    /**
     * Get a dataset content with filter.
     *
     * @param datasetId the dataset id.
     * @param tql the TQL filter to apply (pass null in order to get the non-filtered dataset content).
     * @return the response.
     */
    public Response getDataset(String datasetId, String tql) throws Exception {
        RequestSpecification given = given();
        if (tql != null) {
            given.queryParam("filter", tql);
        }
        given.queryParam("includeTechnicalProperties", "true");
        return given //
                .when() //
                .get("/api/datasets/{datasetId}", datasetId);
    }

    /**
     * Export the current preparation sample depending the given parameters.
     *
     * @param parameters the export parameters.
     * @return the response.
     */
    public Response executeExport(Map<String, String> parameters) throws IOException {
        Response response = given() //
                .contentType(JSON) //
                .when() //
                .queryParameters(parameters) //
                .get("/api/export");

        if (HttpStatus.ACCEPTED.value() == response.getStatusCode()) {
            // first time we have a 202 with a Location to see asynchronous method status
            final String asyncMethodStatusUrl = response.getHeader(HttpHeaders.LOCATION);

            waitForAsyncMethodToFinish(asyncMethodStatusUrl);

            response = given() //
                    .contentType(JSON) //
                    .when() //
                    .queryParameters(parameters) //
                    .get("/api/export");
        }
        return response;
    }

    /**
     * Get the default home folder.
     *
     * @return the home folder.
     */
    public String getHomeFolder() {
        return encode64("/");
    }

    /**
     * Delete a preparation identified by its id.
     *
     * @param preparationId the preparation id to delete.
     * @return the response.
     */
    public Response deletePreparation(String preparationId) {
        return given() //
                .when() //
                .delete("/api/preparations/{preparationId}", preparationId);
    }

    public Response getDataSetMetaData(String dataSetMetaDataId) {
        return given() //
                .when() //
                .get("/api/datasets/{dataSetMetaDataId}/metadata", dataSetMetaDataId);
    }

    /**
     * Store a given {@link InputStream} into a temporary {@link File} and store the {@link File} reference in IT
     * context.
     *
     * @param tempFilename the temporary {@link File} filename
     * @param input the {@link InputStream} to store.
     * @throws IOException in case of IO exception.
     */
    public File storeInputStreamAsTempFile(String tempFilename, InputStream input) throws IOException {
        Path path = Files.createTempFile(FilenameUtils.getBaseName(tempFilename),
                "." + FilenameUtils.getExtension(tempFilename));
        Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
        File tempFile = path.toFile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Create a new folder.
     *
     * @param parentFolderId the parent folder id.
     * @param folder the folder to create.
     * @return the response.
     */
    public Response createFolder(String parentFolderId, String folder) {
        return given() //
                .urlEncodingEnabled(false) //
                .queryParam(PARENT_ID, parentFolderId) //
                .queryParam(PATH, folder) //
                .when() //
                .put("/api/folders");
    }

    /**
     * Delete a new folder.
     *
     * @param folderId the folder id to delete.
     * @return the response.
     */
    public Response deleteFolder(String folderId) {
        return given() //
                .urlEncodingEnabled(false) // in case of OS call
                .when() //
                .delete("/api/folders/{folderId}", folderId);
    }

    /**
     * List existing folders.
     *
     * @return the response.
     */
    public Response listFolders() {
        return given() //
                .when() //
                .get("/api/folders");
    }

    /**
     * Move a preparation from a folder to another.
     *
     * @param prepId the preparation id.
     * @param folderSrc the preparation source folder.
     * @param folderDest the preparation destination folder.
     * @param prepName the new preparation name (can be the same as the original one).
     * @return the response.
     */
    public Response movePreparation(String prepId, String folderSrc, String folderDest, String prepName) {
        return given() //
                .urlEncodingEnabled(false) //
                .queryParam(FOLDER, folderSrc) //
                .queryParam(DESTINATION, folderDest) //
                .queryParam(NEW_NAME, prepName)
                .when() //
                .put("/api/preparations/{prepId}/move", prepId);
    }

    /**
     * Copy a preparation from a folder to another.
     *
     * @param id the preparation id.
     * @param folderDest the preparation destination folder.
     * @param prepName the new preparation name (can be the same as the original one).
     * @return the response.
     */
    public Response copyPreparation(String id, String folderDest, String prepName) {
        return given() //
                .contentType(JSON) //
                .when() //
                .urlEncodingEnabled(false) //
                .queryParam(NEW_NAME, prepName) //
                .queryParam(DESTINATION, folderDest) //
                .post("/api/preparations/{id}/copy", id);
    }

    /**
     * Get the semantic types of a column
     *
     * @param columnId the column id.
     * @param datasetId the new dataset name (can be the same as the original one).
     * @return the response.
     */
    public Response getDatasetsColumnSemanticTypes(String columnId, String datasetId) {
        return given() //
                .when() //
                .get("/api/datasets/{datasetId}/columns/{columnId}/types", datasetId, columnId);
    }

    /**
     * Get the semantic types of a column
     *
     * @param columnId the column id.
     * @param prepId the new preparation name (can be the same as the original one).
     * @return the response.
     */
    public Response getPreparationsColumnSemanticTypes(String columnId, String prepId) {
        return given() //
                .when() //
                .get("/api/preparations/{prepId}/columns/{columnId}/types", prepId, columnId);
    }

    /**
     * Get the user information.
     *
     * @return the response.
     */
    public Response getUserInformation() {
        return given() //
                .when() //
                .get("/api/user");
    }

    /**
     * Encode a {@link String} in 64 base.
     *
     * @param value the value to encode.
     * @return the encoded value.
     */
    public String encode64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    public Response getExportFormats(String preparationId) {
        return given() //
                .when() //
                .get("/api/export/formats/preparations/{preparationId}", preparationId);
    }

    /**
     * Return the list of datasets
     *
     * @param queryParameters Map containing the parameter names and their values to send with the request.
     * @return The response of the request.
     */
    public Response getDatasets(Map<String, String> queryParameters) {
        return given() //
                .when() //
                .queryParameters(queryParameters) //
                .get("/api/datasets");
    }

    /**
     * Return the list of datasets
     *
     * @param asyncMethodStatusUrl Map containing the parameter names and their values to send with the request.
     * @return The response of the request.
     */
    public AsyncExecutionMessage getAsyncResponse(String asyncMethodStatusUrl) throws IOException {
        return given() //
                .when() //
                .expect() //
                .statusCode(200) //
                .log() //
                .ifError() //
                .get(asyncMethodStatusUrl) //
                .as(AsyncExecutionMessage.class);

    }

    /**
     * Ping async method status url in order to wait the end of the execution
     *
     * @param asyncMethodStatusUrl the asynchronous method to ping.
     * @throws IOException
     */
    protected AsyncExecutionMessage waitForAsyncMethodToFinish(String asyncMethodStatusUrl) throws IOException {
        boolean isAsyncMethodRunning = true;
        int nbLoop = 0;

        AsyncExecutionMessage asyncExecutionMessage = null;

        while (isAsyncMethodRunning && nbLoop < 1000) {

            String statusAsyncMethod = given()
                    .when() //
                    .expect()
                    .statusCode(200)
                    .log()
                    .ifError() //
                    .get(asyncMethodStatusUrl)
                    .asString();

            asyncExecutionMessage = mapper.readerFor(AsyncExecutionMessage.class).readValue(statusAsyncMethod);

            AsyncExecution.Status asyncStatus = asyncExecutionMessage.getStatus();
            isAsyncMethodRunning = asyncStatus == RUNNING || asyncStatus == NEW;

            if (asyncStatus == FAILED) {
                LOGGER.error("AsyncExecution failed");
                Assert.fail("AsyncExecution failed");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Cannot Sleep", e);
                Assert.fail();
            }
            nbLoop++;
        }

        return asyncExecutionMessage;
    }

    public OSDataPrepAPIHelper setRestAssuredDebug(VerboseMode restAssuredDebug) {
        this.restAssuredDebug = restAssuredDebug;
        return this;
    }

    public Response applyAggragate(Aggregate aggregate) throws Exception {
        return given() //
                .header(new Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)) //
                .when() //
                .body(mapper.writeValueAsString(aggregate)) //
                .post("/api/aggregate");
    }

    public ITExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Description of the Integration Test execution context. This is useful to manage url changes between OnPremise &
     * Cloud context.
     */
    public enum ITExecutionContext {
        ON_PREMISE,
        CLOUD
    }
}
