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

package org.talend.dataprep.helper;

import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionRequest;
import org.talend.dataprep.helper.api.PreparationRequest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Utility class to allow dataprep-api integration tests.
 */
@Component
public class DataPrepAPIHelper {

    @Value("${backend.api.url:http://localhost:8888}")
    private String apiBaseUrl;

    @Value("${restassured.debug:false}")
    private boolean enableRestAssuredDebug;

    /**
     * Wraps the {@link RestAssured#given()} method so that we can add behavior
     *
     * @return the request specification to use.
     */
    public RequestSpecification given() {
        RequestSpecification given = RestAssured.given();
        if (enableRestAssuredDebug) {
            given = given.log().all(true);
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
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                .when() //
                .body(new PreparationRequest(datasetID, preparationName)) //
                .urlEncodingEnabled(false) //
                .queryParam("folder", homeFolderId) //
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
                .baseUri(apiBaseUrl) //
                .when() //
                .get("/api/preparations/" + preparationId + "/details");
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
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                .when() //
                .body(new ActionRequest(action)) //
                .post("/api/preparations/" + preparationId + "/actions");
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
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                .when() //
                .body(new ActionRequest(action)) //
                .put("/api/preparations/" + preparationId + "/actions/" + stepId);
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
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                .when() //
                .post("/api/preparations/" + preparationId + "/steps/" + stepId + "/order?parentStepId=" + parentStepId);
    }

    /**
     * Upload a dataset into dataprep.
     *
     * @param filename the file to upload
     * @param datasetName the dataset basename
     * @return the response
     * @throws java.io.IOException if creation isn't possible
     */
    public Response uploadDataset(String filename, String datasetName) throws java.io.IOException {
        return given() //
                .header(new Header("Content-Type", "text/plain")) //
                .baseUri(apiBaseUrl) //
                .body(IOUtils.toString(DataPrepAPIHelper.class.getResourceAsStream(filename), Charset.defaultCharset())).when() //
                .queryParam("name", datasetName) //
                .post("/api/datasets");
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     * @return the response
     */
    public Response deleteDataset(String dataSetId) {
        return given() //
                .baseUri(apiBaseUrl) //
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
                .baseUri(apiBaseUrl) //
                .when() //
                .get("api/datasets/summary");
    }

    /**
     * List all dataset in TDP instance.
     *
     * @return the response.
     */
    public Response listDataset() {
        return given() //
                .baseUri(apiBaseUrl) //
                .get("/api/datasets");
    }

    /**
     * Get a preparation as a list of step id.
     *
     * @param preparationId the preparation id.
     * @return the response.
     */
    public Response getPreparation(String preparationId) {
        return given() //
                .baseUri(apiBaseUrl) //
                .when() //
                .get("/api/preparations/" + preparationId + "/details");
    }

    /**
     * List all preparations.
     *
     * @return the response.
     */
    public Response listAllPreparation() {
        return given() //
                .baseUri(apiBaseUrl) //
                .when() //
                .get("/api/folders/preparations");
    }

    /**
     * List all preparations in a specified folder.
     *
     * @param folder the folder where to search preparations.
     * @return the response.
     */
    public Response listPreparation(String folder) {
        return given() //
                .baseUri(apiBaseUrl) //
                .urlEncodingEnabled(false) //
                .when() //
                .get("/api/folders/" + encode64(folder) + "/preparations");
    }

    /**
     * Get a dataset.
     *
     * @param datasetId the dataset id.
     * @return the response.
     */
    public Response getDataset(String datasetId) {
        return given() //
                .baseUri(apiBaseUrl) //
                .when() //
                .get("/api/datasets/" + datasetId);
    }

    /**
     * Execute a preparation full run on a dataset followed by an export.
     *
     * @param exportType export format.
     * @param datasetId the dataset id on which the full run will be applied.
     * @param preparationId the full run preparation id.
     * @param stepId the last step id.
     * @param delimiter the column delimiter.
     * @param filename the name for the exported generated file.
     * @return the response.
     */
    public Response executeFullRunExport(String exportType, String datasetId, String preparationId, String stepId,
            String delimiter, String filename) {
        return given() //
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                // .header("Content-Type", "application/json; charset=utf8") //
                .urlEncodingEnabled(false) //
                .when() //
                .queryParam("preparationId", preparationId) //
                .queryParam("stepId", stepId) //
                .queryParam("datasetId", datasetId) //
                .queryParam("exportType", exportType) //
                .queryParam("exportParameters.csv_fields_delimiter", delimiter) //
                .queryParam("exportParameters.fileName", filename) //
                .get("/api/export");
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
                .baseUri(apiBaseUrl) //
                .when() //
                .delete("/api/preparations/" + preparationId);
    }

    public Response getDataSetMetaData(String dataSetMetaDataId) {
        return given() //
                .baseUri(apiBaseUrl) //
                .when() //
                .get("/api/datasets/" + dataSetMetaDataId + "/metadata");
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * Store a given {@link InputStream} into a temporary {@link File} and store the {@link File} reference in IT context.
     *
     * @param tempFilename the temporary {@link File} filename
     * @param input the {@link InputStream} to store.
     * @throws IOException in case of IO exception.
     */
    public File storeInputStreamAsTempFile(String tempFilename, InputStream input) throws IOException {
        Path path = Files.createTempFile(FilenameUtils.getBaseName(tempFilename), "." + FilenameUtils.getExtension(tempFilename));
        File tempFile = path.toFile();
        FileOutputStream fos = new FileOutputStream(path.toFile());
        IOUtils.copy(input, fos);
        fos.close();
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Create a new folder.
     *
     * @param parentFolder the parent folder.
     * @param folder the folder to create.
     * @return the response.
     */
    public Response createFolder(String parentFolder, String folder) {
        return given() //
                .baseUri(apiBaseUrl) //
                .urlEncodingEnabled(false) //
                .when() //
                .put("/api/folders?parentId=" + encode64(parentFolder) + "&path=" + folder);
    }

    /**
     * Delete a new folder.
     *
     * @param folder the folder to delete (without the "/" at the beginning).
     * @return the response.
     */
    public Response deleteFolder(String folder) {
        return given() //
                .baseUri(apiBaseUrl) //
                .urlEncodingEnabled(false) //
                .when() //
                .delete("/api/folders/" + encode64(folder));
    }

    /**
     * List existing folders.
     *
     * @return the response.
     */
    public Response listFolders() {
        return given() //
                .baseUri(apiBaseUrl) //
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
                .baseUri(apiBaseUrl) //
                .urlEncodingEnabled(false) //
                .when() //
                .put("/api/preparations/" + prepId //
                        + "/move?folder=" + encode64(folderSrc) //
                        + "&destination=" + encode64(folderDest) //
                        + "&newName=" + prepName);
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
}
