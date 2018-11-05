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

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.api.service.test.APIClientTest.ActionParameters.createAction;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.COLUMN;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.store.Directory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.test.APIClientTest;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataquality.semantic.broadcast.TdqCategories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Unit test for Transformation API.
 */
public class TransformAPITest extends ApiServiceTestBase {

    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test asynchronous preparation transformation
     */
    @Test
    public void first_transformation_should_be_async() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-402.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-402.json"), UTF_8));

        // when
        Response transformedResponse = given()
                .when() //
                .expect()
                .statusCode(202)
                .log()
                .ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId);

        // first time we have a 202 with a Location to see asynchronous method status
        Assert.assertEquals(HttpStatus.ACCEPTED.value(), transformedResponse.getStatusCode());
        final String asyncMethodStatusUrl = transformedResponse.getHeader("Location");

        Assert.assertNotNull(asyncMethodStatusUrl);

        boolean isAsyncMethodRunning = true;
        int nbLoop = 0;

        while (isAsyncMethodRunning && nbLoop < 100) {

            String statusAsyncMethod = given()
                    .when() //
                    .expect()
                    .statusCode(200)
                    .log()
                    .ifError() //
                    .get(asyncMethodStatusUrl)
                    .asString();

            AsyncExecutionMessage asyncExecutionMessage =
                    mapper.readerFor(AsyncExecutionMessage.class).readValue(statusAsyncMethod);

            isAsyncMethodRunning = asyncExecutionMessage.getStatus().equals(AsyncExecution.Status.RUNNING);

            Thread.sleep(50);
            nbLoop++;
        }

        // second time should be a 200
        transformedResponse = given()
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .get("/api/preparations/{id}/content?version=head", preparationId);

        Assert.assertEquals(HttpStatus.OK.value(), transformedResponse.getStatusCode());

    }

    @Test
    public void testTransformOneAction() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId, IOUtils
                .toString(this.getClass().getResourceAsStream("transformation/upper_case_firstname.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("dataset/expected_dataset_firstname_uppercase.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testTransform_datasetWithNoWordPatternAnalysisShouldBeExportedWithThem() throws Exception {
        // given
        String dataset_4404 = testClient.createDataset("dataset/dataset.csv", "dataset_4404");

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataset_4404);
        assert dataSetMetadata != null;
        dataSetMetadata.getRowMetadata().getColumns().forEach(
                c -> c.getStatistics().setWordPatternFrequencyTable(new LinkedList<>()));
        dataSetMetadataRepository.save(dataSetMetadata);

        final String preparationId = testClient.createPreparationFromDataset(dataset_4404, "testPrep", home.getId());
        testClient.applyAction(preparationId, createAction("uppercase").withColumnId("0001").withScope(COLUMN));

        // when
        final APIClientTest.PreparationExport transformed = testClient.getPreparationAsObject(preparationId);

        // then
        for (ColumnMetadata c : transformed.metadata.getRowMetadata().getColumns()) {
            assertThat(c.getStatistics().getWordPatternFrequencyTable(), is(not(empty())));
        }
    }

    @Test
    public void testTransformTwoActions() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "another test Dataset", home.getId());
        testClient.applyAction(preparationId, IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/upper_case_lastname_firstname.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("dataset/expected_dataset_lastname_firstname_uppercase.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_dataset() throws Exception {
        // given
        final String dataSetId = testClient.createDataset("transformation/cluster_dataset.csv", "testClustering");
        final String expectedClusterParameters = IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/expected_cluster_params_double_metaphone.json"),
                UTF_8);

        // when
        final String actualClusterParameters = given()
                .formParam("datasetId", dataSetId)
                .formParam("columnId", "0001")
                .when()
                .get("/api/transform/suggest/textclustering/params")
                .asString();

        // then
        assertThat(actualClusterParameters, sameJSONAs(expectedClusterParameters).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_preparation_head() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("transformation/cluster_dataset.csv",
                "testClustering", home.getId());
        final String expectedClusterParameters = IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/expected_cluster_params_double_metaphone.json"),
                UTF_8);

        // update cache preparation
        testClient.getPreparation(preparationId);

        // when
        final String actualClusterParameters = given()
                .formParam("preparationId", preparationId)
                .formParam("columnId", "0001")
                .when()
                .get("/api/transform/suggest/textclustering/params")
                .asString();

        // then
        assertThat(actualClusterParameters, sameJSONAs(expectedClusterParameters).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_dynamic_params_with_preparation_step() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("transformation/cluster_dataset.csv",
                "testClustering", home.getId());

        testClient.applyAction(preparationId,
                createAction("uppercase")
                        .withParameter("column_id", "0002")
                        .withParameter("column_name", "firstname")
                        .withParameter("scope", "column"));

        testClient.applyAction(preparationId,
                createAction("uppercase")
                        .withParameter("column_id", "0003")
                        .withParameter("column_name", "lastname")
                        .withParameter("scope", "column"));

        final List<String> steps =
                given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");

        // when
        final String actualClusterParameters = given()
                .param("preparationId", preparationId)
                .param("stepId", steps.get(1))
                .param("columnId", "0001")
                .when()
                .get("/api/transform/suggest/textclustering/params")
                .asString();

        // then (actions have normalized all cluster values, so no more clusters to be returned).
        assertFalse(actualClusterParameters.isEmpty());
        String expectedJson = IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/expected_cluster_params_double_metaphone.json"),
                UTF_8);
        assertThat(actualClusterParameters, sameJSONAs(expectedJson).allowingAnyArrayOrdering());
    }

    @Test
    public void testSuggestActionParams_should_return_400_with_no_preparationId_and_no_datasetId() throws Exception {
        // when
        final Response response =
                given().formParam("columnId", "0001").when().get("/api/transform/suggest/textclustering/params");

        // then
        response.then().statusCode(400);
    }

    /**
     * see TDP-280 (text clustering parameters exceed url length limit)
     */
    @Test
    public void should_not_exceed_url_length_limit() throws Exception {

        // given
        final String preparationId = testClient.createPreparationFromFile("bugfix/TDP-280.csv", "cars", home.getId());

        // parameters for text clustering are complicated and computed by the front. Since computing them is not
        // the point of this test, let's just get them from a file.
        final String actions =
                IOUtils.toString(this.getClass().getResourceAsStream("bugfix/TDP-280_action.json"), UTF_8);

        // when
        final int addActionResponseCode = given()
                .contentType(ContentType.JSON)
                .body(actions)
                .when()
                .post("/api/preparations/{id}/actions", preparationId)
                .getStatusCode();

        // then
        assertEquals(200, addActionResponseCode);
        final String actualContent = testClient.getPreparation(preparationId).asString();
        assertThat(actualContent, sameJSONAsFile(this.getClass().getResourceAsStream("bugfix/TDP-280_expected.json")));
    }

    /**
     * see TDP-402 (Allow to adapt all dates to the selected pattern)
     */
    @Test
    public void should_use_all_date_patterns() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-402.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-402.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("dataset/dataset_TDP-402_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    /**
     * see TDP-1308 (Replace all gender values with )
     */
    @Test
    public void shouldChangeTypeOnTransformation() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-1308.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-1308.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();
        // then
        this.getClass().getResourceAsStream("dataset/dataset_TDP-1308_expected.json");
        assertFalse(transformed.isEmpty());
    }

    @Test
    public void testMultipleParams() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-402.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/multiple_filters.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("transformation/multiple_filters_expected.json");

        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-714
     */
    @Test
    public void testCustomDateFormat_MMM_dd_yyyyTransformation() throws Exception {

        // given (a dataset with single date column)
        final String preparationId = testClient.createPreparationFromFile("dataset/TDP-714.csv", "dates", home.getId());

        // when (change the date format to an unknown DQ pattern)
        testClient.applyAction(preparationId, IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/change_date_format_MMM_dd_yyyy.json"), UTF_8));

        // then (the column is still a date without any invalid)
        final String datasetContent = testClient.getPreparation(preparationId).asString();

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
        final String preparationId = testClient.createPreparationFromFile("dataset/TDP-714.csv", "dates", home.getId());

        // when (change the date format to an unknown DQ pattern)
        testClient.applyAction(preparationId, IOUtils.toString(
                this.getClass().getResourceAsStream("transformation/change_date_format_MMMM_yyyy_dd.json"), UTF_8));

        // then (the column is still a date without any invalid)
        final String datasetContent = testClient.getPreparation(preparationId).asString();

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
        final String preparationId = testClient.createPreparationFromFile("dataset/TDP-714.csv", "dates", home.getId());

        // when (change the date format to an unknown DQ pattern)
        testClient.applyAction(preparationId, IOUtils
                .toString(this.getClass().getResourceAsStream("transformation/split_compare_numbers.json"), UTF_8));

        // then (the column is still a date without any invalid)
        final String datasetContent = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("transformation/split_compare_numbers_expected.json");

        assertThat(datasetContent, sameJSONAsFile(expectedContent));
    }

    /**
     * see TDP-1672 (Filter on values created by previous step)
     */
    @Test
    public void shouldFilterOnPreviouslyCreatedValues() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-1672.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-1672.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("dataset/dataset_TDP-1672_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    /**
     * see TDP-2165 (Forced type is applied on wrong column)
     */
    @Test
    public void typeChangeShouldOnlyImpactTargetedColumn() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-2165.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-2165.json"), UTF_8));

        // when
        final String transformed = testClient.getPreparation(preparationId).asString();

        // then
        final InputStream expectedContent =
                this.getClass().getResourceAsStream("dataset/dataset_TDP-2165_expected.json");
        assertThat(transformed, sameJSONAsFile(expectedContent));
    }

    @Test
    public void retrieveDictionary() throws Exception {
        // when
        final InputStream dictionary = given()
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .get("/api/transform/dictionary")
                .asInputStream();

        // then
        final ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(dictionary));
        final Object object = ois.readObject();
        Assert.assertEquals(TdqCategories.class, object.getClass());

        final TdqCategories serviceDictionary = (TdqCategories) object;
        final Directory dictionaryDirectory = serviceDictionary.getDictionary().asDirectory(); // Test Lucene directory
                                                                                               // creation.
        assertNotNull(dictionaryDirectory);
        final Directory keywordDirectory = serviceDictionary.getKeyword().asDirectory(); // Test Lucene directory
                                                                                         // creation.
        assertNotNull(keywordDirectory);
    }

    @Test
    public void testShouldEvictPreparationCacheOnDataSetUpdate() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset_TDP-2165.csv", "testDataset", home.getId());
        testClient.applyAction(preparationId,
                IOUtils.toString(this.getClass().getResourceAsStream("transformation/TDP-2165.json"), UTF_8));

        testClient.getPreparation(preparationId);

        final Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        final TransformationCacheKey transformationCacheKey =
                cacheKeyGenerator.generateContentKey(preparation.getDataSetId(), //
                        preparationId, //
                        preparation.getHeadId(), //
                        "JSON", //
                        HEAD, //
                        "" // no filter
                );
        assertTrue(contentCache.has(transformationCacheKey));

        // when
        context.publishEvent(new DatasetUpdatedEvent(dataSetMetadataRepository.get(preparation.getDataSetId())));

        // then
        assertFalse(contentCache.has(transformationCacheKey));
    }
}
