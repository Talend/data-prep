package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Unit test for Transformation API.
 */
public class TransformAPITest extends ApiServiceTestBase {

    @Test
    public void testTransformNoOp() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        final String expectedContent = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json"));

        // when
        final String transformed = given().contentType(ContentType.JSON).body("").when().post("/api/transform/" + dataSetId)
                .asString();

        // then
        assertThat(transformed, sameJSONAs(expectedContent).allowingExtraUnexpectedFields());
    }

    @Test
    public void testTransformOneAction() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");

        final InputStream expectedContent = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase.json");
        final String actions = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname.json"));

        // when
        final String transformed = given().contentType(ContentType.JSON).body(actions).when().log().ifValidationFails()
                .post("/api/transform/" + dataSetId).asString();

        // then
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testTransformTwoActions() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "testDataset", "text/csv");
        final String actions = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/upper_case_lastname_firstname.json"));
        final InputStream expectedContent = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_lastname_firstname_uppercase.json");

        // when
        final String transformed = given().contentType(ContentType.JSON).body(actions).when().post("/api/transform/" + dataSetId)
                .asString();

        // then
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_dataset() throws Exception {
        // given
        final String dataSetId = createDataset("transformation/cluster_dataset.csv", "testClustering", "text/csv");
        final String expectedClusterParameters = IOUtils.toString(PreparationAPITest.class
                .getResourceAsStream("transformation/expected_cluster_params_soundex.json"));

        // when
        final String actualClusterParameters = given().formParam("datasetId", dataSetId).formParam("columnId", "0001")
                .when().get("/api/transform/suggest/textclustering/params").asString();

        // then
        assertThat(actualClusterParameters, sameJSONAs(expectedClusterParameters).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_preparation_head() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("transformation/cluster_dataset.csv", "testClustering", "text/csv");
        final String expectedClusterParameters = IOUtils.toString(PreparationAPITest.class
                .getResourceAsStream("transformation/expected_cluster_params_soundex.json"));

        // when
        final String actualClusterParameters = given().formParam("preparationId", preparationId)
                .formParam("columnId", "0001").when().get("/api/transform/suggest/textclustering/params").asString();

        // then
        assertThat(actualClusterParameters, sameJSONAs(expectedClusterParameters).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_preparation_step() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("transformation/cluster_dataset.csv", "testClustering", "text/csv");
        applyActionFromFile(preparationId, "export/upper_case_firstname.json");
        applyActionFromFile(preparationId, "export/upper_case_lastname.json");

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath()
                .getList("steps");

        final String expectedClusterParameters = IOUtils.toString(PreparationAPITest.class
                .getResourceAsStream("transformation/expected_cluster_params_with_steps.json"));

        // when
        final String actualClusterParameters = given()
                .formParam("preparationId", preparationId)
                .formParam("stepId", steps.get(1))
                .formParam("columnId", "0001")
                .when()
                .get("/api/transform/suggest/textclustering/params")
                .asString();

        // then (actions have normalized all cluster values, so no more clusters to be returned).
        assertThat(actualClusterParameters, sameJSONAs(expectedClusterParameters).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_400_with_no_preparationId_and_no_datasetId() throws Exception {
        // when
        final Response response = given().formParam("columnId", "0001").when()
                .get("/api/transform/suggest/textclustering/params");

        // then
        response.then().statusCode(400);
    }

    /**
     * see TDP-280 (text clustering parameters exceed url length limit)
     */
    @Test
    public void should_not_exceed_url_length_limit() throws Exception {

        // given
        final String preparationId = createPreparationFromFile("bugfix/TDP-280.csv", "cars", "text/csv");

        // parameters for text clustering are complicated and computed by the front. Since computing them is not
        // the point of this test, let's just get them from a file.
        final String actions = IOUtils.toString(this.getClass().getResourceAsStream("bugfix/TDP-280_action.json"));

        // when
        final int addActionResponseCode = given().contentType(ContentType.JSON).body(actions).when().post("/api/preparations/{id}/actions", preparationId).getStatusCode();

        // then
        assertEquals(200, addActionResponseCode);
        final String actualContent = given().get("/api/preparations/{id}/content?version=head&sample=100", preparationId).asString();
        assertThat(actualContent, sameJSONAsFile(this.getClass().getResourceAsStream("bugfix/TDP-280_expected.json")));
    }

    /**
     * see TDP-402 (Allow to adapt all dates to the selected pattern)
     */
    @Test
    public void should_use_all_date_patterns() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset_TDP-402.csv", "testDataset", "text/csv");
        final String actions = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/TDP-402.json"));
        final InputStream expectedContent = PreparationAPITest.class
                .getResourceAsStream("dataset/dataset_TDP-402_expected.json");

        // when
        final String transformed = given().contentType(ContentType.JSON).body(actions).when().post("/api/transform/" + dataSetId)
                .asString();

        // then
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testMultipleParams() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset_TDP-402.csv", "testDataset", "text/csv");
        final String actions = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/multiple_filters.json"));
        final InputStream expectedContent = PreparationAPITest.class
                .getResourceAsStream("transformation/multiple_filters_expected.json");

        // when
        final String transformed = given().contentType(ContentType.JSON).body(actions).when().post("/api/transform/" + dataSetId)
                .asString();

        // then
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

}
