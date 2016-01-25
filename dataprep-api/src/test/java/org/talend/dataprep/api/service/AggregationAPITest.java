package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;

import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Common Unit test for the Aggregation API.
 */
public class AggregationAPITest extends ApiServiceTestBase {


    @Test
    public void should_not_aggregate_because_dataset_and_preparation_id_are_missing() throws IOException {

        // given
        AggregationParameters params = getAggregationParameters("aggregation/aggregation_parameters.json");
        params.setDatasetId(null);
        params.setPreparationId(null);

        // when
        final Response response = given().contentType(ContentType.JSON)//
                .body(builder.build().writer().writeValueAsString(params))//
                .when()//
                .post("/api/aggregate");

        // then
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void should_aggregate_on_dataset() throws IOException {

        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "tagada", "text/csv");

        AggregationParameters params = getAggregationParameters("aggregation/aggregation_parameters.json");
        params.setDatasetId(dataSetId);
        params.setPreparationId(null);
        params.setStepId(null);

        // when
        final String response = given().contentType(ContentType.JSON)//
                .body(builder.build().writer().writeValueAsString(params))//
                .when()//
                .post("/api/aggregate").asString();

        // then
        assertThat(response, sameJSONAsFile(this.getClass().getResourceAsStream("aggregation/aggregation_expected.json")));
    }

    @Test
    public void should_aggregate_on_preparation() throws IOException {

        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet", "text/csv");

        AggregationParameters params = getAggregationParameters("aggregation/aggregation_parameters.json");
        params.setDatasetId(null);
        params.setPreparationId(preparationId);
        params.setStepId(null);

        // when
        final String response = given().contentType(ContentType.JSON)//
                .body(builder.build().writer().writeValueAsString(params))//
                .when()//
                .post("/api/aggregate").asString();

        // then
        assertThat(response, sameJSONAsFile(this.getClass().getResourceAsStream("aggregation/aggregation_expected.json")));
    }

    @Test
    public void should_not_aggregate_because_dataset_and_preparation_id_are_set() throws IOException {

        // given
        AggregationParameters params = getAggregationParameters("aggregation/aggregation_parameters.json");
        params.setDatasetId("ds#753874");

        // when
        final Response response = given().contentType(ContentType.JSON)//
                .body(builder.build().writer().writeValueAsString(params))//
                .when()//
                .post("/api/aggregate");

        // then
        assertEquals(400, response.getStatusCode());
    }
}
