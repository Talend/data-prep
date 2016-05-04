//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Unit test for Transformation API.
 */
public class TransformAPITest extends ApiServiceTestBase {


    @Test
    public void testTransformOneAction() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testDataset", "text/csv");
        applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/upper_case_firstname.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass()
                .getResourceAsStream("dataset/expected_dataset_firstname_uppercase.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testTransformTwoActions() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testDataset", "text/csv");
        applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/upper_case_lastname_firstname.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass()
                .getResourceAsStream("dataset/expected_dataset_lastname_firstname_uppercase.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_dataset() throws Exception {
        // given
        final String dataSetId = createDataset("transformation/cluster_dataset.csv", "testClustering", "text/csv");
        final String expectedClusterParameters = IOUtils
                .toString(this.getClass()
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
        final String expectedClusterParameters = IOUtils
                .toString(this.getClass()
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

        final String expectedClusterParameters = IOUtils
                .toString(this.getClass()
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
        final String preparationId = createPreparationFromFile("dataset/dataset_TDP-402.csv", "testDataset", "text/csv");
        applyAction(preparationId, IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-402.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass().getResourceAsStream("dataset/dataset_TDP-402_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    /**
     * see TDP-1308 (Replace all gender values with )
     */
    @Test
    public void shouldChangeTypeOnTransformation() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset_TDP-1308.csv", "testDataset", "text/csv");
        applyAction(preparationId, IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-1308.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass().getResourceAsStream("dataset/dataset_TDP-1308_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }


    @Test
    public void testMultipleParams() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset_TDP-402.csv", "testDataset", "text/csv");
        applyAction(preparationId, IOUtils.toString(this.getClass().getResourceAsStream("transformation/multiple_filters.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass().getResourceAsStream("transformation/multiple_filters_expected.json");

        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-714
     */
    @Test
    public void testCustomDateFormat_MMM_dd_yyyyTransformation() throws Exception {

        // given (a dataset with single date column)
        final String preparationId = createPreparationFromFile("dataset/TDP-714.csv", "dates", "text/csv");

        // when (change the date format to an unknown DQ pattern)
        applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/change_date_format_MMM_dd_yyyy.json")));

        // then (the column is still a date without any invalid)
        final String datasetContent = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId)
                .asString();

        final JsonNode rootNode = mapper.readTree(datasetContent);
        final DataSetMetadata metadata = mapper.readerFor(DataSetMetadata.class).readValue(rootNode.path("metadata"));

        assertThat(metadata.getRowMetadata().getColumns().isEmpty(), is(false));
        final ColumnMetadata column = metadata.getRowMetadata().getColumns().get(0);
        assertThat(column.getName(), is("date"));
        assertThat(column.getType(), is("date"));
        assertThat(column.getQuality().getInvalid(), is(0));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1012
     */
    @Test
    public void testCustomDateFormat_MMMM_yyyy_dd_Transformation() throws Exception {

        // given (a dataset with single date column)
        final String preparationId = createPreparationFromFile("dataset/TDP-714.csv", "dates", "text/csv");

        // when (change the date format to an unknown DQ pattern)
        applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/change_date_format_MMMM_yyyy_dd.json")));

        // then (the column is still a date without any invalid)
        final String datasetContent = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId)
                .asString();

        final JsonNode rootNode = mapper.readTree(datasetContent);
        final DataSetMetadata metadata = mapper.readerFor(DataSetMetadata.class).readValue(rootNode.path("metadata"));

        assertThat(metadata.getRowMetadata().getColumns().isEmpty(), is(false));
        final ColumnMetadata column = metadata.getRowMetadata().getColumns().get(0);
        assertThat(column.getName(), is("date"));
        assertThat(column.getType(), is("date"));
        assertThat(column.getStatistics().getHistogram().getItems().size(), is(12));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1624
     */
    @Test
    public void testCompareNumbersAfterSplit() throws Exception {

        // given (a dataset with single date column)
        final String preparationId = createPreparationFromFile("dataset/TDP-714.csv", "dates", "text/csv");

        // when (change the date format to an unknown DQ pattern)
        applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/split_compare_numbers.json")));

        // then (the column is still a date without any invalid)
        final String datasetContent = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId)
                .asString();

        // then
        final InputStream expectedContent = this.getClass().getResourceAsStream("transformation/split_compare_numbers_expected.json");

        assertThat(datasetContent, sameJSONAsFile(expectedContent));
    }

    /**
     * see TDP-1672 (Filter on values created by previous step)
     */
    @Test
    public void shouldFilterOnPreviouslyCreatedValues() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset_TDP-1672.csv", "testDataset", "text/csv");
        applyAction(preparationId, IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-1672.json")));

        // when
        final String transformed = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId).asString();

        // then
        final InputStream expectedContent = this.getClass().getResourceAsStream("dataset/dataset_TDP-1672_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

}
