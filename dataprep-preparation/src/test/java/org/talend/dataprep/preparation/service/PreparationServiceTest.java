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

package org.talend.dataprep.preparation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.preparation.BasePreparationTest;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

/**
 * Unit test for the preparation service.
 */
public class PreparationServiceTest extends BasePreparationTest{

    @Autowired
    private PreparationUtils preparationUtils;

    @Test
    public void CORSHeaders() throws Exception {
        given().header("Origin", "fake.host.to.trigger.cors").when().get("/preparations").then()
                .header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
    }

    // ------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------GETTER------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldListAllPreparations() throws Exception {
        // given
        when().get("/preparations/details").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));

        final Preparation preparation = new Preparation("#548425458", "1234", rootStep.id(), versionService.version().getVersionId());
        preparation.setCreationDate(0);
        preparation.setLastModificationDate(12345);
        repository.add(preparation);

        // when
        final Response response = when().get("/preparations/details");

        // then
        response.then().statusCode(HttpStatus.OK.value())
                .body(sameJSONAs("[{\"id\":\"#548425458\"," + "\"dataSetId\":\"1234\","
                        + "\"author\":null," + "\"name\":null," + "\"creationDate\":0," + "\"lastModificationDate\":12345,"
                        + "\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"]," + "\"diff\":[]," + "\"actions\":[],"
                        + "\"metadata\":[]" + "}]"));

        // given
        final Preparation preparation1 = new Preparation("#1438725", "5678", rootStep.id(), versionService.version().getVersionId());
        preparation1.setCreationDate(500);
        preparation1.setLastModificationDate(456789);
        repository.add(preparation1);

        // when
        final Response responseMultiple = when().get("/preparations/details");

        // then
        //@formatter:off
        responseMultiple.then()
                .statusCode(HttpStatus.OK.value())
                .body(sameJSONAs(
                        "[" +
                         "{\"id\":\"#548425458\"," +
                                "\"dataSetId\":\"1234\"," +
                                "\"author\":null," +
                                "\"name\":null," +
                                "\"creationDate\":0," +
                                "\"lastModificationDate\":12345," +
                                "\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"]," +
                                "\"diff\":[]," +
                                "\"actions\":[]," +
                                "\"metadata\":[]" +
                          "}," +
                          "{\"id\":\"#1438725\"," +
                                "\"dataSetId\":\"5678\"," +
                                "\"author\":null," +
                                "\"name\":null," +
                                "\"creationDate\":500," +
                                "\"lastModificationDate\":456789," +
                                "\"steps\":[\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"]," +
                                "\"diff\":[]," +
                                "\"actions\":[]," +
                                "\"metadata\":[]" +
                          "}" +
                          "]")
                        .allowingAnyArrayOrdering());
        //@formatter:on
    }

    @Test
    public void shouldListByFolder() throws Exception {

        // given
        folderRepository.addFolder("/root");
        List<String> rootPreparations = new ArrayList<>();
        rootPreparations.add(createPreparationWithAPI("{\"name\": \"prep_1\", \"dataSetId\": \"1234\"}", "/root"));

        folderRepository.addFolder("/root/three_preps");
        List<String> threePreparations = new ArrayList<>();
        threePreparations.add(createPreparationWithAPI("{\"name\": \"prep_2\", \"dataSetId\": \"1234\"}", "/root/three_preps"));
        threePreparations.add(createPreparationWithAPI("{\"name\": \"prep_3\", \"dataSetId\": \"1234\"}", "/root/three_preps"));
        threePreparations.add(createPreparationWithAPI("{\"name\": \"prep_4\", \"dataSetId\": \"1234\"}", "/root/three_preps"));

        folderRepository.addFolder("/root/three_preps/no_prep");
        List<String> noPreparations = new ArrayList<>();

        // then
        checkSearchFolder("/root", rootPreparations);
        checkSearchFolder("/root/three_preps", threePreparations);
        checkSearchFolder("/root/three_preps/no_prep", noPreparations);
    }

    /**
     * Check that the folder search is correct.
     *
     * @param folderPath the folder to search.
     * @param expectedIds the expected preparations id.
     * @throws IOException if an error occurs.
     */
    private void checkSearchFolder(String folderPath, List<String> expectedIds) throws IOException {

        // when
        final Response response = given() //
                .queryParam("folder", folderPath) //
                .queryParam("sort", "DATE") //
                .queryParam("order", "ASC") //
                .when().expect().statusCode(200).log().ifError() //
                .get("/preparations/search");

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.reader().readTree(response.asInputStream());
        assertTrue(rootNode.isArray());
        assertEquals(expectedIds.size(), rootNode.size());
        final List<String> actualIds = StreamSupport.stream(rootNode.spliterator(), false).map(n -> n.get("id").asText()).collect(Collectors.toList());
        assertEquals(expectedIds, actualIds);
    }


    @Test
    public void shouldSearchByName() throws Exception {

        // given
        final String tdpId = createPreparation("1234", "talendDataPrep");
        final String dpId = createPreparation("4567", "dataPrep");
        final String pId = createPreparation("8901", "prep");

        // when then
        checkSearchByName("prep", false, Arrays.asList(pId, dpId, tdpId));

    }

    private void checkSearchByName(String name, boolean exactMatch, List<String> expectedIds) throws IOException {

        // when
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("exactMatch", exactMatch) //
                .queryParam("sort", "DATE") //
                .queryParam("order", "ASC") //
                .when().expect().statusCode(200).log().ifError() //
                .get("/preparations/search");

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.reader().readTree(response.asInputStream());
        assertTrue(rootNode.isArray());
        assertEquals(expectedIds.size(), rootNode.size());
        final List<String> actualIds = StreamSupport.stream(rootNode.spliterator(), false).map(n -> n.get("id").asText()).collect(Collectors.toList());
        assertEquals(expectedIds.size(), actualIds.stream().filter(expectedIds::contains).count());
    }

    /**
     * See <a href="https://jira.talendforge.org/browse/TDP-1604">TDP-1604</a>
     */
    @Test
    public void listPreparationsShouldBeOrderedByLastModificationDate() throws Exception {
        // given
        when().get("/preparations/details").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));

        List<String> preparationIds = new ArrayList<>(5);
        for (int i=0;i<5;i++) {
            final long now = new Date().getTime();
            Preparation preparation = new Preparation(UUID.randomUUID().toString(), "12345", rootStep.id(), versionService.version().getVersionId());
            preparation.setName("prep-"+i);
            preparation.setLastModificationDate(now);
            repository.add(preparation);
            preparationIds.add(0, preparation.id()); // last modified preparation is first
            Thread.sleep(100); // to make sure the last modification date is older
        }

        // when
        final InputStream responseContent = when().get("/preparations/details").asInputStream();
        final List<Preparation> actual = mapper.readValue(responseContent, new TypeReference<List<Preparation>>() {});

        // then
        assertEquals(preparationIds.size(), actual.size());
        for(int i=0; i<actual.size(); i++) {
            assertEquals(preparationIds.get(i), actual.get(i).getId());
        }
    }

    @Test
    public void list() throws Exception {
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));
        final Preparation preparation1 = new Preparation("#18875", "1234", rootStep.id(), versionService.version().getVersionId());
        preparation1.setCreationDate(0);
        repository.add(preparation1);
        when().get("/preparations").then().statusCode(HttpStatus.OK.value())
                .body(sameJSONAs("[\"#18875\"]"));
        final Preparation preparation2 = new Preparation("#483275", "5678", rootStep.id(), versionService.version().getVersionId());
        preparation2.setCreationDate(0);
        repository.add(preparation2);
        List<String> list = when().get("/preparations").jsonPath().getList("");
        assertThat(list, hasItems("#18875", "#483275"));
    }

    @Test
    public void get() throws Exception {
        Preparation preparation = new Preparation("8b6281c5e99c41313a83777c3ab43b06adda9e5c", "1234", rootStep.id(), versionService.version().getVersionId());
        preparation.setCreationDate(0);
        preparation.setLastModificationDate(12345);
        repository.add(preparation);
        String preparationDetails = when().get("/preparations/{id}", preparation.id()).asString();
        InputStream expected = PreparationServiceTest.class.getResourceAsStream("preparation_1234.json");
        assertThat(preparationDetails, sameJSONAsFile(expected));
    }


    @Test
    public void cannotCopyUnknownPreparation() throws Exception {

        //given
        String unknownId = "0001";

        // when
        final Response response = given() //
                .queryParam("name", "the new preparation") //
                .queryParam("destination", "new preparation") //
                .when() //
                .expect().statusCode(404).log().ifError() //
                .post("/preparations/{id}/copy", unknownId);

        // then
        assertThat(response.getStatusCode(), is(404));

    }

    @Test
    public void shouldCopy() throws Exception {
        // given
        String from = "/from";
        String to = "/to";
        folderRepository.addFolder(from);
        folderRepository.addFolder(to);

        String originalId = createPreparationWithAPI("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}", from);

        // when
        final Response response = given() //
                .queryParam("name", "the new preparation") //
                .queryParam("destination", to) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations/{id}/copy", originalId);
        String copyId = response.asString();

        // then
        assertThat(response.getStatusCode(), is(200));
        final Iterator<FolderEntry> iterator = folderRepository.entries(to, PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry copy = iterator.next();
        assertEquals(copy.getContentId(), copyId);
        assertEquals("the new preparation", repository.get(copyId, Preparation.class).getName());
    }

    @Test
    public void cannotCopyIfAnExistingPreparationAlrdeayExists() throws Exception {
        // given
        String folder = "/great_folder";
        String name = "my preparation";
        folderRepository.addFolder(folder);

        String originalId = createPreparationWithAPI("{\"name\": \""+ name +"\", \"dataSetId\": \"1234\"}", folder);

        // when
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("destination", folder) //
                .when() //
                .expect().statusCode(409).log().ifError() //
                .post("/preparations/{id}/copy", originalId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void shouldCopyWithDefaultParameters() throws Exception {
        // given
        folderRepository.addFolder("/");
        folderRepository.addFolder("/yet_another_folder");

        String originalId = createPreparationWithAPI("{\"name\": \"prep_1\", \"dataSetId\": \"1234\"}", "/yet_another_folder");

        // when
        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations/{id}/copy", originalId);
        String copyId = response.asString();

        // then
        assertThat(response.getStatusCode(), is(200));
        final Iterator<FolderEntry> iterator = folderRepository.entries("/", PREPARATION).iterator();
        boolean found = false;
        while (iterator.hasNext()) {
            final FolderEntry entry = iterator.next();
            if (entry.getContentId().equals(copyId)) {
                found = true;
                assertEquals("prep_1 Copy", repository.get(entry.getContentId(), Preparation.class).getName());
            }
        }
        assertTrue(found);
    }


    @Test
    public void shouldMove() throws Exception {
        // given
        String from = "/from";
        String to = "/to";
        folderRepository.addFolder(from);
        folderRepository.addFolder(to);

        String originalId = createPreparationWithAPI("{\"name\": \"test_move\", \"dataSetId\": \"7535\"}", from);

        // when
        final Response response = given() //
                .queryParam("folder", from) //
                .queryParam("destination", to) //
                .queryParam("newName", "moved preparation") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/move", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Iterator<FolderEntry> iterator = folderRepository.entries(to, PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry copy = iterator.next();
        assertEquals(copy.getContentId(), originalId);
        assertEquals("moved preparation", repository.get(originalId, Preparation.class).getName());
    }


    @Test
    public void cannotMovePreparationThatDoesNotExist() throws Exception {

        String name="super preparation";

        // given
        String from = "/from";
        folderRepository.addFolder(from);
        String preparationId = createPreparationWithAPI("{\"name\": \"another preparation\", \"dataSetId\": \"7535\"}", from);

        String to = "/to";
        folderRepository.addFolder(to);
        createPreparationWithAPI("{\"name\": \""+ name +"\", \"dataSetId\": \"7384\"}", to);

        // when
        final Response response = given() //
                .queryParam("folder", from) //
                .queryParam("destination", to) //
                .queryParam("newName", name) //
                .when() //
                .expect().statusCode(409).log().ifError() //
                .put("/preparations/{id}/move", preparationId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void shouldMoveWithDefaultParameters() throws Exception {
        // given
        String from = "/from";
        String to = "/";
        folderRepository.addFolder(from);
        folderRepository.addFolder(to);

        String originalId = createPreparationWithAPI("{\"name\": \"yap\", \"dataSetId\": \"7535\"}", from);

        // when
        final Response response = given() //
                .queryParam("folder", from) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/move", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Iterator<FolderEntry> iterator = folderRepository.entries(to, PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry copy = iterator.next();
        assertEquals(copy.getContentId(), originalId);
        assertEquals("yap", repository.get(originalId, Preparation.class).getName());
    }

    @Test
    public void cannotMovePreparationBecauseTheNameIsAlreadyTaken() throws Exception {

        // when
        final Response response = given() //
                .queryParam("newName", "moved preparation") //
                .when() //
                .expect().statusCode(404).log().ifError() //
                .put("/preparations/{id}/move", "ABC123XYZ");

        // then
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldReturnListOfSpecificDatasetPreparations() throws Exception {
        // given : relevant preparation
        final String wantedDataSetId = "wanted";
        final String preparation1 = createPreparation(wantedDataSetId, "prep_1");
        final String preparation2 = createPreparation(wantedDataSetId, "prep_2");

        // given : noise data not to be returned by the service
        final String preparation3 = createPreparation("4523", "prep_3");
        final String preparation4 = createPreparation("7534", "prep_4");
        final String preparation5 = createPreparation("1598", "prep_5");

        // when
        final String result = when().get("/preparations/search?dataSetId=" + wantedDataSetId).asString();
        final List<String> preparationIds = JsonPath.from(result).get("id");

        // then
        assertThat(preparationIds, hasItem(preparation1));
        assertThat(preparationIds, hasItem(preparation2));
        assertThat(preparationIds, not(hasItem(preparation3)));
        assertThat(preparationIds, not(hasItem(preparation4)));
        assertThat(preparationIds, not(hasItem(preparation5)));
    }

    @Test
    public void shouldLocatePreparation() throws Exception {

        // given
        final Folder bar = folderRepository.addFolder("/foo/bar");
        final String barEntry = createPreparationWithAPI("{\"name\": \"youpi\", \"dataSetId\": \"4824\"}", bar.getPath());

        // when
        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/preparations/{id}/folder", barEntry);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Folder actual = mapper.readValue(response.asInputStream(), Folder.class);
        assertEquals(bar, actual);
    }

    @Test
    public void locateUnknownPreparationShouldSend404 () throws Exception {
        // when
        final Response response = given() //
                .when() //
                .expect().statusCode(404).log().ifError() //
                .get("/preparations/{id}/folder", "unknown preparation");

        // then
        assertThat(response.getStatusCode(), is(404));
    }


    @Test
    public void shouldListErrors() throws Exception {
        // when
        final String errors = when().get("/preparations/errors").asString();

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualErrorCodes = mapper.readTree(errors);

        // then
        assertTrue(actualErrorCodes.isArray());
        assertTrue(actualErrorCodes.size() > 0);
        for (final JsonNode currentCode : actualErrorCodes) {
            assertTrue(currentCode.has("code"));
            assertTrue(currentCode.has("http-status-code"));
        }
    }

    @Test
    public void shouldCopySteps() throws Exception {

        // given
        final String referenceId = createPreparation("1234", "reference");
        applyTransformation(referenceId, "upper_case.json");
        applyTransformation(referenceId, "lower_case.json");
        final Preparation reference = repository.get(referenceId, Preparation.class);

        final String preparationId = createPreparation("42321", "preparation");

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        assertEquals(preparation.getHeadId(), reference.getHeadId());
    }

    @Test
    public void shouldNotCopyStepsFromPreparationNotFound() throws Exception {
        // when
        final Response response = given() //
                .param("from", "reference") //
                .when() //
                .expect().statusCode(404).log().ifError() //
                .put("/preparations/{id}/steps/copy", "prepNotFound");

        // then
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldNotCopyStepsBecausePreparationIsNotEmpty() throws Exception {

        // given
        final String referenceId = createPreparation("8436587", "reference");
        applyTransformation(referenceId, "upper_case.json");
        applyTransformation(referenceId, "lower_case.json");

        final String preparationId = createPreparation("42321", "preparation");
        applyTransformation(preparationId, "upper_case.json");

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .when() //
                //.expect().statusCode(409).log().ifError() //
                .put("/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void shouldNotCopyStepsBecauseReferencePreparationIsNotFound() throws Exception {

        // given
        final String preparationId = createPreparation("154753", "preparation");
        Preparation expected = repository.get(preparationId, Preparation.class);

        // when
        final Response response = given() //
                .param("from", "not to be found") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
        Preparation actual = repository.get(preparationId, Preparation.class);
        assertEquals(expected, actual);
    }


    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------LIFECYCLE----------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void create() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = createPreparationWithAPI("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}");
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("test_name"));
        assertThat(preparation.getAuthor(), is(System.getProperty("user.name")));
        assertThat(preparation.getAppVersion(), is(versionService.version().getVersionId()));
    }

    @Test
    public void createInFolder() throws Exception {
        // given
        final String path = "/test/create/preparation";
        folderRepository.addFolder(path);
        assertThat(folderRepository.children(path).iterator().hasNext(), is(false));

        // when
        String preparationId = createPreparationWithAPI("{\"name\": \"another_preparation\", \"dataSetId\": \"75368\"}", path);

        // then
        final Iterator<FolderEntry> iterator = folderRepository.entries(path, PREPARATION).iterator();
        assertTrue(iterator.hasNext());
        final FolderEntry entry = iterator.next();
        assertEquals(preparationId, entry.getContentId());
        assertEquals(PREPARATION, entry.getContentType());
    }

    @Test
    public void createWithSpecialCharacters() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON)
                .body("{\"name\": \"éàçè\", \"dataSetId\": \"1234\"}".getBytes("UTF-8")).when().post("/preparations").asString();
        assertThat(preparationId, is(preparationId));
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void delete() throws Exception {
        // given
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String path="preparation/that/will/be/deleted";
        folderRepository.addFolder(path);
        String preparationId = createPreparationWithAPI("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}", path);
        assertThat(repository.listAll(Preparation.class).size(), is(1));
        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("test_name"));
        assertTrue(folderRepository.findFolderEntries(preparationId, PREPARATION).iterator().hasNext());

        // when
        when().delete("/preparations/{id}", preparationId).then().statusCode(HttpStatus.OK.value());

        // then
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        assertFalse(folderRepository.findFolderEntries(preparationId, PREPARATION).iterator().hasNext());
    }

    @Test
    public void update() throws Exception {
        assertThat(repository.listAll(Preparation.class).size(), is(0));
        String preparationId = given().contentType(ContentType.JSON).body("{\"name\": \"test_name\", \"dataSetId\": \"1234\"}")
                .when().post("/preparations").asString();

        Preparation preparation = repository.listAll(Preparation.class).iterator().next();
        assertThat(preparationId, is(preparation.getId()));

        long oldModificationDate = preparation.getLastModificationDate();

        // Test preparation details update
        String updatedId = given().contentType(ContentType.JSON)
                .body("{\"name\": \"test_name_updated\", \"dataSetId\": \"1234\"}")
                .when().put("/preparations/{id}", preparationId).asString();

        // Preparation id should not change (name is not part of preparation id).
        assertThat(updatedId, is(preparationId));
        Collection<Preparation> preparations = repository.listAll(Preparation.class);
        assertThat(preparations.size(), is(1));
        preparation = preparations.iterator().next();
        assertThat(preparation.getName(), is("test_name_updated"));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));
        assertThat(preparation.getAppVersion(), is(versionService.version().getVersionId()));
    }

    @Test
    public void updateWithSpecialArguments() throws Exception {
        Preparation preparation = new Preparation("#123", versionService.version().getVersionId());
        preparation.setDataSetId("1234");
        preparation.setName("test_name");
        String preparationId = given().contentType(ContentType.JSON).body(mapper.writer().writeValueAsBytes(preparation))
                .when().post("/preparations").asString();

        // Test preparation details update
        String updatedId = given().contentType(ContentType.JSON)
                .body("{\"name\": \"éàçè\", \"dataSetId\": \"1234\"}".getBytes("UTF-8")).when()
                .put("/preparations/{id}", preparationId).asString();

        // Preparation id should not change (new name)
        assertThat(updatedId, is(preparationId));
        Collection<Preparation> preparations = repository.listAll(Preparation.class);
        assertThat(preparations.size(), is(1));
        preparation = preparations.iterator().next();
        assertThat(preparation.id(), is(updatedId));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void shouldChangePreparationHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(secondStepId));

        // when
        given().when()//
                .put("/preparations/{id}/head/{stepId}", preparationId, firstStepId)//
                .then()//
                .statusCode(200);

        // then
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(firstStepId));
    }

    @Test
    public void shouldThrowExceptionOnPreparationHeadChangeWithUnknownStep() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(firstStepId));

        // when
        final Response response = given().when()//
                .put("/preparations/{id}/head/{stepId}", preparationId, "unknown_step_id");

        // then
        response.then()//
                .statusCode(404)//
                .assertThat()//
                .body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------APPEND STEP----------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldAddActionStepAfterHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getHeadId(), is(rootStep.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname.json");

        // then
        final String expectedStepId = "907741a33bba6e7b3c6c2e4e7d1305c6bd0644b8";

        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getHeadId(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headAction.getActions(), hasSize(1));
        assertThat(headAction.getActions().get(0).getName(), is("copy"));
    }

    @Test
    public void shouldAddActionWithFilterStepAfterHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getHeadId(), is(rootStep.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname_filter.json");

        // then
        final String expectedStepId = "1306e2ac2526530ec8cd75206eeaa4191eafe4fa";

        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getHeadId(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headAction.getActions(), hasSize(1));
        final Action copyAction = headAction.getActions().get(0);
        assertThat(copyAction.getName(), is("copy"));
        assertThat(copyAction.getParameters().get(ImplicitParameters.FILTER.getKey()),
                is("{\"eq\":{\"field\":\"0001\",\"value\":\"value\"}}"));
    }

    @Test
    public void shouldSaveStepDiff() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final Preparation preparation = repository.get(preparationId, Preparation.class);

        assertThat(preparation.getHeadId(), is(rootStep.getId()));

        // when
        applyTransformation(preparationId, "copy_lastname.json");

        // then
        final String expectedStepId = "907741a33bba6e7b3c6c2e4e7d1305c6bd0644b8";
        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(rootStep.getId()));
        assertThat(head.getDiff().getCreatedColumns(), hasSize(1));
        assertThat(head.getDiff().getCreatedColumns(), hasItem("0006"));
    }

    @Test
    public void shouldReturnErrorWhenScopeIsNotConsistentOnAppendTransformation() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");

        // when
        final Response request = given()
                .body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("incomplete_transformation_params.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId);

        // then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------UPDATE STEP----------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldModifySingleAction() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        assertThat(firstStepId, is("22b9436476d28dcf800dddd11aa7441f20c0c1e9"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .put("/preparations/{id}/actions/{action}", preparationId, firstStepId);

        // then
        final String expectedStepId = "971ff04a46557df17bc1302f2fa2dbcac935086b";
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(rootStep.getId()));
    }

    @Test
    public void shouldModifyLastAction() throws Exception {
        // Initial preparation
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");
        assertThat(secondStepId, is("3fdaae0ccfd3bfd869790999670106535cdbccf1"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when : update second (last) step
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))
                .contentType(ContentType.JSON).when().put("/preparations/{id}/actions/{action}", preparationId, secondStepId);

        // then
        final String expectedStepId = "3086490ebc8ac72475d249010b0ff67c38ae3454";
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(expectedStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(expectedStepId, Step.class);
        assertThat(head.getParent(), is(firstStepId));
    }

    @Test
    public void shouldModifyFirstAction() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        assertThat(firstStepId, is("22b9436476d28dcf800dddd11aa7441f20c0c1e9"));
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");
        assertThat(secondStepId, is("3fdaae0ccfd3bfd869790999670106535cdbccf1"));

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("upper_case_modified.json")))
                .contentType(ContentType.JSON).when()
                .put("/preparations/{id}/actions/{action}", preparation.id(), "a41184275b046d86c8d98d413ed019bc0a7f3c49");

        // then
        final String expectedFirstStepId = "22b9436476d28dcf800dddd11aa7441f20c0c1e9";
        final String expectedSecondStepId = "3fdaae0ccfd3bfd869790999670106535cdbccf1";

        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(expectedSecondStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThanOrEqualTo(oldModificationDate)));

        final Step head = repository.get(expectedSecondStepId, Step.class);
        assertThat(head.getParent(), is(expectedFirstStepId));

        final Step first = repository.get(expectedFirstStepId, Step.class);
        assertThat(first.getParent(), is(rootStep.getId()));
    }

    @Test
    public void shouldSaveUpdatedStepDiffAndShiftColumnsIds() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String step0 = applyTransformation(preparationId, "update/0.copy_birth.json");
        final String step1 = applyTransformation(preparationId, "update/1.split_2_columns.json");
        final String step2 = applyTransformation(preparationId, "update/2.copy_firstname_after_split.json");
        final String step3 = applyTransformation(preparationId, "update/3.rename_copy_firstname.json");
        final String step4 = applyTransformation(preparationId, "update/4.copy_lastname.json");

        assertThatStepHasCreatedColumns(step0, "0007");
        assertThatStepHasCreatedColumns(step1, "0008", "0009");
        assertThatStepHasCreatedColumns(step2, "0010");
        assertThatStepIsOnColumn(step3, "0010");
        assertThatStepHasCreatedColumns(step4, "0011");

        // when : +1 column
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("update/1bis.split_3_columns.json")))
                .contentType(ContentType.JSON).when().put("/preparations/{id}/actions/{action}", preparationId, step1);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertThatStepHasCreatedColumns(stepIds.get(1), "0007"); // id < 0009 : do not change
        assertThatStepHasCreatedColumns(stepIds.get(2), "0008", "0009", "0010"); // +1 column
        assertThatStepHasCreatedColumns(stepIds.get(3), "0011"); // id >= 0009 : shift +1
        assertThatStepIsOnColumn(stepIds.get(4), "0011"); // id >= 0009 : shift + 1
        assertThatStepHasCreatedColumns(stepIds.get(5), "0012"); // id >= 0009 : shift + 1
    }

    @Test
    public void shouldReturnErrorWhenScopeIsNotConsistentOnTransformationUpdate() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String stepId = applyTransformation(preparationId, "upper_case.json");

        // when
        final Response request = given()
                .body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream("incomplete_transformation_params.json")))//
                .contentType(ContentType.JSON)//
                .when()//
                .put("/preparations/{id}/actions/{action}", preparationId, stepId);

        // then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_ALL_MISSING_ACTION_SCOPE"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------DELETE STEP----------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldDeleteSingleStep() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        final String firstStepId = applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(secondStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, firstStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String headId = preparation.getHeadId();

        head = repository.get(headId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(1));
        assertThat(headActions.getActions().get(0).getName(), is("lowercase"));
    }

    @Test
    public void shouldDeleteStepsThatApplyToCreatedColumn() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "copy_lastname.json");
        applyTransformation(preparationId, "rename_copy_lastname.json");
        final String headStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, copyStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getHeadId();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("lowercase"));
    }

    @Test
    public void shouldShiftColumnCreatedAfterStepWithAllActionsParametersOnThoseSteps() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "copy_lastname.json");
        applyTransformation(preparationId, "rename_copy_lastname.json");
        applyTransformation(preparationId, "copy_firstname.json");
        applyTransformation(preparationId, "rename_copy_firstname.json");
        final String headStepId = applyTransformation(preparationId, "lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(6));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("copy"));
        assertThat(headActions.getActions().get(4).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(5).getName(), is("lowercase"));

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, copyStepId)//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getHeadId();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));

        final Map<String, String> renameColumnFirstnameParameters = headActions.getActions().get(2).getParameters();
        assertThat(renameColumnFirstnameParameters.get("column_name"), is("firstname")); // check we have the rename
                                                                                         // firstname action
        assertThat(renameColumnFirstnameParameters.get("column_id"), is("0006")); // shifted id, was 0007

        final Map<String, String> copyColumnFirstnameParameters = headActions.getActions().get(1).getParameters();
        assertThat(copyColumnFirstnameParameters.get("column_name"), is("firstname")); // check we have the copy
                                                                                       // firstname action

        final Step renameCopyFirstnameStep = repository.get(head.getParent(), Step.class);
        final Step copyFirstnameStep = repository.get(renameCopyFirstnameStep.getParent(), Step.class);
        assertThat(copyFirstnameStep.getDiff().getCreatedColumns(), hasSize(1));
        assertThat(copyFirstnameStep.getDiff().getCreatedColumns(), hasItem("0006")); // shifted id, was 0007
    }

    @Test
    public void shouldThrowExceptionWhenStepToDeleteIsRoot() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        applyTransformation(preparationId, "lower_case.json");

        // when: delete ROOT
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, rootStep.getId());

        // then
        response.then().statusCode(403).assertThat().body("code", is("TDP_PS_PREPARATION_ROOT_STEP_CANNOT_BE_DELETED"));
    }

    @Test
    public void shouldThrowExceptionWhenStepDoesntExist() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        applyTransformation(preparationId, "lower_case.json");

        // when : delete unknown step
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, "azerty");

        // then
        response.then().statusCode(404).assertThat().body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void shouldUpdateModificationDateOnDeleteStep() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when: delete LOWERCASE
        when().delete("/preparations/{id}/actions/{action}", preparation.id(), secondStepId);

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(oldModificationDate, lessThan(preparation.getLastModificationDate()));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------UTILS-------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Create a new preparation with the given name and creation date to 0.
     *
     * @param datasetId the dataset id related to this preparation.
     * @param name preparation name.
     * @return The preparation id
     */
    private String createPreparation(final String datasetId, final String name) {
        final Preparation preparation = new Preparation(UUID.randomUUID().toString(), datasetId, rootStep.id(), versionService.version().getVersionId());
        preparation.setName(name);
        preparation.setCreationDate(0);
        repository.add(preparation);
        return preparation.id();
    }

    /**
     * Append an action to a preparation
     *
     * @param preparationId The preparation id
     * @param transformationFilePath The transformation json file path
     * @return The created step id
     */
    private String applyTransformation(final String preparationId, final String transformationFilePath) throws IOException {
        given().body(IOUtils.toString(PreparationServiceTest.class.getResourceAsStream(transformationFilePath)))//
                .contentType(ContentType.JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId)//
                .then()//
                .statusCode(200);

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        return preparation.getHeadId();
    }

    /**
     * Assert that step has exactly the wanted created columns ids
     *
     * @param stepId The step id
     * @param columnsIds The created columns ids
     */
    private void assertThatStepHasCreatedColumns(final String stepId, final String... columnsIds) {
        final Step head = repository.get(stepId, Step.class);
        assertThat(head.getDiff().getCreatedColumns(), hasSize(columnsIds.length));
        for (final String columnId : columnsIds) {
            assertThat(head.getDiff().getCreatedColumns(), hasItem(columnId));
        }
    }

    /**
     * Assert that the step is on the wanted column id
     *
     * @param stepId The step id
     * @param columnId The column id
     */
    private void assertThatStepIsOnColumn(final String stepId, final String columnId) {
        final Step step = repository.get(stepId, Step.class);
        final PreparationActions stepActions = repository.get(step.getContent(), PreparationActions.class);
        final int stepActionIndex = stepActions.getActions().size() - 1;

        final Map<String, String> stepParameters = stepActions.getActions().get(stepActionIndex).getParameters();
        assertThat(stepParameters.get("column_id"), is(columnId));
    }
}
