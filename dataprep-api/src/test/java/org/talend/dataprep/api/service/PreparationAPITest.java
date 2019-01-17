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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.api.service.EntityBuilder.buildAction;
import static org.talend.dataprep.api.service.EntityBuilder.buildParametersMap;
import static org.talend.dataprep.api.service.PreparationAPITestClient.appendStepsToPrep;
import static org.talend.dataprep.api.service.PreparationAPITestClient.changePreparationStepsOrder;
import static org.talend.dataprep.cache.ContentCache.TimeToLive.PERMANENT;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.DATASET_DOES_NOT_EXIST;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.FOLDER_DOES_NOT_EXIST;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_DOES_NOT_EXIST;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.MixedContentMap;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationListItemDTO;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.PreviewAddParameters;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.exception.TdpExceptionDto;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.schema.csv.CSVFormatFamily;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.actions.date.ComputeTimeSince;
import org.talend.dataprep.transformation.actions.text.Trim;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import au.com.bytecode.opencsv.CSVReader;

public class PreparationAPITest extends ApiServiceTestBase {

    @Autowired
    private Security security;

    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------GETTER-------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void testEmptyPreparationList() {
        assertThat(when().get("/api/preparations").asString(), sameJSONAs("[]"));
        assertThat(when().get("/api/preparations/?format=short").asString(), sameJSONAs("[]"));
        assertThat(when().get("/api/preparations/?format=long").asString(), sameJSONAs("[]"));
    }

    @Test
    public void testPreparationsList() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when : short format
        final JsonPath shortFormat = when().get("/api/preparations/?format=short").jsonPath();

        // then
        final List<String> values = shortFormat.getList("id");
        assertThat(values.get(0), is(preparationId));

        // when : long format
        Response response1 = when().get("/api/preparations/?format=long");

        // then
        List<PreparationListItemDTO> preparations = mapper
                .readerFor(PreparationListItemDTO.class)
                .<PreparationListItemDTO> readValues(response1.asInputStream())
                .readAll();
        assertEquals(1, preparations.size());
        PreparationListItemDTO userPreparation = preparations.iterator().next();
        assertThat(userPreparation.getDataSet().getDataSetName(), is("tagada"));
        assertThat(userPreparation.getId(), is(preparationId));

        // when : summary format
        Response response = when().get("/api/preparations/?format=summary");

