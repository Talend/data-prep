package org.talend.dataprep.helper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.objects.Action;
import org.talend.dataprep.helper.objects.ActionRequest;
import org.talend.dataprep.helper.objects.Parameters;
import org.talend.dataprep.helper.objects.PreparationRequest;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static org.talend.dataprep.helper.utils.DataPrepWebInfo.*;

@Component
public class DataPrepAPIHelper {

    private String baseUrl = "http://localhost:8888";

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

//    public Response getHomeFolder(){
//        return given()
//                .when()
//                .get("/api/user")
//                .jsonPath().getString("homeFolderId");
//    }
}
