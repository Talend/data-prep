package org.talend.dataprep.qa.api.feature;

import org.springframework.stereotype.Component;

@Component
public class Preparation extends TalendFeature {


    /**
     * @param name      the preparation's name.
     * @param dataSetId the dataset's id to use for the preparation.
     * @return the new preparation id.
     */
    public String createPreparation(String name, String dataSetId) {
//        Response response =
//                given().header(new Header("Content-Type", "text/plain"))
//                        // FIXME : this way of sending datasets through Strings limits the dataset size to the JVM available memory
//                        .body(IOUtils.toString(DatasetStory.class.getResourceAsStream(filename), true))
//                        .when()
//                        .post(environment.getProperty("run.environment.url") + "/api/datasets?name=" + datasetName);
//        response.then().
//                statusCode(200);
        return null;
    }
}
