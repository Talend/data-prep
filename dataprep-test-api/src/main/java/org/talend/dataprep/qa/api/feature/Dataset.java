package org.talend.dataprep.qa.api.feature;

import io.restassured.http.Header;
import io.restassured.response.Response;
import org.jbehave.core.io.IOUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.api.step.DatasetStory;

@Component
@Deprecated
public class Dataset extends TalendFeature {


    /**
     * Upload a dataset into dataprep.
     *
     * @param filename    the file to upload
     * @param datasetName the dataset basename
     * @return the newly created dataset id
     * @throws java.io.IOException if creation isn't possible
     */
    @Deprecated
    public String uploadDataset(String filename, String datasetName) throws java.io.IOException {
        Response response =
                given().header(new Header("Content-Type", "text/plain"))
                        // FIXME : this way of sending datasets through Strings limits the dataset size to the JVM available memory
                        .body(IOUtils.toString(DatasetStory.class.getResourceAsStream(filename), true))
                        .when()
                        .post(environment.getProperty("run.environment.url") + "/api/datasets?name=" + datasetName);
        response.then().
                statusCode(200);
        return IOUtils.toString(response.getBody().asInputStream(), true);
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     */
    @Deprecated
    public void deleteDataSet(String dataSetId) {
        given().
                when().
                delete(environment.getProperty("run.environment.url") + "/api/datasets/" + dataSetId).
                then().
                statusCode(200);
    }

}
