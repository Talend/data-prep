// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class PreparationAPITest extends ApiServiceTestBase {

    /**
     * The root step.
     */
    @Resource(name = "rootStep")
    private Step rootStep;

    @Autowired
    private Security security;

    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------GETTER-------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void testEmptyPreparationList() throws Exception {
        assertThat(when().get("/api/preparations").asString(), sameJSONAs("[]"));
        assertThat(when().get("/api/preparations/?format=short").asString(), sameJSONAs("[]"));
        assertThat(when().get("/api/preparations/?format=long").asString(), sameJSONAs("[]"));
    }

    @Test
    public void testPreparationsList() throws Exception {
        // given
        String tagadaId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        String preparationId = createPreparationFromDataset(tagadaId, "testPreparation");

        // when : short format
        final JsonPath shortFormat = when().get("/api/preparations/?format=short").jsonPath();

        // then
        final List<String> values = shortFormat.getList("");
        assertThat(values.get(0), is(preparationId));

        // when : long format
        final JsonPath longFormat = when().get("/api/preparations/?format=long").jsonPath();

        // then
        assertThat(longFormat.getList("dataSetId").size(), is(1));
        assertThat(longFormat.getList("dataSetId").get(0), is(tagadaId));
        assertThat(longFormat.getList("author").size(), is(1));
        assertThat(longFormat.getList("author").get(0), is(security.getUserId()));
        assertThat(longFormat.getList("id").size(), is(1));
        assertThat(longFormat.getList("id").get(0), is(preparationId));
        assertThat(longFormat.getList("actions").size(), is(1));
        assertThat(((List) longFormat.getList("actions").get(0)).size(), is(0));

        // when : summary format
        final JsonPath summaryFormat = when().get("/api/preparations/?format=summary").jsonPath();

        // then
        assertThat(summaryFormat.getList("id").size(), is(1));
        assertThat(summaryFormat.getList("id").get(0), is(preparationId));
        assertThat(summaryFormat.getList("name").size(), is(1));
        assertThat(summaryFormat.getList("name").get(0), is("testPreparation"));
        assertThat(summaryFormat.getList("owner").size(), is(1));
        assertThat(summaryFormat.getList("lastModificationDate").size(), is(1));
        assertThat(summaryFormat.getList("allowDistributedRun").size(), is(1));
    }

    @Test
    public void testPreparationGet() throws Exception {
        // when
        final String datasetId = createDataset("dataset/dataset.csv", "great dataset", "text/csv");
        final String preparationId = createPreparationFromDataset(datasetId, "1234");

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
    public void testListCompatibleDataSets() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "compatible1", "text/csv");
        final String dataSetId2 = createDataset("dataset/dataset.csv", "compatible2", "text/csv");
        final String dataSetId3 = createDataset("t-shirt_100.csv", "incompatible", "text/csv");
        final String preparationId = createPreparationFromDataset(dataSetId, "testPreparation");


        // when
        final String compatibleDatasetList = when().get("/api/preparations/{id}/basedatasets", preparationId).asString();

        // then
        assertTrue(compatibleDatasetList.contains(dataSetId2));
        assertFalse(compatibleDatasetList.contains(dataSetId3));
    }

    @Test
    public void testListCompatibleDataSetsWhenUniqueDatasetInRepository() throws Exception {
        // given
        final String dataSetId = createDataset("dataset/dataset.csv", "unique", "text/csv");
        final String preparationId = createPreparationFromDataset(dataSetId, "testPreparation");

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
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "super preparation", "text/csv", origin.getId());

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
        final Iterator<FolderEntry> iterator = folderRepository.entries(destination.getId(), PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry entry = iterator.next();
        assertEquals(entry.getContentId(), copyId);

        // check the name
        final Preparation actual = preparationRepository.get(copyId, Preparation.class);
        assertEquals(newPreparationName, actual.getName());
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
        final String id = createPreparationFromFile("dataset/dataset.csv", "great_preparation", "text/csv", source.getId());

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
        final Iterator<FolderEntry> iterator = folderRepository.entries(destination.getId(), PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry entry = iterator.next();
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

    //------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------LIFECYCLE-----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void testPreparationUpdate() throws Exception {
        // given
        String tagadaId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        final String preparationId = createPreparationFromDataset(tagadaId, "original_name");

        JsonPath longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is("original_name"));
        assertThat(longFormat.getList("id").size(), is(1));
        assertThat(longFormat.getList("id").get(0), is(preparationId));

        // when
        given().contentType(ContentType.JSON).body("{ \"name\": \"updated_name\", \"dataSetId\": \"" + tagadaId + "\" }")
                .put("/api/preparations/{id}", preparationId).asString();

        // then
        longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is("updated_name"));
    }

    @Test
    public void testPreparationDelete() throws Exception {
        // given
        String tagadaId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        final String preparationId = createPreparationFromDataset(tagadaId, "original_name");

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

        String tagadaId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        final String preparationId = createPreparationFromDataset(tagadaId, "original_name");

        final String list = when().get("/api/preparations").asString();
        assertThat(list.contains(preparationId), is(true));

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
        when().delete("/api/preparations/" + preparationId).asString();

        // then
        Assert.assertThat(contentCache.has(metadataKey), is(false));
        Assert.assertThat(contentCache.has(contentKey), is(false));
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------STEPS-------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_append_action_after_actual_head() throws Exception {
        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");

        // when
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        final List<String> steps = getPreparation(preparationId).getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(rootStep.id()));
    }

    @Test
    public void shouldAddMultipleActionStepAfterHead() throws Exception {
        /// when: 1 AppendStep with 2 actions
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");

        // when
        applyActionFromFile(preparationId, "transformation/upper_case_lastname_firstname.json");

        // then : it should have appended 2 actions
        final PreparationMessageForTest preparationMessage = getPreparation(preparationId);
        final List<String> steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(rootStep.id()));
    }

    private PreparationMessageForTest getPreparation(String preparationId) throws IOException {
        return mapper.readValue(given().get("/api/preparations/{preparation}/details", preparationId).asInputStream(),
                PreparationMessageForTest.class);
    }

    @Test
    public void should_save_created_columns_ids_on_append() throws Exception {
        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");

        // when
        applyActionFromFile(preparationId, "transformation/copy_firstname.json");

        // then
        final JsonPath jsonPath = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath();
        final List<String> createdColumns = jsonPath.getList("diff[0].createdColumns");
        assertThat(createdColumns, hasSize(1));
        assertThat(createdColumns, hasItem("0006"));
    }

    @Test
    public void should_fail_properly_on_append_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"));
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");

        // when
        final Response request = given().contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .post("/api/preparations/{id}/actions", preparationId);

        //then
        request.then()//
                .statusCode(400)//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    @Test
    public void should_update_action() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparation(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(rootStep.id()));

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59") with another action
        final String actionContent3 = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/lower_case_lastname.json"));
        given().contentType(ContentType.JSON)
                .body(actionContent3)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId,
                        steps.get(1))
                .then().statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparation(preparationId).getSteps();
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(rootStep.id()));
    }

    @Test
    public void should_save_created_columns_ids_on_update() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = getPreparation(preparationId).getSteps();

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59")
        // with another action that create a column
        final String updateAction = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/copy_firstname.json"));
        given().contentType(ContentType.JSON)
                .body(updateAction)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId,
                        steps.get(1))
                .then().statusCode(is(200));

        // then
        final PreparationMessageForTest preparation = getPreparation(preparationId);
        final List<String> createdColumns = preparation.getDiff().get(0).getCreatedColumns();
        assertThat(createdColumns, hasSize(1));
        assertThat(createdColumns, hasItem("0006"));
    }

    @Test
    public void should_fail_properly_on_update_error() throws Exception {
        // given
        final String missingScopeAction = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/upper_case_firstname_without_scope.json"));
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = getPreparation(preparationId).getSteps();
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
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = getPreparation(preparationId).getSteps();
        final String firstStep = steps.get(1);

        // when
        given().delete("/api/preparations/{preparation}/actions/{action}", preparationId, firstStep) //
                .then() //
                .statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = getPreparation(preparationId).getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(rootStep.id()));
    }

    @Test
    public void should_throw_error_when_preparation_does_not_exist_on_delete() throws Exception {
        // when : delete unknown preparation action
        final Response response = given().delete("/api/preparations/{preparation}/actions/{action}", "unknown_prep", "unkown_step");

        //then : should have preparation service error
        response.then().statusCode(is(404)).body("code", is("TDP_PS_PREPARATION_DOES_NOT_EXIST"));
    }

    @Test
    public void should_change_preparation_head() throws Exception {
        //given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        final String newHead = preparationRepository.get(preparation.getHeadId(), Step.class).getParent().getId();

        //when
        given().when()//
                .put("/api/preparations/{id}/head/{stepId}", preparationId, newHead)//
                .then()//
                .statusCode(200);

        //then
        preparation = preparationRepository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(newHead));
    }

    @Test
    public void should_throw_exception_on_preparation_head_change_with_unknown_step() throws Exception {
        //given
        String tagadaId = createDataset("dataset/dataset.csv", "tagada", "text/csv");
        final String preparationId = createPreparationFromDataset(tagadaId, "testPreparation");

        //when
        final Response response = given().when()//
                .put("/api/preparations/{id}/head/{stepId}", preparationId, "unknown_step_id");

        //then
        response.then()//
                .statusCode(404)//
                .assertThat()//
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void shouldCopySteps() throws Exception {
        // given
        final String referenceId = createPreparationFromFile("dataset/dataset.csv", "reference", "text/csv");
        applyActionFromFile(referenceId, "transformation/upper_case_firstname.json");
        Preparation reference = preparationRepository.get(referenceId, Preparation.class);

        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "prep", "text/csv");

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .put("/api/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        assertEquals(reference.getHeadId(), preparation.getHeadId());
    }

    //------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------CONTENT------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------


    @Test
    public void shouldCreatePreparationInDefaultFolder() throws Exception {

        // given
        Folder home = folderRepository.getHome();
        Iterator<FolderEntry> entries = folderRepository.entries(home.getId(), PREPARATION).iterator();
        assertFalse(entries.hasNext());

        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", "text/csv");

        // then
        entries = folderRepository.entries(home.getId(), PREPARATION).iterator();
        assertTrue(entries.hasNext());
        final FolderEntry entry = entries.next();
        assertThat(entry.getContentId(), is(preparationId));
        assertThat(entry.getContentType(), is(PREPARATION));
        assertFalse(entries.hasNext());
    }

    @Test
    public void shouldCreatePreparationInSpecificFolder() throws Exception {

        // given
        final String path = "/folder-1/sub-folder-2";
        Folder folder = folderRepository.addFolder(folderRepository.getHome().getId(), path);
        Iterator<FolderEntry> entries = folderRepository.entries(folder.getId(), PREPARATION).iterator();
        assertFalse(entries.hasNext());

        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", "text/csv", folder.getId());

        // then
        entries = folderRepository.entries(folder.getId(), PREPARATION).iterator();
        assertTrue(entries.hasNext());
        final FolderEntry entry = entries.next();
        assertThat(entry.getContentId(), is(preparationId));
        assertThat(entry.getContentType(), is(PREPARATION));
        assertFalse(entries.hasNext());
    }


    @Test
    public void testPreparationInitialContent() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet", "text/csv");

        final InputStream expected = PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json");

        // when
        final String content = when().get("/api/preparations/{id}/content", preparationId).asString();

        // then
        assertThat(content, sameJSONAsFile(expected));
    }

    @Test
    public void testPreparationContentWithActions() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparationContentGet", "text/csv");
        PreparationMessageForTest preparation = getPreparation(preparationId);
        List<String> steps = preparation.getSteps();

        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), is(rootStep.id()));

        // when
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        final PreparationMessageForTest preparationMessage = getPreparation(preparationId);
        steps = preparationMessage.getSteps();
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(rootStep.id()));

        // Request preparation content at different versions (preparation has 2 steps -> Root + Upper Case).
        assertThat(when().get("/api/preparations/{id}/content", preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=head", preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + steps.get(0), preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + steps.get(1), preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_firstname_uppercase_with_column.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=origin", preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
        assertThat(when().get("/api/preparations/{id}/content?version=" + rootStep.id(), preparationId).asString(),
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("dataset/expected_dataset_with_columns.json")));
    }

    @Test
    public void shouldGetPreparationContent() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet", "text/csv");

        // when
        String preparationContent = given().get("/api/preparations/{preparation}/content", preparationId).asString();

        // then
        assertThat(preparationContent,
                sameJSONAsFile(PreparationAPITest.class.getResourceAsStream("t-shirt_100.csv.expected.json")));
    }

    @Test
    public void shouldGetPreparationContentWhenInvalidSample() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet", "text/csv");

        // when
        String preparationContent = given().get("/api/preparations/{preparation}/content?sample=mdljshf", preparationId)
                .asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(preparationContent);
        JsonNode records = rootNode.get("records");
        assertThat(records.size(), is(100));
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------PREVIEW------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void testPreparationDiffPreview() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("preview/preview_dataset.csv", "testPreview", "text/csv");
        applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        applyActionFromFile(preparationId, "preview/delete_city.json");

        final List<String> steps = getPreparation(preparationId).getSteps();
        final String firstActionStep = steps.get(1);
        final String lastStep = steps.get(steps.size() - 1);

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"currentStepId\": \"" + firstActionStep + "\",\n" // action 1
                + "   \"previewStepId\": \"" + lastStep + "\",\n" // action 1 + 2 + 3
                + "   \"tdpIds\": [2, 4, 6]" //
                + "}";

        final InputStream expectedDiffStream = PreparationAPITest.class
                .getResourceAsStream("preview/expected_diff_preview.json");

        // when
        final String diff = given().contentType(ContentType.JSON).body(input).when().post("/api/preparations/preview/diff")
                .asString();

        // then
        assertThat(diff, sameJSONAsFile(expectedDiffStream));
    }

    @Test
    public void testPreparationUpdatePreview() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("preview/preview_dataset.csv", "testPreview", "text/csv");
        applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        applyActionFromFile(preparationId, "preview/delete_city.json");

        final PreparationMessageForTest preparationMessage = getPreparation(preparationId);
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
        final String preparationId = createPreparationFromFile("preview/preview_dataset.csv", "testPreview", "text/csv");
        applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        applyActionFromFile(preparationId, "preview/delete_city.json");

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": [{\n"
                + "         \"action\": \"uppercase\",\n"
                + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n"
                + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n"
                + "         }\n" //
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
        final String datasetId = createDataset("preview/preview_dataset.csv", "testPreview", "text/csv");

        final String input = "{" //
                + "   \"datasetId\": \"" + datasetId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": [{\n"
                + "         \"action\": \"uppercase\",\n"
                + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n"
                + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n"
                + "         }\n" //
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
        final String preparationId = createPreparationFromFile("preview/preview_dataset.csv", "testPreview", "text/csv");
        applyActionFromFile(preparationId, "preview/upper_case_lastname.json");
        applyActionFromFile(preparationId, "preview/upper_case_firstname.json");
        applyActionFromFile(preparationId, "preview/delete_city.json");

        final String input = "{" //
                + "   \"preparationId\": \"" + preparationId + "\",\n" //
                + "   \"tdpIds\": [2, 4, 6],\n" //
                + "   \"actions\": ["
                + "         {\n"
                + "             \"action\": \"uppercase\",\n"
                + "             \"parameters\": {\n"
                + "                 \"column_id\": \"0005\",\n"
                + "                 \"column_name\": \"alive\"\n,"
                + "                 \"scope\": \"column\"\n"
                + "             }\n" //
                + "         },\n"
                + "         {\n"
                + "             \"action\": \"uppercase\",\n"
                + "             \"parameters\": {\n"
                + "                 \"column_id\": \"0006\",\n"
                + "                 \"column_name\": \"city\"\n,"
                + "                 \"scope\": \"column\"\n"
                + "             }\n" //
                + "         }\n"
                + "    ]\n" //
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
        final String datasetId = createDataset("preview/preview_dataset.csv", "testPreview", "text/csv");

        String testPrepId = createPreparationFromDataset(datasetId, "testPrep");

        AppendStep appendStep = new AppendStep();
        appendStep.setActions(Arrays.asList(
                buildAction("uppercase", buildParametersMap("column_id", "0002", "column_name", "lastname", "scope", "column")),
                buildAction("uppercase", buildParametersMap("column_id", "0001", "column_name", "firstname", "scope", "column"))));
        appendStepsToPrep(testPrepId, appendStep);

        // Adding steps
        PreparationMessageForTest testPrepDetails = getPreparation(testPrepId);

        List<String> stepsCreated = testPrepDetails.getSteps();

        String rootStep = stepsCreated.get(0);
        String secondStep = stepsCreated.get(2);

        // changing steps order
        changePreparationStepsOrder(testPrepId, rootStep, secondStep);

        PreparationMessageForTest testPrepDetailsAfter = getPreparation(testPrepId);

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
        final String lookupDataSetId = createDataset("dataset/dataset.csv", "lookup_ds", "text/csv");
        final String dataSetId = createDataset("dataset/dataset_cars.csv", "cars", "text/csv");

        String carsPreparationId = createPreparationFromDataset(dataSetId, "cars_preparation");

        String action = IOUtils.toString(getClass().getResource("preparations/cars_lookup_action.json"));
        applyAction(carsPreparationId, action.replace("{lookup_ds_id}", lookupDataSetId));

        // Try to delete lookup dataset => fail because used
        expect().statusCode(CONFLICT.value()).when().delete("/api/datasets/{id}", lookupDataSetId);

        PreparationMessageForTest preparationDetails = getPreparation(carsPreparationId);
        String firstStepId = preparationDetails.getSteps().get(0);

        // Now undo
        expect().statusCode(OK.value()).when().put("/api/preparations/{id}/head/{headId}", carsPreparationId, firstStepId);

        // Try again to delete lookup dataset
        expect().statusCode(OK.value()).when().get("/api/datasets/{id}", lookupDataSetId);
    }

    @Test
    public void shouldGetPreparationColumnTypes() throws Exception {

        // given
        final String id = createPreparationFromFile("dataset/dataset.csv", "super preparation", "text/csv");

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
}
