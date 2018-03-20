//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

import com.jayway.restassured.response.Response;

/**
 * Integration tests for aggregation.
 */
public class AggregationTest extends TransformationServiceBaseTest {

    @Test
    public void invalidOperation() throws IOException {
        // given
        String invalidOperation = "hqsmkghfqg";

        // when
        final Response response = given()//
                .contentType(APPLICATION_JSON_VALUE).body(invalidOperation)//
                .when().post("/aggregate");

        // then
        assertEquals(400, response.getStatusCode());
    }

    @Test
    public void shouldAggregateAverage() throws IOException {

        // when
        final String actual = aggregateFromDataSet("../aggregation/average.json", "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/average_expected.json")));
    }

    @Test
    public void shouldAggregateMin() throws IOException {
        // when
        final String actual = aggregateFromDataSet("../aggregation/min.json", "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/min_expected.json")));
    }

    @Test
    public void shouldAggregateMax() throws IOException {
        // when
        final String actual = aggregateFromDataSet("../aggregation/max.json", "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/max_expected.json")));
    }

    @Test
    public void shouldAggregateSum() throws IOException {
        // when
        final String actual = aggregateFromDataSet("../aggregation/sum.json", "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_expected.json")));
    }

    @Test
    public void shouldAggregateFromPreparation() throws IOException {
        // given
        final String datasetId = createDataset("../aggregation/aggregation_dataset.csv", "for a preparation", "text/csv");
        final String preparationId = createEmptyPreparationFromDataset(datasetId, "preparation");
        applyActionFromFile(preparationId, "../aggregation/uppercase_action.json");

        // when
        final String actionsAsJson = IOUtils.toString(this.getClass().getResourceAsStream("../aggregation/sum.json"),
                UTF_8);
        final AggregationParameters parameters = mapper.readerFor(AggregationParameters.class).readValue(actionsAsJson);
        parameters.setDatasetId(null);
        parameters.setPreparationId(preparationId);
        parameters.setStepId(null);

        String actual = given()//
                .body(mapper.writeValueAsString(parameters))//
                .contentType(APPLICATION_JSON_VALUE) //
                .when().post("/aggregate")//
                .asString();

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/uppercase_sum_expected.json")));
    }

    @Test
    public void shouldAggregateSumWithFilter() throws IOException {
        // when
        final String actual = aggregateFromDataSet("../aggregation/sum_filter.json", "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_filter_expected.json")));
    }

    @Test
    public void shouldAggregateSumWithNullFilter() throws IOException {
        // when
        final String actual = aggregateFromDataSet("../aggregation/sum_filter_null.json",
                "../aggregation/aggregation_dataset.csv");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_expected.json")));
    }


    private String aggregateFromDataSet(String actions, String content) throws IOException {

        // create the dataset
        final String datasetId = createDataset(content, "aggregateFromDataSet input " + Instant.now().getEpochSecond(),
                "text/csv");

        // update the actions
        final String actionsAsJson = IOUtils.toString(this.getClass().getResourceAsStream(actions), UTF_8);
        final AggregationParameters parameters = mapper.readerFor(AggregationParameters.class).readValue(actionsAsJson);
        parameters.setDatasetId(datasetId);
        parameters.setPreparationId(null);
        parameters.setStepId(null);

        return given()//
                .body(mapper.writer().writeValueAsString(parameters))//
                .contentType(APPLICATION_JSON_VALUE) //
                .when().expect().statusCode(200).log().ifError().post("/aggregate")//
                .asString();
    }

}
