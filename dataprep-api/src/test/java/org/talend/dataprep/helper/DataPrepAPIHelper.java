package org.talend.dataprep.helper;

import static org.talend.dataprep.helper.utils.DataPrepWebInfo.API_ACTIONS;
import static org.talend.dataprep.helper.utils.DataPrepWebInfo.API_BACKEND_EXPORT;
import static org.talend.dataprep.helper.utils.DataPrepWebInfo.API_DATASETS;
import static org.talend.dataprep.helper.utils.DataPrepWebInfo.API_DETAILS;
import static org.talend.dataprep.helper.utils.DataPrepWebInfo.API_PREPARATIONS;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.objects.Action;
import org.talend.dataprep.helper.objects.ActionRequest;
import org.talend.dataprep.helper.objects.Parameters;
import org.talend.dataprep.helper.objects.PreparationRequest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Utility class to allow dataprep-api integration tests.
 */
@Component
public class DataPrepAPIHelper {


    @Value("${backend.global.api.url:http://localhost:8888}")
    private String globalApiBaseUrl;

    @Value("${backend.upload.api.url:http://localhost:8888}")
    private String uploadApiBaseUrl;

    @Value("${backend.export.api.url:http://localhost:8888}")
    private String exportApiBaseUrl;

    public RequestSpecification given() {
        return RestAssured.given().log().all(true);
    }

    /**
     * Create a preparation from a dataset and a home folder.
     *
     * @param datasetID       the dataset id to create the preparation from.
     * @param preparationName name for the new preparation.
     * @param homeFolderId    new preparation folder.
     * @return the response.
     */
    public Response createPreparation(String datasetID, String preparationName, String homeFolderId) {
        return given()
                .baseUri(globalApiBaseUrl)
                .contentType(ContentType.JSON)
                .when()
                .body(new PreparationRequest(datasetID, preparationName))
                .urlEncodingEnabled(false)
                .queryParam("folder", homeFolderId)
                .post(API_PREPARATIONS);
    }

    /**
     * Add an action to a preparation.
     *
     * @param preparationId the preparation Id.
     * @param actionName    the action name to add as a step.
     * @param columnName    the column name on which the action will be executed.
     * @param columnId      the column id on which the action will be executed.
     * @return the response.
     */
    public Response addStep(String preparationId, String actionName, String columnName, String columnId) {
        Parameters parameters = new Parameters(columnId, columnName, null, "column");
        Action action = new Action(actionName, parameters);
        List<Action> actions = new LinkedList<>();
        actions.add(action);
        ActionRequest actionRequest = new ActionRequest(actions);
        return given()
                .baseUri(globalApiBaseUrl)
                .contentType(ContentType.JSON)
                .when()
                .body(actionRequest)
                .post(API_PREPARATIONS + preparationId + "/" + API_ACTIONS);
    }

    /**
     * Upload a dataset into dataprep.
     *
     * @param filename    the file to upload
     * @param datasetName the dataset basename
     * @return the response
     * @throws java.io.IOException if creation isn't possible
     */
    public Response uploadDataset(String filename, String datasetName) throws java.io.IOException {
        Response response =
                given().header(new Header("Content-Type", "text/plain"))
                        .baseUri(uploadApiBaseUrl)
                        .body(this.getClass().getResourceAsStream(filename))
                        .when()
                        .queryParam("name", datasetName)
                        .post(API_DATASETS);
        return response;
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     * @return the response
     */
    public Response deleteDataSet(String dataSetId) {
        return given()
                .baseUri(globalApiBaseUrl)
                .when()
                .delete(API_DATASETS + dataSetId);
    }

    /**
     * List all dataset in TDP instance.
     *
     * @return the response.
     */
    public Response getDatasetList() {
        return given()
                .baseUri(globalApiBaseUrl)
                .get(API_DATASETS);
    }

    /**
     * Get a preparation as a list of step id.
     *
     * @param preparationId the preparation id.
     * @return the response.
     */
    public Response getPreparation(String preparationId) {
        return given()
                .baseUri(globalApiBaseUrl)
                .when()
                .get(API_PREPARATIONS + preparationId + "/" + API_DETAILS);
    }

    /**
     * Get a dataset.
     *
     * @param datasetId the dataset id.
     * @return the response.
     */
    public Response getDataset(String datasetId) {
        return given()
                .baseUri(globalApiBaseUrl)
                .when()
                .get(API_DATASETS + datasetId);
    }

    /**
     * Execute a preparation full run on a dataset followed by an export.
     *
     * @param exportType    export format.
     * @param datasetId     the dataset id on which the full run will be applied.
     * @param preparationId the full run preparation id.
     * @param stepId        the last step id.
     * @param delimiter     the column delimiter.
     * @param filename      the name for the exported generated file.
     * @return the response.
     */
    public Response executeFullRunExport(String exportType, String datasetId, String preparationId, String stepId, String delimiter, String filename) {
        return given()
                .baseUri(exportApiBaseUrl)
                .contentType(ContentType.JSON)
                .urlEncodingEnabled(false)
                .when()
                .queryParam("preparationId", preparationId)
                .queryParam("stepId", stepId)
                .queryParam("datasetId", datasetId)
                .queryParam("exportType", exportType)
                .queryParam("exportParameters.csv_fields_delimiter", delimiter)
                .queryParam("exportParameters.fileName", filename)
                .get(API_BACKEND_EXPORT);
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
        return given()
                .baseUri(globalApiBaseUrl)
                .when()
                .delete(API_PREPARATIONS + preparationId);
    }

    public Response getDataSetMetaData(String dataSetMetaDataId) {
        // @formatter:off
        return
            given()
                .baseUri(globalApiBaseUrl)
            .when()
                .get("/api/datasets/" + dataSetMetaDataId);
        // @formatter:on
    }

    public String getGlobalApiBaseUrl() {
        return globalApiBaseUrl;
    }

    public String getUploadApiBaseUrl() {
        return uploadApiBaseUrl;
    }

    public String getExportApiBaseUrl() {
        return exportApiBaseUrl;
    }
}
