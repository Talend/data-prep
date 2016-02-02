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

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;

import com.jayway.restassured.response.Response;

/**
 * Integration tests on actions.
 */
public class TransformTests extends TransformationServiceBaseTests {

    /** Content cache for the tests. */
    @Autowired
    private ContentCache contentCache;

    /** The dataprep ready to use jackson object builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Before
    public void customSetUp() throws Exception {
        contentCache.clear();
    }

    @Test
    public void noAction() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");

        // when
        String transformedContent = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON") //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("no_action_expected.json"));
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void testUnknownFormat() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "unknown format", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");

        // when
        final Response response = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", //
                        preparationId, //
                        dataSetId, //
                        "Gloubi-boulga"); // Casimir rules !

        // then
        Assert.assertEquals(500, response.getStatusCode());
        assertTrue(response.asString().contains("OUTPUT_TYPE_NOT_SUPPORTED"));
    }

    @Test
    public void testUnknownDataSet() throws Exception {
        // when
        final Response response = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", "no need for preparation id",
                        "unknown_dataset_id", "JSON");

        // then
        Assert.assertEquals(500, response.getStatusCode());
        assertTrue(response.asString().contains("UNABLE_TO_READ_DATASET_CONTENT"));
    }

    @Test
    public void testUnknownPreparation() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "no preparation for this one", "text/csv");

        // when
        final Response response = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", "no_preparation_id", dataSetId, "JSON");

        // then
        Assert.assertEquals(500, response.getStatusCode());
        assertTrue(response.asString().contains("UNABLE_TO_READ_PREPARATION"));
    }

    @Test
    public void uppercaseAction() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        applyActionFromFile(preparationId, "uppercase_action.json");

        // when
        String transformedContent = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON") //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("uppercase_expected.json"));
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void lowercaseActionWithFilter() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "lowercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "lowercase prep");
        applyActionFromFile(preparationId, "lowercase_filtered_action.json");

        // when
        String transformedContent = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON") //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("lowercase_filtered_expected.json"));
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void uppercaseActionWithSample() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        applyActionFromFile(preparationId, "uppercase_action.json");

        // when
        String transformedContent = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}?sample={sample}", preparationId, dataSetId,
                        "JSON", 6) //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("uppercase_expected_with_sample.json"));
        assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void testCache() throws Exception {
        // given
        String dsId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String prepId = createEmptyPreparationFromDataset(dsId, "uppercase prep");
        applyActionFromFile(prepId, "uppercase_action.json");

        String dataSetMetadataContent = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/datasets/{id}/metadata", dsId) //
                .asString();
        final DataSetMetadata metadata = builder.build().readerFor(DataSetMetadata.class).readValue(dataSetMetadataContent);

        final Preparation preparation = getPreparation(prepId);
        final String headId = preparation.getSteps().get(preparation.getSteps().size() - 1);

        TransformationCacheKey key = new TransformationCacheKey(prepId, metadata, "JSON", headId);
        assertFalse(contentCache.has(key));

        // when
        given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{prepId}/dataset/{datasetId}/{format}", prepId, dsId, "JSON") //
                .asString();

        // then
        assertTrue(contentCache.has(key));

        // just to pass through the cache
        final Response response = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{prepId}/dataset/{datasetId}/{format}", prepId, dsId, "JSON");
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void exportDataSet() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "my dataset", "text/csv");

        // when
        String exportContent = given() //
                .queryParam("name", "ds_export").expect().statusCode(200).log().ifError()//
                .when() //
                .get("/export/dataset/{id}/{format}", dataSetId, "JSON") //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("no_action_expected.json"));
        assertEquals(expectedContent, exportContent, false);
    }

    @Test
    public void actionFailure() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        applyActionFromFile(preparationId, "failed_action.json");

        // when
        String transformedContent = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON") //
                .asString();

        // then
        // Action failure
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("no_action_expected.json"));
        assertEquals(expectedContent, transformedContent, false);
    }

}
