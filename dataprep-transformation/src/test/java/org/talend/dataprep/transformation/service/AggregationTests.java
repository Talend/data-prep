package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jayway.restassured.response.Response;

/**
 * Integration tests for aggregation.
 */
public class AggregationTests extends TransformationServiceBaseTests {

    @Test
    public void invalidOperation() throws IOException {
        // given
        String invalidOperation = "hqsmkghfqg";

        // when
        final Response response = given()//
                .multiPart("parameters", invalidOperation)//
                .multiPart("content", "test to write")//
                .when().post("/aggregate");

        // then
        assertEquals(response.getStatusCode(), 400);
    }

    @Test
    public void shouldAggregateAverage() throws IOException {

        // when
        final String actual = aggregate("../aggregation/average.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/average_expected.json")));
    }

    @Test
    public void shouldAggregateMin() throws IOException {
        // when
        final String actual = aggregate("../aggregation/min.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/min_expected.json")));
    }

    @Test
    public void shouldAggregateMax() throws IOException {
        // when
        final String actual = aggregate("../aggregation/max.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/max_expected.json")));
    }

    @Test
    public void shouldAggregateSum() throws IOException {
        // when
        final String actual = aggregate("../aggregation/sum.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_expected.json")));
    }

    @Test
    public void shouldAggregateSumWithFilter() throws IOException {
        // when
        final String actual = aggregate("../aggregation/sum_filter.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_filter_expected.json")));
    }

    @Test
    public void shouldAggregateSumWithNullFilter() throws IOException {
        // when
        final String actual = aggregate("../aggregation/sum_filter_null.json", "../aggregation/aggregation_dataset_input.json");

        // then
        assertThat(actual, sameJSONAsFile(this.getClass().getResourceAsStream("../aggregation/sum_expected.json")));
    }


    private String aggregate(String action, String content) throws IOException {
        // given
        final String actions = IOUtils.toString(this.getClass().getResourceAsStream(action));
        String dataset = IOUtils.toString(this.getClass().getResourceAsStream(content));

        // when
        return given()//
                .multiPart("parameters", actions)//
                .multiPart("content", dataset)//
                .when().post("/aggregate")//
                .asString();
    }

}
