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

import static com.jayway.restassured.RestAssured.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.api.service.EntityBuilder.buildAction;
import static org.talend.dataprep.api.service.EntityBuilder.buildParametersMap;
import static org.talend.dataprep.api.service.PreparationAPITestClient.appendStepsToPrep;
import static org.talend.dataprep.api.service.PreparationAPITestClient.changePreparationStepsOrder;
import static org.talend.dataprep.cache.ContentCache.TimeToLive.PERMANENT;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationSummary;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

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
    public void testEmptyPreparationList() throws Exception {
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
        final List<String> values = shortFormat.getList("");
        assertThat(values.get(0), is(preparationId));

        // when : long format
        Response response1 = when().get("/api/preparations/?format=long");

        // then
        List<UserPreparation> preparations = mapper.readerFor(UserPreparation.class)
                .<UserPreparation> readValues(response1.asInputStream()).readAll();
        assertEquals(1, preparations.size());
        UserPreparation userPreparation = preparations.iterator().next();
        assertThat(userPreparation.getDataSetId(), is(tagadaId));
        assertThat(userPreparation.getAuthor(), is(security.getUserId()));
        assertThat(userPreparation.getId(), is(preparationId));
        assertThat(userPreparation.getActions(), is(empty()));

        // when : summary format
        Response response = when().get("/api/preparations/?format=summary");

        // then
        List<PreparationSummary> preparationSummaries = mapper.readerFor(PreparationSummary.class)
                .<PreparationSummary> readValues(response.asInputStream()).readAll();
        assertEquals(1, preparationSummaries.size());
        PreparationSummary preparationSummary = preparationSummaries.iterator().next();
        assertThat(preparationSummary.getId(), is(preparationId));
        assertThat(preparationSummary.getName(), is("testPreparation"));
        assertThat(preparationSummary.getLastModificationDate(), is(notNullValue()));
        assertThat(preparationSummary.isAllowDistributedRun(), is(notNullValue()));
    }

    @Test
    public void testPreparationsGet() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when : long format
        Response response1 = when().get("/api/preparations/{preparationId}/details", preparationId);

        // then
        EnrichedPreparation userPreparation = mapper.readerFor(EnrichedPreparation.class).readValue(response1.asInputStream());
        assertThat(userPreparation.getDataSetId(), is(tagadaId));
        assertThat(userPreparation.getAuthor(), is(security.getUserId()));
        assertThat(userPreparation.getId(), is(preparationId));
        assertThat(userPreparation.getActions(), is(empty()));
        assertThat(userPreparation.getFolder().getPath(), is(home.getPath()));
    }

    @Test
    public void testPreparationsList_withFilterOnName() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationId = testClient.createPreparationFromDataset(tagadaId, "testPreparation", home.getId());

        // when : short format
        final JsonPath shortFormat = when().get("/api/preparations/?format=short&name={name}", "testPreparation").jsonPath();

        // then
        final List<String> values = shortFormat.getList("");
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
        List<String> result = mapper.readerFor(String.class).<String> readValues(shouldNotBeEmpty.asInputStream()).readAll();
        assertThat(result.get(0), is(preparationId));

        // when
        final JsonPath shouldBeEmpty = when().get("/api/preparations/?format=short&folder_path={folder_path}", "/toto")
                .jsonPath();

        // then
        assertThat(shouldBeEmpty.<String> getList(""), is(empty()));
    }

    @Test
    public void testPreparationsList_withFilterOnFullPath() throws Exception {
        // given
        String tagadaId = testClient.createDataset("dataset/dataset.csv", "tagada");
        String preparationName = "tagadaPreparation";
        String preparationId = testClient.createPreparationFromDataset(tagadaId, preparationName, home.getId());

        // when : short format
        final JsonPath shouldNotBeEmpty = when().get("/api/preparations/?format=short&path={path}", "/" + preparationName)
                .jsonPath();

        // then
        assertThat(shouldNotBeEmpty.<String> getList("").get(0), is(preparationId));

        // when
        final JsonPath shouldBeEmpty = when().get("/api/preparations/?format=short&path={path}", "/toto/" + preparationName)
                .jsonPath();

        // then
        assertThat(shouldBeEmpty.<String> getList(""), is(empty()));
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

        assertNotNull(longFormat.getMap("rowMetadata"));
        assertNotNull(longFormat.getMap("rowMetadata").get("columns"));
        ArrayList rowMetaData = (ArrayList) longFormat.getMap("rowMetadata").get("columns");
        assertThat(rowMetaData.size(), is(6));

        assertThat(longFormat.getString("allowFullRun"), is("false"));
        final List<String> steps = longFormat.getList("steps"); // make sure the "steps" node is a string array
        assertThat(steps.size(), is(1));
    }

    @Test
    public void testListCompatibleDataSets() throws Exception {
        // given
        final String dataSetId = testClient.createDataset("dataset/dataset.csv", "compatible1");
        final String dataSetId2 = testClient.createDataset("dataset/dataset.csv", "compatible2");
        final String dataSetId3 = testClient.createDataset("t-shirt_100.csv", "incompatible");
        final String preparationId = testClient.createPreparationFromDataset(dataSetId, "testPreparation", home.getId());

        // when
        final String compatibleDatasetList = when().get("/api/preparations/{id}/basedatasets", preparationId).asString();

        // then
        assertTrue(compatibleDatasetList.contains(dataSetId2));
        assertFalse(compatibleDatasetList.contains(dataSetId3));
    }

    @Test
    public void testListCompatibleDataSetsWhenUniqueDatasetInRepository() throws Exception {
        // given
        final String dataSetId = testClient.createDataset("dataset/dataset.csv", "unique");
        final String preparationId = testClient.createPreparationFromDataset(dataSetId, "testPreparation", home.getId());

        // when
        final String compatibleDatasetList = when().get("/api/preparations/{id}/basedatasets", preparationId).asString();

        // then
        assertFalse(compatibleDatasetList.contains(dataSetId));
    }

    @Test
    public void shouldCopyPreparation() throws Exception {
        // given
        Folder destination = folderRepository.addFolder(home.getId(), "/destination");
        Folder origin = folderRepository.addFolder(home.getId(), "/from");
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "super preparation",
                origin.getId());

        // when
        String newPreparationName = "NEW super preparation";
        final Response response = given() //
                .queryParam("destination", destination.getId()) //
                .queryParam("newName", newPreparationName) //
                .when()//
                .expect().statusCode(200).log().ifError() //
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
            return entriesStream.collect(Collectors.toList());
        }
    }

    @Test
    public void copyPreparationShouldForwardExceptions() throws Exception {

        // when
        final Response response = given() //
                .queryParam("destination", "/destination") //
                .when()//
                .expect().statusCode(404).log().ifError() //
                .post("api/preparations/{id}/copy", "preparation_not_found");

        // then
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void shouldMovePreparation() throws Exception {
        // given
        final Folder source = folderRepository.addFolder(home.getId(), "source");
        final String id = testClient.createPreparationFromFile("dataset/dataset.csv", "great_preparation", source.getId());

        final Folder destination = folderRepository.addFolder(home.getId(), "destination");

        // when
        final Response response = given() //
                .queryParam("folder", source.getId()) //
                .queryParam("destination", destination.getId()) //
                .queryParam("newName", "NEW great preparation") //
                .when()//
                .expect().statusCode(200).log().ifError() //
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
    public void movePreparationShouldForwardExceptions() throws Exception {

        // when
        final Response response = given() //
                .queryParam("folder", "/from") //
                .queryParam("destination", "/to") //
                .queryParam("newName", "NEW great preparation") //
                .when()//
                .expect().statusCode(404).log().ifError() //
                .put("api/preparations/{id}/move", "unknown_preparation");

        // then
        assertEquals(404, response.getStatusCode());
    }

    /**
     *
     * @see <a href="https://jira.talendforge.org/browse/TDP-3965">TDP-3965</a>
     */
    @Test
    public void ensureThatPreparationDetailsCanBeParsedAsStandalonePreparation_TDP_3965() throws Exception {
        // when
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");
        InputStream inputStream = given().expect().statusCode(200).get("/api/preparations/{preparation}/details", preparationId)
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

        Preparation preparation = new Preparation();
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

        final ContentCacheKey metadataKey = cacheKeyGenerator.metadataBuilder().preparationId(preparationId).stepId("step1")
                .sourceType(FILTER).build();
        final ContentCacheKey contentKey = cacheKeyGenerator.contentBuilder().datasetId("datasetId").preparationId(preparationId)
                .stepId("step1").format(JSON).parameters(emptyMap()).sourceType(FILTER).build();
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
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

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
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname_firstname.json");

        // then : it should have appended 2 actions
        final EnrichedPreparation preparationMessage = getPreparationDetails(preparationId);
        final List<String> steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    private EnrichedPreparation getPreparationDetails(String preparationId) throws IOException {
        return mapper.readValue(given().get("/api/preparations/{preparation}/details", preparationId).asInputStream(),
                EnrichedPreparation.class);
    }

    @Test
    public void should_save_created_columns_ids_on_append() throws Exception {
        // when
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        testClient.applyActionFromFile(preparationId, "transformation/copy_firstname.json");

        // then
        final JsonPath jsonPath = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath();
        final List<String> createdColumns = jsonPath.getList("diff[0].createdColumns");
        assertThat(createdColumns, hasSize(1));
        assertThat(createdColumns, hasItem("0006"));
    }

    @Test
    public void should_fail_properly_on_append_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(
                PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"), UTF_8);
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());

        // when
        final Response request = given().contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .post("/api/preparations/{id}/actions", preparationId);

        // then
        request.then()//
                .statusCode(400)//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    @Test
    public void should_update_action() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59") with
        // another action
        final String actionContent3 = IOUtils
                .toString(PreparationAPITest.class.getResourceAsStream("transformation/lower_case_lastname.json"), UTF_8);
        given().contentType(ContentType.JSON).body(actionContent3)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId, steps.get(1)).then().statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void should_save_created_columns_ids_on_update() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = getPreparationDetails(preparationId).getSteps();

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59")
        // with another action that create a column
        final String updateAction = IOUtils
                .toString(PreparationAPITest.class.getResourceAsStream("transformation/copy_firstname.json"), UTF_8);
        given().contentType(ContentType.JSON).body(updateAction)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId, steps.get(1)).then().statusCode(is(200));

        // then
        final EnrichedPreparation preparation = getPreparationDetails(preparationId);
        final List<String> createdColumns = preparation.getDiff().get(0).getCreatedColumns();
        assertThat(createdColumns, hasSize(1));
        assertThat(createdColumns, hasItem("0006"));
    }

    @Test
    public void should_fail_properly_on_update_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(
                PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"), UTF_8);
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = getPreparationDetails(preparationId).getSteps();
        final String firstStep = steps.get(1);

        // when
        final Response request = given().contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .put("/api/preparations/{id}/actions/{step}", preparationId, firstStep);

        // then
        request.then()//
                .statusCode(400)//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    @Test
    public void should_delete_preparation_action() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparationDetails(preparationId).getSteps();
        final String firstStep = steps.get(1);

        // when
        given().delete("/api/preparations/{preparation}/actions/{step}", preparationId, firstStep) //
                .then() //
                .statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparationDetails(preparationId).getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));
    }

    @Test
    public void should_throw_error_when_preparation_does_not_exist_on_delete() throws Exception {
        // when : delete unknown preparation action
        final Response response = given().delete("/api/preparations/{preparation}/actions/{action}", "unknown_prep",
                "unkown_step");

        // then : should have preparation service error
        response.then().statusCode(is(404)).body("code", is("TDP_PS_PREPARATION_DOES_NOT_EXIST"));
    }

    @Test
    public void should_change_preparation_head() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparation", home.getId());
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        final String newHead = preparationRepository.get(preparation.getHeadId(), Step.class).getParent();

        // when
        given().when()//
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
        final Response response = given().when()//
                .put("/api/preparations/{id}/head/{stepId}", preparationId, "unknown_step_id");

        // then
        response.then()//
                .statusCode(404)//
                .assertThat()//
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void shouldCopySteps() throws Exception {
        // given
        final String referenceId = testClient.createPreparationFromFile("dataset/dataset.csv", "reference", home.getId());
        testClient.applyActionFromFile(referenceId, "transformation/upper_case_firstname.json");
        Preparation reference = preparationRepository.get(referenceId, Preparation.class);

        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "prep", home.getId());

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .expect().statusCode(200).log().ifError() //
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
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation",
                home.getId());

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
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation",
                folder.getId());

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

        final Response response = given() //
                .contentType(ContentType.JSON) //
                .body("{ \"name\": \"" + "my_preparation" + "\", \"dataSetId\": \"" + dataSetId + "\"}")
                .queryParam("folder", home.getId()) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/api/preparations");
    }

    @Test
    public void testPreparationInitialContent() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet",
                home.getId());

        final InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json");

        // when
        final String content = when().get("/api/preparations/{id}/content", preparationId).asString();

        // then
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testPreparationInitialMetadata() throws Exception {
        // given
        final String preparationName = "testPreparationContentGet";
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", preparationName, home.getId());

        // when
        final String content = when().get("/api/preparations/{id}/metadata", preparationId).asString();

        // then
        final DataSetMetadata actual = mapper.readerFor(DataSetMetadata.class).readValue(content);
        assertNotNull(actual);
        final List<ColumnMetadata> columns = actual.getRowMetadata().getColumns();
        assertEquals(6, columns.size());
        final List<String> expectedColumns = Arrays.asList("id", "firstname", "lastname", "age", "date-of-birth", "alive");
        for (ColumnMetadata column : columns) {
            assertTrue(expectedColumns.contains(column.getName()));
        }
    }

    @Test
    public void testPreparationContentWithActions() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet",
                home.getId());
        EnrichedPreparation preparation = getPreparationDetails(preparationId);
        List<String> steps = preparation.getSteps();

        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // when
        testClient.applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        final EnrichedPreparation preparationMessage = getPreparationDetails(preparationId);
        steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(Step.ROOT_STEP.id()));

        // Request preparation content at different versions (preparation has 2 steps -> Root + Upper Case).
        assertThat(when().get("/api/preparations/{id}/content", preparationId).asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=head", preparationId).asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + steps.get(0), preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + steps.get(1), preparationId).asString(), sameJSONAsFile(
                PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=origin", preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + Step.ROOT_STEP.id(), preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
    }

    @Test
    public void shouldGetPreparationContent() throws IOException {
        // given
        final String preparationId = testClient.createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet",
                home.getId());

        // when
        String preparationContent = given().get("/api/preparations/{preparation}/content", preparationId).asString();

        // then
        assertThat(preparationContent,
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv.expected.json")));
    }

    @Test
    public void shouldGetPreparationContentWhenInvalidSample() throws IOException {
        // given
        final String preparationId = testClient.createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet",
                home.getId());

        // when
        String preparationContent = given().get("/api/preparations/{preparation}/content?sample=mdljshf", preparationId)
                .asString();

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
        final String preparationId = testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview",
                home.getId());
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

        final InputStream expectedDiffStream = PreparationAPITest.class.getResourceAsStream("preview/expected_diff_preview.json");

        // when
        final String diff = given().contentType(ContentType.JSON).body(input).when().post("/api/preparations/preview/diff")
                .asString();

        // then
        assertThat(diff, sameJSONAsFile(expectedDiffStream));
    }

    @Test
    public void testPreparationUpdatePreview() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview",
                home.getId());
        testClient.applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "preview/delete_city.json");

        final EnrichedPreparation preparationMessage = getPreparationDetails(preparationId);
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

        final InputStream expectedDiffStream = PreparationAPITest.class
                .getResourceAsStream("preview/expected_update_preview.json");

        // when
        final String diff = given().contentType(ContentType.JSON).body(input).when().post("/api/preparations/preview/update")
                .asString();

        // then
        assertThat(diff, sameJSONAsFile(expectedDiffStream));
    }

    @Test
    public void testPreparationAddPreviewOnPreparation() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview",
                home.getId());
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
                .expect().statusCode(200).log().ifError() //
                .post("/api/preparations/preview/add") //
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
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
        final InputStream expectedPreviewStream = PreparationAPITest.class
                .getResourceAsStream("preview/expected_add_preview_on_dataset.json");

        // when
        final String preview = given().contentType(ContentType.JSON).body(input).when().post("/api/preparations/preview/add")
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
    }

    @Test
    public void testPreparationMultipleAddPreview() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("preview/preview_dataset.csv", "testPreview",
                home.getId());
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
        final InputStream expectedPreviewStream = getClass().getResourceAsStream("preview/expected_multi_add_preview.json");

        // when
        final String preview = given() //
                .contentType(ContentType.JSON) //
                .body(input) //
                .when() //
                .expect().statusCode(200).log().ifError() //
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
        appendStep.setActions(Arrays.asList(
                buildAction("uppercase", buildParametersMap("column_id", "0002", "column_name", "lastname", "scope", "column")),
                buildAction("uppercase",
                        buildParametersMap("column_id", "0001", "column_name", "firstname", "scope", "column"))));
        appendStepsToPrep(testPrepId, appendStep);

        // Adding steps
        EnrichedPreparation testPrepDetails = getPreparationDetails(testPrepId);

        List<String> stepsCreated = testPrepDetails.getSteps();

        String rootStep = stepsCreated.get(0);
        String secondStep = stepsCreated.get(2);

        // changing steps order
        changePreparationStepsOrder(testPrepId, rootStep, secondStep);

        EnrichedPreparation testPrepDetailsAfter = getPreparationDetails(testPrepId);

        assertEquals(testPrepDetailsAfter.getActions().get(0), testPrepDetails.getActions().get(1));
        assertEquals(testPrepDetailsAfter.getActions().get(1), testPrepDetails.getActions().get(0));
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

        EnrichedPreparation preparationDetails = getPreparationDetails(carsPreparationId);
        String firstStepId = preparationDetails.getSteps().get(0);

        // Now undo
        expect().statusCode(OK.value()).when().put("/api/preparations/{id}/head/{headId}", carsPreparationId, firstStepId);

        // Try again to delete lookup dataset
        expect().statusCode(OK.value()).when().get("/api/datasets/{id}", lookupDataSetId);
    }

    @Test
    public void shouldGetPreparationColumnTypes() throws Exception {

        // given
        final String id = testClient.createPreparationFromFile("dataset/dataset.csv", "super preparation", home.getId());

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
     * This bug is allowed by OptimizedStrategy that does not apply some actions on RowMetadata and thus RowMetadata.nextId is
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

        RowMetadata preparationContent = testClient.getPreparationContent(preparationId);
        ColumnMetadata idCopyColumn = getColumnByName(preparationContent, "id_copy");

        Map<String, String> deleteIdCopyParameters = new HashMap<>();
        deleteIdCopyParameters.put("column_id", "0008");
        deleteIdCopyParameters.put("column_name", "id_copy");
        deleteIdCopyParameters.put("scope", "column");
        testClient.applyAction(preparationId, "delete_column", deleteIdCopyParameters);

        // force export to update cache
        testClient.getPreparationContent(preparationId);

        // when
        Map<String, String> copyFirstNameParameters = new HashMap<>();
        copyFirstNameParameters.put("column_id", "0001");
        copyFirstNameParameters.put("column_name", "first_name");
        copyFirstNameParameters.put("scope", "column");
        testClient.applyAction(preparationId, "copy", copyFirstNameParameters);

        // then
        preparationContent = testClient.getPreparationContent(preparationId);
        assertNotNull(preparationContent);
        ColumnMetadata firstNameColumn = getColumnByName(preparationContent, "first_name_copy");
        assertNotEquals(idCopyColumn.getId(), firstNameColumn.getId());
    }

    private ColumnMetadata getColumnByName(RowMetadata preparationContent, String columnName) {
        Optional<ColumnMetadata> firstNameColumn = preparationContent.getColumns().stream()
                .filter(c -> columnName.equals(c.getName())).findAny();
        assertTrue(firstNameColumn.isPresent());
        return firstNameColumn.get();
    }

}
