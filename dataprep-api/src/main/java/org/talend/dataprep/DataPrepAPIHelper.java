package org.talend.dataprep;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class DataPrepAPIHelper {

    @Autowired
    protected ConfigurableEnvironment environment;

    private static RequestSpecification given() {
        return RestAssured.given().log().all(true);
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
                        .post(environment.getProperty("run.environment.url") + "/api/datasets?name=" + datasetName);
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
                delete(environment.getProperty("run.environment.url") + "/api/datasets/" + dataSetId);
    }

    /**
     * List all dataset in TDP instance.
     *
     * @return the response.
     */
    public Response getDatasetList() {
        return given().get(environment.getProperty("run.environment.url") + "/api/datasets/");
    }
}
