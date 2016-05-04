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
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.security.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class PreparationAPITest extends ApiServiceTestBase {

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;

    @Autowired
    private Security security;

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
        String preparationId = createPreparationFromDataset("1234", "testPreparation");

        // when : short format
        final JsonPath shortFormat = when().get("/api/preparations/?format=short").jsonPath();

        // then
        final List<String> values = shortFormat.getList("");
        assertThat(values.get(0), is(preparationId));

        // when : long format
        final JsonPath longFormat = when().get("/api/preparations/?format=long").jsonPath();

        // then
        assertThat(longFormat.getList("dataSetId").size(), is(1));
        assertThat(longFormat.getList("dataSetId").get(0), is("1234"));
        assertThat(longFormat.getList("author").size(), is(1));
        assertThat(longFormat.getList("author").get(0), is(security.getUserId()));
        assertThat(longFormat.getList("id").size(), is(1));
        assertThat(longFormat.getList("id").get(0), is(preparationId));
        assertThat(longFormat.getList("actions").size(), is(1));
        assertThat(((List) longFormat.getList("actions").get(0)).size(), is(0));
    }

    @Test
    public void testPreparationGet() throws Exception {
        // when
        final String preparationId = createPreparationFromDataset("1234", "testPreparation");

        // then
        final JsonPath longFormat = given().get("/api/preparations/{id}/details", preparationId).jsonPath();
        assertThat(longFormat.getString("dataSetId"), is("1234"));
        assertThat(longFormat.getString("author"), is(security.getUserId()));
        assertThat(longFormat.getString("id"), is(preparationId));
        assertThat(longFormat.getList("actions").size(), is(0));
    }

    @Test
    public void testListCompatibleDataSets() throws Exception {
        // given
        final String dataSetId = createDataset( "dataset/dataset.csv", "compatible1", "text/csv" );
        final String dataSetId2 = createDataset( "dataset/dataset.csv", "compatible2", "text/csv" );
        final String dataSetId3 = createDataset("t-shirt_100.csv", "incompatible", "text/csv");
        final String preparationId = createPreparationFromDataset(dataSetId, "testPreparation");


        // when
        final String compatibleDatasetList = when().get("/api/preparations/{id}/basedatasets", preparationId).asString();

        // then
        assertTrue( compatibleDatasetList.contains( dataSetId2 ) );
        assertFalse( compatibleDatasetList.contains( dataSetId3 ) );
    }

    @Test
    public void testListCompatibleDataSetsWhenUniqueDatasetInRepository() throws Exception {
        // given
        final String dataSetId = createDataset( "dataset/dataset.csv", "unique", "text/csv" );
        final String preparationId = createPreparationFromDataset(dataSetId, "testPreparation");

        // when
        final String compatibleDatasetList = when().get("/api/preparations/{id}/basedatasets", preparationId).asString();

        // then
        assertFalse( compatibleDatasetList.contains( dataSetId ) );
    }


    @Test
    public void shouldCopyPreparation() throws Exception {
        // given
        folderRepository.addFolder("/destination");
        final String id = createPreparationFromFile("dataset/dataset.csv", "super preparation", "text/csv", "/from");

        // when
        final Response response = given() //
                .queryParam("destination", "/destination") //
                .queryParam("newName", "NEW super preparation") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .post("api/preparations/{id}/copy", id);

        // then
        assertEquals(200, response.getStatusCode());
        String copyId = response.asString();

        // check the folder entry
        final Iterator<FolderEntry> iterator = folderRepository.entries("/destination", PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry entry = iterator.next();
        assertEquals(entry.getContentId(), copyId);

        // check the name
        final Preparation actual = preparationRepository.get(copyId, Preparation.class);
        assertEquals("NEW super preparation", actual.getName());
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
        String source = "/source";
        folderRepository.addFolder(source);
        final String id = createPreparationFromFile("dataset/dataset.csv", "great_preparation", "text/csv", source);

        String destination = "/destination";
        folderRepository.addFolder(destination);

        // when
        final Response response = given() //
                .queryParam("folder", source) //
                .queryParam("destination", destination) //
                .queryParam("newName", "NEW great preparation") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .put("api/preparations/{id}/move", id);

        // then
        assertEquals(200, response.getStatusCode());

        // check the folder entry
        final Iterator<FolderEntry> iterator = folderRepository.entries(destination, PREPARATION).iterator();
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
        final String preparationId = createPreparationFromDataset("1234", "original_name");

        JsonPath longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is("original_name"));
        assertThat(longFormat.getList("id").size(), is(1));
        assertThat(longFormat.getList("id").get(0), is(preparationId));

        // when
        given().contentType(ContentType.JSON).body("{ \"name\": \"updated_name\", \"dataSetId\": \"1234\" }")
                .put("/api/preparations/{id}", preparationId).asString();

        // then
        longFormat = when().get("/api/preparations/?format=long").jsonPath();
        assertThat(longFormat.getList("name").size(), is(1));
        assertThat(longFormat.getList("name").get(0), is("updated_name"));
    }

    @Test
    public void testPreparationDelete() throws Exception {
        // given
        final String preparationId = createPreparationFromDataset("1234", "original_name");

        String list = when().get("/api/preparations").asString();
        assertTrue(list.contains(preparationId));

        // when
        when().delete("/api/preparations/" + preparationId).asString();

        // then
        list = when().get("/api/preparations").asString();
        assertEquals("[]", list);
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
        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath()
                .getList("steps");
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(rootStep.id()));
        assertThat(steps.get(1), is("c19c0f82ff8c2296acb4da9c485e6dc83ead6c45"));
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
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE")) ;
    }

    @Test
    public void should_update_action() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(rootStep.id()));
        assertThat(steps.get(1), is("81d219222e99d73b6a762cf2b0ec74261196df75")); // <- transformation/upper_case_lastname
        assertThat(steps.get(2), is("3505adaabdcdb1d7fd4e7d2898a2782ec572401d")); // <- upper_case_firstname

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59") with another action
        final String actionContent3 = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/lower_case_lastname.json"));
        given().contentType(ContentType.JSON)
                .body(actionContent3)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId,
                        "81d219222e99d73b6a762cf2b0ec74261196df75").then().statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        assertThat(steps.size(), is(3));
        assertThat(steps.get(0), is(rootStep.id()));
        assertThat(steps.get(1), is("434e7fc9005014ea6636a5c0803932db9dcc0943"));
        assertThat(steps.get(2), is("ef5c618b0648b5a9888e92c20424d00b7bbda180"));
    }

    @Test
    public void should_save_created_columns_ids_on_update() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        assertThat(steps.get(1), is("81d219222e99d73b6a762cf2b0ec74261196df75")); // <- transformation/upper_case_lastname

        // when : Update first action (transformation/upper_case_lastname / "2b6ae58738239819df3d8c4063e7cb56f53c0d59")
        // with another action that create a column
        final String updateAction = IOUtils.toString(PreparationAPITest.class.getResourceAsStream("transformation/copy_firstname.json"));
        given().contentType(ContentType.JSON)
                .body(updateAction)
                .put("/api/preparations/{preparation}/actions/{action}", preparationId,
                        "81d219222e99d73b6a762cf2b0ec74261196df75").then().statusCode(is(200));

        // then
        final JsonPath jsonPath = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath();
        final List<String> createdColumns = jsonPath.getList("diff[0].createdColumns");
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

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        final String firstStep = steps.get(1);

        // when
        final Response request = given().contentType(ContentType.JSON)//
                .body(missingScopeAction)//
                .when()//
                .put("/api/preparations/{id}/actions/{step}", preparationId, firstStep);

        // then
        request.then()//
                .statusCode(400)//
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE")) ;
    }

    @Test
    public void should_delete_preparation_action() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testPreparation", "text/csv");
        applyActionFromFile(preparationId, "transformation/upper_case_lastname.json");
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        final String firstStep = steps.get(1);

        // when
        given().delete("/api/preparations/{preparation}/actions/{action}", preparationId, firstStep) //
                .then() //
                .statusCode(is(200));

        // then : Steps id should have changed due to update
        steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
        assertThat(steps.size(), is(2));
        assertThat(steps.get(0), is(rootStep.id()));
        assertThat(steps.get(1), is("c19c0f82ff8c2296acb4da9c485e6dc83ead6c45"));
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
        final String newHead = preparationRepository.get(preparation.getHeadId(), Step.class).getParent();

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
        final String preparationId = createPreparationFromDataset("1234", "testPreparation");

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
        Iterator<FolderEntry> entries = folderRepository.entries("/", PREPARATION).iterator();
        assertFalse(entries.hasNext());

        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", "text/csv");

        // then
        entries = folderRepository.entries("/", PREPARATION).iterator();
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
        Iterator<FolderEntry> entries = folderRepository.entries(path, PREPARATION).iterator();
        assertFalse(entries.hasNext());

        // when
        final String preparationId = createPreparationFromFile("dataset/dataset.csv", "testCreatePreparation", "text/csv", path);

        // then
        entries = folderRepository.entries(path, PREPARATION).iterator();
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
        String json = given().get("/api/preparations/{preparation}/details", preparationId).asString();
        Preparation preparation = mapper.readerFor(Preparation.class).readValue(json);
        List<String> steps = preparation.getSteps();

        assertThat(steps.size(), is(1));
        assertThat(steps.get(0), is(rootStep.id()));

        // when
        applyActionFromFile(preparationId, "transformation/upper_case_firstname.json");

        // then
        steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath().getList("steps");
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
    public void shouldGetPreparationContentSample() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testPreparationContentGet", "text/csv");

        // when
        String preparationContent = given().get("/api/preparations/{preparation}/content?sample=53", preparationId).asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(preparationContent);
        JsonNode records = rootNode.get("records");
        assertThat(records.size(), is(53));
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

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath()
                .getList("steps");
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

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath()
                .getList("steps");
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
                + "   \"action\": {\n"
                + "         \"action\": \"uppercase\",\n"
                + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n"
                + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n"
                + "         }\n" //
                + "    }\n" //
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
                + "   \"action\": {\n"
                + "         \"action\": \"uppercase\",\n"
                + "         \"parameters\": {\n"
                + "             \"column_id\": \"0005\",\n"
                + "             \"column_name\": \"alive\"\n,"
                + "             \"scope\": \"column\"\n"
                + "         }\n" //
                + "    }\n" //
                + "}";
        final InputStream expectedPreviewStream = PreparationAPITest.class
                .getResourceAsStream("preview/expected_add_preview_on_dataset.json");

        // when
        final String preview = given().contentType(ContentType.JSON).body(input).when().post("/api/preparations/preview/add")
                .asString();

        // then
        assertThat(preview, sameJSONAsFile(expectedPreviewStream));
    }
}
