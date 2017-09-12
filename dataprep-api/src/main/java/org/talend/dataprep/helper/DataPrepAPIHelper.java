package org.talend.dataprep.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.objects.*;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static org.talend.dataprep.helper.utils.DataPrepWebInfo.*;

@Component
public class DataPrepAPIHelper {

    private String baseUrl;

    private static RequestSpecification given() {
        return RestAssured.given().log().all(true);
    }

    /**
     * Get the current base URL.
     *
     * @return the current base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Set base URL.
     *
     * @param baseUrl the base URL to set.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
                .contentType(ContentType.JSON)
                .when()
                .body(new PreparationRequest(datasetID, preparationName))
                .urlEncodingEnabled(false)
//                .queryParam("folder", homeFolderId)
//                .post(API_PREPARATIONS);
                .post(API_PREPARATIONS_FOLDER + homeFolderId);
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
                        // FIXME : this way of sending datasets through Strings limits the dataset size to the JVM available memory
                        .body(IOUtils.toString(DataPrepAPIHelper.class.getResourceAsStream(filename), Charset.defaultCharset()))
                        .when()
                        .post(baseUrl + API_DATASETS_NAME + datasetName);
        return response;
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     * @return the response
     */
    public Response deleteDataSet(String dataSetId) {
        return given().
                when().
                delete(baseUrl + API_DATASETS + dataSetId);
    }

    /**
     * List all dataset in TDP instance.
     *
     * @return the response.
     */
    public Response getDatasetList() {
        return given().get(baseUrl + API_DATASETS);
    }

    /**
     * Get a preparation as a list of step id.
     *
     * @param preparationId the preparation id.
     * @return the response.
     */
    public Response getPreparation(String preparationId) {
        return given()
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
                .when()
                .get(API_DATASETS + datasetId);
    }

    /**
     * Execute a preparation full run on a dataset followed by an export.
     *
     * @param exportType    export format.
     * @param datasetId     the dataset id on which the full run will be applied.
     * @param preparationId the full run preparation id.
     * @param stepId        the last step id. TODO : verify this assertion
     * @param delimiter     the column delimiter.
     * @param filename      the name for the exported generated file.
     * @return the response.
     */
    public Response executeFullRunExport(String exportType, String datasetId, String preparationId, String stepId, String delimiter, String filename) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .body(new FullRunRequest(exportType, datasetId, preparationId, stepId, delimiter, filename))
                .post(API_FULLRUN_EXPORT);
    }

    /**
     * Get the full run export history of a preparation.
     *
     * @param preparationId the preparation id.
     * @return the response.
     */
    public Response getFullRunExportHistory(String preparationId) {
        return given()
                .when()
                .get(API_FULLRUN_EXPORT + "/" + preparationId + "/" + API_HISTORY);
    }


    /**
     * Get the default home folder.
     *
     * @return the home folder.
     */
    public String getHomeFolder() {
//        return given()
//                .when()
//                .get("/api/user")
//                .jsonPath().getString("homeFolderId");
        return "Lw==";
//        return "/";
    }

    /**
     * Delete a preparation identified by its id.
     *
     * @param preparationId the preparation id to delete.
     * @return the response.
     */
    public Response deletePreparation(String preparationId) {
        return given()
                .when()
                .delete(API_PREPARATIONS + preparationId);
    }
}
