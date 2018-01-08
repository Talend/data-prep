// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.cache.ContentCache.TimeToLive.PERMANENT;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataquality.semantic.broadcast.TdqCategories;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;

/**
 * Integration tests on actions.
 */
public class TransformationServiceTest extends TransformationServiceBaseTest {

    /**
     * Content cache for the tests.
     */
    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    PreparationRepository preparationRepository;

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
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("no_action_expected.json"),
                UTF_8);
        JSONAssert.assertEquals(expectedContent, transformedContent, false);
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
        assertEquals(415, response.getStatusCode());
        assertTrue(response.asString().contains("OUTPUT_TYPE_NOT_SUPPORTED"));
    }

    @Test
    public void testUnknownDataSet() throws Exception {
        // Given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");

        // when
        final Response response = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId,
                        "unknown_dataset_id", "JSON");

        // then
        assertEquals(400, response.getStatusCode());
        assertTrue(response.asString().contains("DATASET_DOES_NOT_EXIST"));
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
        assertEquals(500, response.getStatusCode());
        assertTrue(response.asString().contains("UNABLE_TO_READ_PREPARATION"));
    }

    @Test
    public void uppercaseAction() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase" + UUID.randomUUID().toString(), "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        applyActionFromFile(preparationId, "uppercase_action.json");
        applyActionFromFile(preparationId, "lowercase_filtered_action.json");

        // when
        String transformedContent = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON") //
                .asString();

        // then
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("uppercase_expected.json"), UTF_8);
        JSONAssert.assertEquals(expectedContent, transformedContent, false);
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
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("lowercase_filtered_expected.json"),
                UTF_8);
        JSONAssert.assertEquals(expectedContent, transformedContent, false);
    }

    @Test
    public void testCache() throws Exception {
        // given
        String dsId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String prepId = createEmptyPreparationFromDataset(dsId, "uppercase prep");
        applyActionFromFile(prepId, "uppercase_action.json");

        final Preparation preparation = getPreparation(prepId);
        final String headId = preparation.getHeadId();

        final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                dsId, //
                preparation.getId(), //
                headId, //
                JSON, //
                HEAD, //
                "" // no filter
        );
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
        Assert.assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void testEvictPreparationCache() throws Exception {
        // given
        final String preparationId = "prepId";
        final ContentCacheKey metadataKey = cacheKeyGenerator
                .metadataBuilder()
                .preparationId(preparationId)
                .stepId("step1")
                .sourceType(FILTER)
                .build();
        final ContentCacheKey contentKey = cacheKeyGenerator
                .contentBuilder()
                .datasetId("datasetId")
                .preparationId(preparationId)
                .stepId("step1")
                .format(JSON)
                .parameters(emptyMap())
                .sourceType(FILTER)
                .build();
        try (final OutputStream entry = contentCache.put(metadataKey, PERMANENT)) {
            entry.write("metadata".getBytes());
            entry.flush();
        }
        try (final OutputStream entry = contentCache.put(contentKey, PERMANENT)) {
            entry.write("content".getBytes());
            entry.flush();
        }

        assertThat(contentCache.has(metadataKey), is(true));
        assertThat(contentCache.has(contentKey), is(true));

        // when
        given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .delete("/preparation/{preparationId}/cache", preparationId) //
                .asString();

        // then
        assertThat(contentCache.has(metadataKey), is(false));
        assertThat(contentCache.has(contentKey), is(false));
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
        String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("no_action_expected.json"), UTF_8);
        JSONAssert.assertEquals(expectedContent, exportContent, false);
    }

    @Ignore // see TDP-3417
    @Test
    public void actionFailure() throws Exception {
        // given
        String dataSetId = createDataset("input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        applyActionFromFile(preparationId, "uppercase_action.json");
        applyActionFromFile(preparationId, "failed_transformation.json");

        // when
        final Response response = given() //
                .when() //
                .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", preparationId, dataSetId, "JSON");

        // then
        // (failed actions are ignored so the response should be 200)
        assertEquals(200, response.getStatusCode());
        // String result = '\'' + response.asString() + '\'';
        // System.out.println("\n\n\net voila le résultat de la transformation : " + result + "\n\n\n");

        // (make sure the result was not cached)
        final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                dataSetId, //
                preparationId, //
                preparationRepository.get(preparationId, Preparation.class).getHeadId(), //
                JSON, //
                HEAD, //
                "" // no filter
        );

        // Thread.sleep(500L);
        Assert.assertFalse("content cache '" + key.getKey() + "' was not evicted from the cache", contentCache.has(key));
    }

    @Test
    public void getDictionary() throws Exception {
        // when
        final InputStream dictionary = given() //
                .when() //
                .get("dictionary") //
                .asInputStream();

        // then
        final ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(dictionary));
        final Object object = ois.readObject();
        assertEquals(TdqCategories.class, object.getClass());
    }

    @Test
    public void shouldGetPreparationColumnTypes() throws Exception {

        // given
        final String dataSetId = createDataset("communes_france.csv", "communes de France", "text/csv");
        final String preparationId = createEmptyPreparationFromDataset(dataSetId, "get col types prep");

        // when
        final Response response = when().get("/preparations/{preparationId}/columns/{columnId}/types", preparationId, "0000");

        // then
        /*
         * expected response array of
         * {
         *   "id": "CITY",
         *   "label": "City",
         *   "frequency": 100.0
         * }
         */
        assertEquals(200, response.getStatusCode());
        final JsonNode rootNode = mapper.readTree(response.asInputStream());
        assertEquals(8, rootNode.size());
        for (JsonNode type : rootNode) {
            assertTrue(type.has("id"));
            assertTrue(type.has("label"));
            assertTrue(type.has("frequency"));
        }
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-3126
     */
    @Test
    public void shouldGetPreparationColumnTypesWhenDomainIsForced() throws Exception {

        // given
        final String dataSetId = createDataset("first_interactions_400.csv", "first interactions", "text/csv");
        final String preparationId = createEmptyPreparationFromDataset(dataSetId, "first interactions");
        // (force the domain to gender to replace all the 'F' by 'France')
        applyActionFromFile(preparationId, "change_domain.json");
        applyActionFromFile(preparationId, "replace_value.json");

        // when
        final Response response = when().get("/preparations/{preparationId}/columns/{columnId}/types", preparationId, "0005");

        // then
        assertEquals(200, response.getStatusCode());
        final JsonNode rootNode = mapper.readTree(response.asInputStream());
        assertEquals(5, rootNode.size());
        final List<String> actual = new ArrayList<>(5);
        rootNode.forEach(n -> actual.add(n.get("id").textValue().toUpperCase()));
        final List<String> expected = Arrays.asList("COUNTRY", "CIVILITY", "GENDER", "LAST_NAME", "FIRST_NAME");

        assertTrue(expected.containsAll(actual));
    }

    @Test
    public void shouldGetPreparationExportTypes() throws Exception {
        // given
        final String dataSetId = createDataset("communes_france.csv", "communes de France", "text/csv");
        final String preparationId = createEmptyPreparationFromDataset(dataSetId, "get col types prep");

        // when
        final Response preparationResponse = when().get("/export/formats/preparations/{preparationId}", preparationId);
        final Response dataSetResponse = when().get("/export/formats/datasets/{dataSetId}", dataSetId);

        // then
        assertEquals(200, preparationResponse.getStatusCode());
        assertEquals(200, dataSetResponse.getStatusCode());
        final JsonNode preparationResponseNode = mapper.readTree(preparationResponse.asInputStream());
        final JsonNode dataSetResponseNode = mapper.readTree(dataSetResponse.asInputStream());
        assertEquals(2, preparationResponseNode.size());
        assertEquals(2, dataSetResponseNode.size());
        assertEquals(preparationResponseNode, dataSetResponseNode);
    }
}
