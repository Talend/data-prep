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
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionRequest;
import org.talend.dataprep.helper.api.Parameters;
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
     * Add an action to a preparation.
     *
     * @param preparationId the preparation Id.
     * @param actionName the action name to add as a step.
     * @param columnName the column name on which the action will be executed.
     * @param columnId the column id on which the action will be executed.
     * @return the response.
     */
    public Response addStep(String preparationId, String actionName, String columnName, String columnId) {
        Parameters parameters = new Parameters(columnId, columnName, null, "column");
        List<Action> actions = Arrays.asList(new Action(actionName, parameters));
        ActionRequest actionRequest = new ActionRequest(actions);
        return given() //
                .baseUri(apiBaseUrl) //
                .contentType(JSON) //
                .when() //
                .body(actionRequest) //
                .post("/api/preparations/" + preparationId + "/actions");
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
    public Response deleteDataSet(String dataSetId) {
        return given() //
                .baseUri(apiBaseUrl) //
                .when() //
                .delete("/api/datasets/" + dataSetId);
    }

    /**
     * List all dataset in TDP instance.
     *
     * @return the response.
     */
    public Response getDatasetList() {
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
        return Base64.getEncoder().encodeToString("/".getBytes());
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
}