        // then
        List<PreparationListItemDTO> preparationSummaries = mapper
                .readerFor(PreparationListItemDTO.class)
                .<PreparationListItemDTO> readValues(response.asInputStream())
                .readAll();
        assertEquals(1, preparationSummaries.size());
        PreparationListItemDTO preparationSummary = preparationSummaries.iterator().next();
        assertThat(preparationSummary.getId(), is(preparationId));
        assertThat(preparationSummary.getName(), is("testPreparation"));
        assertThat(preparationSummary.getLastModificationDate(), is(notNullValue()));
    }

    @Test
    public void testPreparationsGet() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when : long format
        Response response1 = when().get("/api/preparations/{preparationId}/details", preparationId);

        // then
        PreparationDTO userPreparation = mapper.readerFor(PreparationDTO.class).readValue(response1.asInputStream());
        assertThat(userPreparation.getDataSetId(), is(tagadaId));
        assertThat(userPreparation.getAuthor(), is(security.getUserId()));
        assertThat(userPreparation.getId(), is(preparationId));
    }

    @Test
    public void testPreparationsList_withFilterOnName() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when : short format
        final JsonPath shortFormat =
                when().get("/api/preparations/?format=short&name={name}", "testPreparation").jsonPath();

        // then
        final List<String> values = shortFormat.getList("id");
        assertThat(values.get(0), is(preparationId));
    }

    @Test
    public void testPreparationsList_withFilterOnFolderPath() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationName = "tagadaPreparation";
        String preparationId = testClient.createPreparationFromDataset(tagadaId, preparationName, home.getId());

        // when : short format
        final Response shouldNotBeEmpty = when().get("/api/preparations/?format=short&folder_path={folder_path}", "/");

        // then
        List<PreparationDTO> result = mapper //
                .readerFor(PreparationDTO.class) //
                .<PreparationDTO> readValues(shouldNotBeEmpty.asInputStream()) //
                .readAll();
        assertThat(result.get(0).getId(), is(preparationId));

        // when
        final JsonPath shouldBeEmpty =
                when().get("/api/preparations/?format=short&folder_path={folder_path}", "/toto").jsonPath();

        // then
        assertThat(shouldBeEmpty.<String> getList("id"), is(empty()));
    }

    @Test
    public void testPreparationsList_withFilterOnFullPath() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationName = "tagadaPreparation";
        String preparationId = testClient.createPreparationFromDataset(tagadaId, preparationName, home.getId());

        // when : short format
        final JsonPath shouldNotBeEmpty =
                when().get("/api/preparations/?format=short&path={path}", "/" + preparationName).jsonPath();

        // then
        assertThat(shouldNotBeEmpty.<String> getList("id").get(0), is(preparationId));

        // when
        final JsonPath shouldBeEmpty =
                when().get("/api/preparations/?format=short&path={path}", "/toto/" + preparationName).jsonPath();

        // then
        assertThat(shouldBeEmpty.<String> getList("id"), is(empty()));
    }

    @Test
    public void testPreparationGet() throws Exception {
        // when
        final String datasetId = testClient.createDataset("dataset/dataset.csv", "great dataset");
        final String preparationId = testClient.createPreparationFromDataset(datasetId, "1234", home.getId());

        // then
        final JsonPath longFormat = given().get("/api/preparations/{id}/details", preparationId).jsonPath();
        assertThat(longFormat.getString("dataSetId"), is(datasetId));
        assertThat(longFormat.getString("author"), is(security.getUserId()));
        assertThat(longFormat.getString("id"), is(preparationId));
        assertThat(longFormat.getList("actions").size(), is(0));

        assertThat(longFormat.getString("allowFullRun"), is("false"));
        final List<String> steps = longFormat.getList("steps"); // make sure the "steps" node is a string array
        assertThat(steps.size(), is(1));
    }

    @Test
    public void shouldCopyPreparation() throws Exception {
        // given
        Folder destination = folderRepository.addFolder(home.getId(), "/destination");
        Folder origin = folderRepository.addFolder(home.getId(), "/from");
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "super preparation", origin.getId());

        // when
        String newPreparationName = "NEW super preparation";
        final Response response = given() //
                .queryParam("destination", destination.getId()) //
                .queryParam("newName", newPreparationName) //
                .when()//
                .expect() //
                .statusCode(200) //
                .log() //
                .ifError() //
                .post("api/preparations/{id}/copy", preparationId);

        // then
        assertEquals(200, response.getStatusCode());
        String copyId = response.asString();

        // check the folder entry
        final List<FolderEntry> entries = getEntries(destination.getId());
        assertThat(entries.size(), greaterThan(0));
        final FolderEntry entry = entries.get(0);
        assertEquals(entry.getContentId(), copyId);

        // check the name
        final Preparation actual = preparationRepository.get(copyId, Preparation.class);
        assertEquals(newPreparationName, actual.getName());
    }

    private List<FolderEntry> getEntries(String folderId) {
        try (final Stream<FolderEntry> entriesStream = folderRepository.entries(folderId, PREPARATION)) {
            return entriesStream.collect(toList());
        }
    }

    @Test
    public void copyPreparationShouldForwardExceptions() {

        // when
        final Response response = given() //
                .queryParam("destination", "/destination") //
                .when() //
                .expect() //
                .statusCode(404) //
                .log() //
                .ifError() //
                .post("api/preparations/{id}/copy", "preparation_not_found");

        // then
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void shouldMovePreparation() throws Exception {
        // given
        final Folder source = folderRepository.addFolder(home.getId(), "source");
        final String id =
                testClient.createPreparationFromFile("dataset/dataset.csv", "great_preparation", source.getId());

        final Folder destination = folderRepository.addFolder(home.getId(), "destination");

        // when
        final Response response = given() //
                .queryParam("folder", source.getId()) //
                .queryParam("destination", destination.getId()) //
                .queryParam("newName", "NEW great preparation") //
                .when()//
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .put("api/preparations/{id}/move", id);

        // then
        assertEquals(200, response.getStatusCode());

        // check the folder entry
        final List<FolderEntry> entries = getEntries(destination.getId());
        assertThat(entries.size(), greaterThan(0));
        final FolderEntry entry = entries.get(0);
        assertEquals(entry.getContentId(), id);

        // check the name
        final Preparation actual = preparationRepository.get(id, Preparation.class);
        assertEquals("NEW great preparation", actual.getName());
    }

    @Test
    public void movePreparationShouldForwardExceptions() {

        // when
        final Response response = given() //
                .queryParam("folder", "/from") //
                .queryParam("destination", "/to") //
                .queryParam("newName", "NEW great preparation") //
                .when()//
                .expect()
                .statusCode(404)
                .log()
                .ifError() //
                .put("api/preparations/{id}/move", "unknown_preparation");

        // then
        assertEquals(404, response.getStatusCode());
    }

    /**
     * @see <a href="https://jira.talendforge.org/browse/TDP-3965">TDP-3965</a>
     */
    @Test
    public void ensureThatPreparationDetailsCanBeParsedAsStandalonePreparation_TDP_3965() throws Exception {
        // when
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");
        InputStream inputStream = given() //
                .expect() //
                .statusCode(200) //
                .get("/api/preparations/{preparation}/details", preparationId)
                .asInputStream();

        // then
        try {
            mapper.readerFor(StandalonePreparation.class).readValue(inputStream);
        } catch (Exception e) {
            fail();
        }
    }

    // ------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------LIFECYCLE-----------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void testPreparationUpdate() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        final String preparationId = testClient.createPreparationFromDataset(tagadaId, "original_name", home.getId());

        JsonPath longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is("original_name"));
        assertThat(longFormat.getList("id").size(), is(1));
        assertThat(longFormat.getList("id").get(0), is(preparationId));

        PreparationDTO preparation = new PreparationDTO();
        preparation.setId(preparationId);
        String newName = "updated_name";
        preparation.setName(newName);

        // when
        given().contentType(ContentType.JSON).body(preparation).put("/api/preparations/{id}", preparationId).asString();

        // then
        longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is(newName));
    }

    @Test
    public void testPreparationDelete() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        final String preparationId = testClient.createPreparationFromDataset(tagadaId, "original_name", home.getId());

        String list = when().get("/api/preparations").asString();
        assertTrue(list.contains(preparationId));

        // when
        when().delete("/api/preparations/" + preparationId).asString();

        // then
        list = when().get("/api/preparations").asString();
        assertEquals("[]", list);
    }

    @Test
    public void testPreparationCacheDeletion() throws Exception {
        // given

        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        final String preparationId = testClient.createPreparationFromDataset(tagadaId, "original_name", home.getId());

        final String list = when().get("/api/preparations").asString();
        assertThat(list.contains(preparationId), is(true));

        final ContentCacheKey metadataKey = cacheKeyGenerator //
                .metadataBuilder() //
                .preparationId(preparationId) //
                .stepId("step1") //
                .sourceType(FILTER) //
                .build();
        final ContentCacheKey contentKey = cacheKeyGenerator //
                .contentBuilder() //
                .datasetId("datasetId") //
                .preparationId(preparationId) //
                .stepId("step1") //
                .format(JSON) //
                .parameters(emptyMap()) //
                .sourceType(FILTER) //
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
        when().delete("/api/preparations/" + preparationId).asString();

        // then
        Assert.assertThat(contentCache.has(metadataKey), is(false));
        Assert.assertThat(contentCache.has(contentKey), is(false));
    }

    // ------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------STEPS-------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_append_action_after_actual_head() throws Exception {
        // when
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        final List<String> steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void shouldAddMultipleActionStepAfterHead() throws Exception {
        /// when: 1 AppendStep with 2 actions
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname_firstname.json");

        // then : it should have appended 2 actions
        final PreparationDTO preparationMessage = getPreparationDetails(preparationId);
        final List<String> steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    private PreparationDTO getPreparationDetails(String preparationId) throws IOException {
        return mapper.readValue(given().get("/api/preparations/{preparation}/details", preparationId).asInputStream(),
                PreparationDTO.class);
    }

    @Test
    public void should_fail_properly_on_append_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(
                PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"),
                UTF_8);
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        final Response request = given() //
                .contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .post("/api/preparations/{id}/actions", preparationId);

        // then
        request
                .then()//
                .statusCode(400)//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    @Test
    public void should_update_action() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59")
        // with
        // another action
        final String actionContent3 = IOUtils.toString(
                PreparationAPITest.class.getResourceAsStream("transformation/lower_case_lastname.json"), UTF_8);
        given()
                .contentType(ContentType.JSON)
                .body(actionContent3)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId, steps.get(1))
                .then()
                .statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void should_fail_properly_on_update_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(
                PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"),
                UTF_8);
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = getPreparationDetails(preparationId).getSteps();
        final String firstStep = steps.get(1);

        // when
        final Response request = given()
                .contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .put("/api/preparations/{id}/actions/{step}", preparationId, firstStep);

        // then
        request
                .then()//
                .statusCode(400)//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    @Test
    public void should_delete_preparation_action() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparationDetails(preparationId).getSteps();
        final String firstStep = steps.get(1);

        // when
        given()
                .delete("/api/preparations/{preparation}/actions/{step}", preparationId, firstStep) //
                .then() //
                .statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void should_throw_error_when_preparation_does_not_exist_on_delete() {
        // when : delete unknown preparation action
        final Response response =
                given().delete("/api/preparations/{preparation}/actions/{action}", "unknown_prep", "unkown_step");

        // then : should have preparation service error
        response.then().statusCode(is(404)).body("code", is("TDP_PS_PREPARATION_DOES_NOT_EXIST"));
    }

    @Test
    public void should_change_preparation_head() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        final String newHead = preparationRepository.get(preparation.getHeadId(), Step.class).getParent();

        // when
        given()
                .when()//
                .put("/api/preparations/{id}/head/{stepId}", preparationId, newHead)//
                .then()//
                .statusCode(200);

        // then
        preparation = preparationRepository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(newHead));
    }

    @Test
    public void should_throw_exception_on_preparation_head_change_with_unknown_step() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        final String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when
        final Response response = given()
                .when()//
                .put("/api/preparations/{id}/head/{stepId}", preparationId, "unknown_step_id");

        // then
        response
                .then()//
                .statusCode(404)//
                .assertThat()//
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void shouldCopySteps() throws Exception {
        // given
        final String referenceId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "reference", home.getId());
        testClient.applyActionFromFile(referenceId, "transformation/upper_case_firstname.json");
        Preparation reference = preparationRepository.get(referenceId, Preparation.class);

        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "prep", home.getId());

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .when()//
                .put("/api/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        assertNotEquals(reference.getHeadId(), preparation.getHeadId());
        assertEquals(preparation.getSteps().size(), reference.getSteps().size());
        assertThat(preparation.getSteps().get(0).getId(), is(Step.ROOT_STEP.id()));
        assertEquals(preparation.getSteps().get(1).getContent(), reference.getSteps().get(1).getContent());
    }

    // ------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------CONTENT------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------------------

    @Test
    public void shouldCreatePreparationInDefaultFolder() throws Exception {

        // given
        Folder home = folderRepository.getHome();
        List<FolderEntry> entries = getEntries(home.getId());
        assertTrue(entries.isEmpty());

        // when
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", home.getId());

        // then
        entries = getEntries(home.getId());
        assertThat(entries.size(), is(1));
        final FolderEntry entry = entries.get(0);
        assertThat(entry.getContentId(), is(preparationId));
        assertThat(entry.getContentType(), is(PREPARATION));
    }

    @Test
    public void shouldCreatePreparationInSpecificFolder() throws Exception {

        // given
        final String path = "/folder-1/sub-folder-2";
        Folder folder = folderRepository.addFolder(folderRepository.getHome().getId(), path);
        List<FolderEntry> entries = getEntries(folder.getId());
        assertThat(entries.size(), is(0));

        // when
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", folder.getId());

        // then
        entries = getEntries(folder.getId());
        assertThat(entries.size(), is(1));
        final FolderEntry entry = entries.get(0);
        assertThat(entry.getContentId(), is(preparationId));
        assertThat(entry.getContentType(), is(PREPARATION));
    }

    @Test
    public void shouldNotAcceptPreparationWithoutRowMetadata() throws Exception {
        // given
        Folder home = folderRepository.getHome();
        final List<FolderEntry> entries = getEntries(home.getId());
        assertThat(entries.size(), is(0));
        String dataSetId = testClient.createDataset("dataset/dataset.csv", "testCreatePreparation");

        given() //
                .contentType(ContentType.JSON) //
                .body("{ \"name\": \"" + "my_preparation" + "\", \"dataSetId\": \"" + dataSetId + "\"}")
                .queryParam("folder", home.getId()) //
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .post("/api/preparations");
    }

    @Test
    public void testPreparationInitialContent() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet", home.getId());

        final InputStream expected =
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json");

        // when
        final String content = testClient.getPreparation(preparationId).asString();

        // then
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testPreparationFilteredInitialContent() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv",
                "testPreparationContentGetWithFilter", home.getId());

        final InputStream expected =
                PreparationAPITest.class.getResourceAsStream("dataset/expected_filtered_dataset_with_columns.json");

        // when
        final String content = testClient.getPreparationWithFilter(preparationId, "0001 = 'John'").asString();

        // then
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testPreparationWithMalformedFilterShouldFail() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv",
                "testPreparationContentGetWithMalformedFilter", home.getId());

        // when
        Response response = testClient.getFailedPreparationWithFilter(preparationId, "malformed filter");

        // then
        AsyncExecutionMessage asyncExecutionMessage =
                mapper.readerFor(AsyncExecutionMessage.class).readValue(response.asString());

        assertEquals(AsyncExecution.Status.FAILED, asyncExecutionMessage.getStatus());
        assertEquals(BaseErrorCodes.UNABLE_TO_PARSE_FILTER.getCode(), asyncExecutionMessage.getError().getCode());
    }

    @Test
    public void testPreparationInitialMetadata() throws Exception {
        // given
        final String preparationName = "testPreparationContentGet";
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", preparationName, home.getId());

        // when
        final DataSetMetadata actual = testClient.getPrepMetadata(preparationId);

        // then
        assertNotNull(actual);
        final List<ColumnMetadata> columns = actual.getRowMetadata().getColumns();
        assertEquals(6, columns.size());
        final List<String> expectedColumns =
                Arrays.asList("id", "firstname", "lastname", "age", "date-of-birth", "alive");
        for (ColumnMetadata column : columns) {
            assertTrue(expectedColumns.contains(column.getName()));
        }
    }

    @Test
    public void testPreparationInitialMetadata_wordPatternStats() throws Exception {
        // given
        final String preparationName = "testPreparationContentGet";

        String patternsAsCsvLine = new BufferedReader(new InputStreamReader(
                PreparationAPITest.class.getResourceAsStream("dataset/TDP-4404_data_for_word_pattern_recognition.txt"),
                UTF_8)).lines().collect(Collectors.joining(";"));

        String datasetId = testClient.createDataset(new ByteArrayInputStream(patternsAsCsvLine.getBytes(UTF_8)),
                new MediaType("text", "csv", UTF_8), "test-" + UUID.randomUUID());
        DataSetMetadata dataSetMetadata = testClient.getDataSetMetadata(datasetId);
        dataSetMetadata.getContent().getParameters().put(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        testClient.setDataSetMetadata(dataSetMetadata);

        final String preparationId = testClient.createPreparationFromDataset(datasetId, preparationName, home.getId());
        String expectedWordPatterns = IOUtils.toString(PreparationAPITest.class
                .getResourceAsStream("dataset/TDP-4404_data_for_word_pattern_recognition_result.txt"), UTF_8);

        // when
        final DataSetMetadata actual = testClient.getPrepMetadata(preparationId);

        // then
        assertNotNull(actual);
        final List<ColumnMetadata> columns = actual.getRowMetadata().getColumns();

        assertEquals(29, columns.size());

        String actualWordPatterns = columns
                .stream()
                .map(c -> c
                        .getStatistics() //
                        .getWordPatternFrequencyTable() //
                        .iterator() //
                        .next() //
                        .getPattern()) //
                .collect(Collectors.joining("\n")) + "\n"; // because every file ends with a new line (can't fight autoformat)

        assertEquals(expectedWordPatterns, actualWordPatterns);
    }

    @Test
    public void testPreparationContentWithActions() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet", home.getId());
        PreparationDTO preparation = getPreparationDetails(preparationId);
        List<String> steps = preparation.getSteps();

        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        final PreparationDTO preparationMessage = getPreparationDetails(preparationId);
        steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // Request preparation content at different versions (preparation has 2 steps -> Root + Upper Case).
        assertThat(testClient.getPreparation(preparationId).asString(), sameJSONAsFile(PreparationAPITest.class
                .getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));

        assertThat(testClient.getPreparation(preparationId, "head").asString(), sameJSONAsFile(PreparationAPITest.class
                .getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));

        assertThat(testClient.getPreparation(preparationId, steps.get(0)).asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));

        assertThat(testClient.getPreparation(preparationId, steps.get(1)).asString(),
                sameJSONAsFile(PreparationAPITest.class
                        .getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));

        assertThat(testClient.getPreparation(preparationId, "origin").asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));

        assertThat(testClient.getPreparation(preparationId, Step.ROOT_STEP.id()).asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
    }

    @Test
    public void shouldGetPreparationContent() throws IOException {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet", home.getId());

        // when
        String preparationContent = testClient.getPreparation(preparationId).asString();

        // then
        assertThat(preparationContent,
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv.expected.json")));
    }

    @Test
    public void shouldGetPreparationContentWhenInvalidSample() throws IOException {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet", home.getId());

        // when
        String preparationContent = testClient.getPreparation(preparationId).asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(preparationContent);
        JsonNode records = rootNode.get("records");
        assertThat(records.size(), is(100));
    }

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------PREVIEW------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void testPreparationDiffPreview() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview", home.getId());
        testClient.applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "preview/delete_city.json");

        final List<String> steps = getPreparationDetails(preparationId).getSteps();
        final String firstActionStep = steps.get(1);
        final String lastStep = steps.get(steps.size() - 1);

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"currentStepId\": \"" + firstActionStep + "\",\n" // action 1
                + "   \"previewStepId\": \"" + lastStep + "\",\n" // action 1 + 2 + 3
                + "   \"tdpIds\": [2, 4, 6]" //
                + "}";

        final InputStream expectedDiffStream =
                PreparationAPITest.class.getResourceAsStream("preview/expected_diff_preview.json");

        // when
        final String diff = given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/api/preparations/preview/diff")
                .asString();

        // then
        assertThat(diff, sameJSONAsFile(expectedDiffStream));
    }

    @Test
    public void testPreparationUpdatePreview() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview", home.getId());
        testClient.applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "preview/delete_city.json");

        final PreparationDTO preparationMessage = getPreparationDetails(preparationId);
        final List<String> steps = preparationMessage.getSteps();
        final String lastStep = steps.get(steps.size() - 1);

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"currentStepId\": \"" + lastStep + "\",\n" // action 1 + 2 + 3
                + "   \"updateStepId\": \"" + lastStep + "\",\n" // action 3
                + "   \"tdpIds\": [2, 4, 6]," //
                + "   \"action\": {" //
                + "       \"action\": \"delete_on_value\",\n"//
                + "       \"parameters\": {" //
                + "           \"column_id\": \"0006\"," //
                + "           \"value\": {\"token\": \"Coast city\", \"operator\": \"equals\"},"//
                + "           \"scope\": \"column\""//
                + "       }" //
                + "   }"//
                + "}";

        final InputStream expectedDiffStream =
                PreparationAPITest.class.getResourceAsStream("preview/expected_update_preview.json");

        // when
        final String diff = given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/api/preparations/preview/update")
                .asString();

        // then
        assertThat(diff, sameJSONAsFile(expectedDiffStream));
    }

    @Test
    public void testPreparationAddPreviewOnPreparation() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview", home.getId());
        testClient.applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "preview/delete_city.json");

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": [{\n" + "         \"action\": \"uppercase\",\n" + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n" + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n" + "         }\n" //
                + "    }]\n" //
                + "}";
        final InputStream expectedPreviewStream = getClass().getResourceAsStream("preview/expected_add_preview.json");

        // when
        final String preview = given() //
                .contentType(ContentType.JSON) //
                .body(input) //
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .post("/api/preparations/preview/add") //
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
    }

    /**
     * Verify a calculate time since preview after a trim step on a preparation
     * see <a href="https://jira.talendforge.org/browse/TDP-5057">TDP-5057</a>
     */
    @Test
    public void testPreparationPreviewOnPreparationWithTrimAction_TDP_5057() throws IOException {
        // Create a dataset from csv
        final String datasetId = testClient.createDataset("preview/best_sad_songs_of_all_time.csv", "testPreview");
        // Create a preparation
        String preparationId = testClient.createPreparationFromDataset(datasetId, "testPrep", home.getId());

        // apply trim action on the 8nd column to make this column date valid
        Map<String, String> trimParameters = new HashMap<>();
        trimParameters.put("create_new_column", "false");
        trimParameters.put("padding_character", "whitespace");
        trimParameters.put("scope", "column");
        trimParameters.put("column_id", "0008");
        trimParameters.put("column_name", "Added At");
        trimParameters.put("row_id", "null");

        testClient.applyAction(preparationId, Trim.TRIM_ACTION_NAME, trimParameters);

        // check column is date valid after trim action
        InputStream inputStream = testClient.getPreparation(preparationId).asInputStream();
        mapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        RowMetadata preparationContent = mapper.readValue(inputStream, Data.class).metadata;

        List<PatternFrequency> patternFrequencies =
                preparationContent.getColumns().get(8).getStatistics().getPatternFrequencies();

        assertTrue(patternFrequencies
                .stream() //
                .map(PatternFrequency::getPattern) //
                .anyMatch("yyyy-MM-dd"::equals));

        // create a preview of calculate time since action
        PreviewAddParameters previewAddParameters = new PreviewAddParameters();
        previewAddParameters.setDatasetId(datasetId);
        previewAddParameters.setPreparationId(preparationId);
        previewAddParameters.setTdpIds(Arrays.asList(1, 2, 3, 4, 5, 6, 7));

        Action calculateTimeUntilAction = new Action();
        calculateTimeUntilAction.setName(ComputeTimeSince.TIME_SINCE_ACTION_NAME);
        MixedContentMap actionParameters = new MixedContentMap();
        actionParameters.put("create_new_column", "true");
        actionParameters.put("time_unit", "HOURS");
        actionParameters.put("since_when", "now_server_side");
        actionParameters.put("scope", "column");
        actionParameters.put("column_id", "0008");
        actionParameters.put("column_name", "Added At");
        calculateTimeUntilAction.setParameters(actionParameters);
        previewAddParameters.setActions(Collections.singletonList(calculateTimeUntilAction));

        JsonPath jsonPath = given()
                .contentType(ContentType.JSON) //
                .body(previewAddParameters) //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .when() //
                .post("/api/preparations/preview/add") //
                .jsonPath();

        // check non empty value for the new column
        assertEquals("new preview column should contains values according to calculate time since action", //
                7, //
                jsonPath.getList("records.0009").stream().map(String::valueOf).filter(StringUtils::isBlank).count());

    }

    @Test
    public void testPreparationAddPreviewOnDataset() throws Exception {
        // given
        final String datasetId = testClient.createDataset("preview/preview_dataset.csv", "testPreview");

        final String input = "{" //
                + "   \"datasetId\": \"" + datasetId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": [{\n" + "         \"action\": \"uppercase\",\n" + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n" + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n" + "         }\n" //
                + "    }]\n" //
                + "}";
        final InputStream expectedPreviewStream =
                PreparationAPITest.class.getResourceAsStream("preview/expected_add_preview_on_dataset.json");

        // when
        final String preview = given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/api/preparations/preview/add")
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
    }

    @Test
    public void testPreparationMultipleAddPreview() throws Exception {
        // given
        final String preparationId =
                testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview", home.getId());
        testClient.applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "preview/delete_city.json");

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": [" + "         {\n" + "             \"action\": \"uppercase\",\n"
                + "             \"parameters\": {\n" + "                 \"column_id\": \"0005\",\n"
                + "                 \"column_name\": \"alive\"\n," + "                 \"scope\": \"column\"\n"
                + "             }\n" //
                + "         },\n" + "         {\n" + "             \"action\": \"uppercase\",\n"
                + "             \"parameters\": {\n" + "                 \"column_id\": \"0006\",\n"
                + "                 \"column_name\": \"city\"\n," + "                 \"scope\": \"column\"\n"
                + "             }\n" //
                + "         }\n" + "    ]\n" //
                + "}";
        final InputStream expectedPreviewStream =
                getClass().getResourceAsStream("preview/expected_multi_add_preview.json");

        // when
        final String preview = given() //
                .contentType(ContentType.JSON) //
                .body(input) //
                .when() //
                .expect()
                .statusCode(200)
                .log()
                .ifError() //
                .post("/api/preparations/preview/add") //
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
    }

    @Test
    public void testMoveStep() throws Exception {
        final String datasetId = testClient.createDataset("preview/preview_dataset.csv", "testPreview");

        String testPrepId = testClient.createPreparationFromDataset(datasetId, "testPrep", home.getId());

        AppendStep appendStep = new AppendStep();
        final Action actionToBeMoved = buildAction("uppercase",
                buildParametersMap("column_id", "0001", "column_name", "firstname", "scope", "column"));
        appendStep.setActions(Arrays.asList(
                buildAction("uppercase",
                        buildParametersMap("column_id", "0002", "column_name", "lastname", "scope", "column")),
                actionToBeMoved));
        appendStepsToPrep(testPrepId, appendStep);

        // Adding steps
        PreparationDTO testPrepDetails = getPreparationDetails(testPrepId);

        List<String> stepsCreated = testPrepDetails.getSteps();

        String rootStep = stepsCreated.get(0);
        String secondStep = stepsCreated.get(2);

        // changing steps order
        changePreparationStepsOrder(testPrepId, rootStep, secondStep);

        PreparationDTO testPrepDetailsAfter = getPreparationDetails(testPrepId);

        final Action initialUpperCaseAction = getActions(testPrepDetails.getSteps().get(2)).get(1);
        assertEquals(actionToBeMoved, initialUpperCaseAction);
        final Action movedAction = getActions(testPrepDetailsAfter.getSteps().get(1)).get(0);
        assertEquals(initialUpperCaseAction, movedAction);
    }

    private List<Action> getActions(String id) {
        final String content = preparationRepository.get(id, PersistentStep.class).getContent();
        final PreparationActions preparationActions = preparationRepository.get(content, PreparationActions.class);
        return preparationActions.getActions();
    }

    /**
     * Verify a data set is not locked when used by a step that is not used in any preparation.
     * see <a href="https://jira.talendforge.org/browse/TDP-2562">TDP-2562</a>
     */
    @Test
    public void testSetPreparationHead_TDP_2562() throws Exception {
        // Three data sets
        final String lookupDataSetId = testClient.createDataset("dataset/dataset.csv", "lookup_ds");
        final String dataSetId = testClient.createDataset("dataset/dataset_cars.csv", "cars");

        String carsPreparationId = testClient.createPreparationFromDataset(dataSetId, "cars_preparation", home.getId());

        String action = IOUtils.toString(getClass().getResource("preparations/cars_lookup_action.json"), UTF_8);
        testClient.applyAction(carsPreparationId, action.replace("{lookup_ds_id}", lookupDataSetId));

        // Try to delete lookup dataset => fail because used
        expect().statusCode(CONFLICT.value()).when().delete("/api/datasets/{id}", lookupDataSetId);

        PreparationDTO preparationDetails = getPreparationDetails(carsPreparationId);
        String firstStepId = preparationDetails.getSteps().get(0);

        // Now undo
        expect().statusCode(OK.value()).when().put("/api/preparations/{id}/head/{headId}", carsPreparationId,
                firstStepId);

        // Try again to delete lookup dataset
        expect().statusCode(OK.value()).when().get("/api/datasets/{id}", lookupDataSetId);
    }

    @Test
    public void shouldGetPreparationColumnTypes() throws Exception {

        // given
        final String id =
                testClient.createPreparationFromFile("dataset/dataset.csv", "super preparation", home.getId());

        // when
        final Response response = when().get("/api/preparations/{preparationId}/columns/{columnId}/types", id, "0000");

        // then
        Assert.assertEquals(200, response.getStatusCode());
        final JsonNode rootNode = mapper.readTree(response.asInputStream());
        for (JsonNode type : rootNode) {
            assertTrue(type.has("id"));
            assertTrue(type.has("label"));
            assertTrue(type.has("frequency"));
        }
    }

    /**
     * Test presence of a bug that allow the reuse of the same column ID twice in the same preparation.
     * <p>
     * This bug is allowed by OptimizedStrategy that does not apply some actions on RowMetadata and thus
     * RowMetadata.nextId is
     * not properly updated.
     * </p>
     */
    @Test
    public void test_add_preparation_TDP3927() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile(
                "/org/talend/dataprep/api/service/dataset/bug_TDP-3927_import-col-not-deleted_truncated.csv",
                "bug_TDP-3927_import-col-not-deleted", home.getId());

        Map<String, String> copyIdParameters = new HashMap<>();
        copyIdParameters.put("column_id", "0000");
        copyIdParameters.put("column_name", "id");
        copyIdParameters.put("scope", "column");
        testClient.applyAction(preparationId, "copy", copyIdParameters);

        InputStream inputStream = testClient.getPreparation(preparationId).asInputStream();
        mapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        RowMetadata preparationContent = mapper.readValue(inputStream, Data.class).metadata;

        ColumnMetadata idCopyColumn = getColumnByName(preparationContent, "id_copy");

        Map<String, String> deleteIdCopyParameters = new HashMap<>();
        deleteIdCopyParameters.put("column_id", "0008");
        deleteIdCopyParameters.put("column_name", "id_copy");
        deleteIdCopyParameters.put("scope", "column");
        testClient.applyAction(preparationId, "delete_column", deleteIdCopyParameters);

        // force export to update cache
        testClient.getPreparation(preparationId);

        // when
        Map<String, String> copyFirstNameParameters = new HashMap<>();
        copyFirstNameParameters.put("column_id", "0001");
        copyFirstNameParameters.put("column_name", "first_name");
        copyFirstNameParameters.put("scope", "column");
        testClient.applyAction(preparationId, "copy", copyFirstNameParameters);

        // then
        inputStream = testClient.getPreparation(preparationId).asInputStream();
        mapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        preparationContent = mapper.readValue(inputStream, Data.class).metadata;

        assertNotNull(preparationContent);
        ColumnMetadata firstNameColumn = getColumnByName(preparationContent, "first_name_copy");
        assertNotEquals(idCopyColumn.getId(), firstNameColumn.getId());
    }

    @Test
    public void shouldNotAcceptInvalidVersionId_TDP_4959() throws IOException {
        // given a preparation
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "foo", home.getId());

        // when trying to get the content of the preparation with an invalid version value
        String invalidVersionId = "%00";
        TdpExceptionDto exception = given()
                .queryParam("version", invalidVersionId) //
                .expect()
                .statusCode(404) //
                .log()
                .ifError() //
                .get("/api/preparations/{preparationId}/content", preparationId) //
                .as(TdpExceptionDto.class);

        // assertions
        assertTrue(exception.getCode().endsWith(PREPARATION_STEP_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldNotAcceptInvalidFolderValue_TDP_4959() throws IOException {
        // given an existing dataSet
        String dataSetId = testClient.createDataset("dataset/dataset.csv", "dataset");

        // when using an invalid folder id to create a preparation on the existing dataSet
        String invalidFolderId = "%40";
        TdpExceptionDto exception =
                given()
                        .contentType(ContentType.JSON)
                        .body("{ \"name\": \"foo\", \"dataSetId\": \"" + dataSetId + "\"}") //
                        .queryParam("folder", invalidFolderId) //
                        .expect() //
                        .statusCode(400) //
                        .log()
                        .ifError() //
                        .when() //
                        .post("/api/preparations") //
                        .as(TdpExceptionDto.class);
        // assertions
        assertTrue(exception.getCode().endsWith(FOLDER_DOES_NOT_EXIST.getCode()));
    }

    @Test
    public void shouldNotAcceptInvalidDataSetId_TDP_4959() {
        // when using an invalid dataSet id
        String invalidDataSetId = "@";
        TdpExceptionDto exception = given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"foo\", \"dataSetId\": \"" + invalidDataSetId + "\"}") //
                .queryParam("folder", "5a549eea1235ef6ee90e2096") //
                .expect() //
                .statusCode(404) //
                .log()
                .ifError() //
                .when() //
                .post("/api/preparations") //
                .as(TdpExceptionDto.class);
        // assertions
        assertTrue(exception.getCode().endsWith(DATASET_DOES_NOT_EXIST.getCode()));

    }

    @Test
    public void encodingIssuePreparationDetailsGet_TDP_4959() throws IOException {
        // given a preparation
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "foo", home.getId());

        // should return 400 instead of 500 (exception due to encoding issue for stepId parameter)
        Response response = given() //
                .queryParam("stepId", "%00") //
                .when() //
                .expect()
                .statusCode(400)
                .log()
                .ifError()
                .get("/api/preparations/{preparationId}/details", preparationId);
        TdpExceptionDto exception = response.as(TdpExceptionDto.class);

        assertTrue(exception.getCode().endsWith(UNABLE_TO_GET_PREPARATION_DETAILS.getCode()));
    }

    @Test
    public void datasetWithSameColumnsNameShouldBeHandled_TDP_5838() throws Exception {
        // given
        String inputFile = "/org/talend/dataprep/api/service/preparations/5L4Ccity_TDP-3858.csv";
        CSVReader baseFileReader =
                new CSVReader(new InputStreamReader(getClass().getResourceAsStream(inputFile)), ';', '"');
        String[] headers = baseFileReader.readNext();
        String[] record1 = baseFileReader.readNext();
        String datasetId = testClient.createDataset(inputFile, "5L4C city");

        // when
        Response datasetResponse = testClient.exportDataset(datasetId, "head");

        // then
        CSVReader datasetFileReader =
                new CSVReader(new InputStreamReader(datasetResponse.asInputStream(), UTF_8), ';', '"');
        String[] datasetHeaders = datasetFileReader.readNext();
        String[] datasetRecord1 = datasetFileReader.readNext();
        assertArrayEquals(headers, datasetHeaders);
        assertArrayEquals(record1, datasetRecord1);

        // and given
        String preparationId =
                testClient.createPreparationFromDataset(datasetId, "5L4C city Preparation", home.getId());

        // when
        Response response = testClient.exportPreparation(preparationId, "head", ";");

        // then
        CSVReader resultFileReader = new CSVReader(new InputStreamReader(response.asInputStream(), UTF_8), ';', '"');
        String[] resultHeaders = resultFileReader.readNext();
        assertArrayEquals(headers, resultHeaders);
        String[] resultRecord1 = resultFileReader.readNext();
        assertArrayEquals(record1, resultRecord1);
    }

    @Test
    public void testOnTheFlyPreparationMigrationDatasetName_TDP_6195() throws IOException {
        String datasetName = "my dataset - " + UUID.randomUUID().toString();
        String datasetId = testClient
                .createDataset("/org/talend/dataprep/api/service/preparations/5L4Ccity_TDP-3858.csv", datasetName);
        String preparationId =
                testClient.createPreparationFromDataset(datasetId, "5L4C city Preparation", home.getId());
        PersistentPreparation persistentPreparation =
                preparationRepository.get(preparationId, PersistentPreparation.class);
        persistentPreparation.setDataSetName(null);
        preparationRepository.add(persistentPreparation);

        // Our preparation do not have a dataset name
        PreparationDTO preparationDetails = testClient.getPreparationSummary(preparationId);
        assertNull(preparationDetails.getDataSetName());

        // listing should trigger migration
        testClient.listPreparations();

        // then we have the dataset name set in the preparation
        PreparationDTO preparationDetailsAfterList = testClient.getPreparationSummary(preparationId);
        assertEquals(datasetName, preparationDetailsAfterList.getDataSetName());
    }

    private ColumnMetadata getColumnByName(RowMetadata preparationContent, String columnName) {
        Optional<ColumnMetadata> firstNameColumn =
                preparationContent.getColumns().stream().filter(c -> columnName.equals(c.getName())).findAny();
        assertTrue(firstNameColumn.isPresent());
        return firstNameColumn.get();
    }

    private static class Data {

        public RowMetadata metadata;
    }

}
