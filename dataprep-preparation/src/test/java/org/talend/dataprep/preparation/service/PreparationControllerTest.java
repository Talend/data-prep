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

package org.talend.dataprep.preparation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.preparation.service.EntityBuilder.*;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.preparation.BasePreparationTest;
import org.talend.dataprep.test.LocalizationRule;
import org.talend.dataprep.test.MockTDPException;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.util.SortAndOrderHelper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * Unit test for the preparation service.
 */
public class PreparationControllerTest extends BasePreparationTest {

    @Autowired
    private PreparationUtils preparationUtils;

    @Rule
    public LocalizationRule rule = new LocalizationRule(Locale.US);

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

        final Preparation preparation = new Preparation("#548425458", "1234", Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation.setCreationDate(0);
        preparation.setLastModificationDate(12345);
        repository.add(preparation);

        // when
        final Response response = when().get("/preparations/details");

        // then
        response.then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[{\"id\":\"#548425458\"," + "\"dataSetId\":\"1234\","
                + "\"creationDate\":0," + "\"lastModificationDate\":12345}]").allowingExtraUnexpectedFields());

        // given
        final Preparation preparation1 = new Preparation("#1438725", "5678", Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
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
                                "\"creationDate\":0," +
                                "\"lastModificationDate\":12345," +
                                "}," +
                                "{\"id\":\"#1438725\"," +
                                "\"dataSetId\":\"5678\"," +
                                "\"creationDate\":500," +
                                "\"lastModificationDate\":456789," +
                                "}" +
                                "]")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering());
        //@formatter:on
    }

    @Test
    public void shouldListByFolder() throws Exception {
        // given
        final Folder rootFolder = folderRepository.addFolder(home.getId(), "/root");
        final List<String> rootPreparations = new ArrayList<>(1);
        rootPreparations.add(clientTest.createPreparation(createTestPreparation("prep_1", "1234"), rootFolder.getId()).getId());

        final Folder threePrepsFolder = folderRepository.addFolder(rootFolder.getId(), "three_preps");
        final List<String> threePreparations = new ArrayList<>(3);
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_2", "1234"), threePrepsFolder.getId()).getId());
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_3", "1234"), threePrepsFolder.getId()).getId());
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_4", "1234"), threePrepsFolder.getId()).getId());

        final Folder noPrepsFolder = folderRepository.addFolder(threePrepsFolder.getId(), "no_prep");
        List<String> noPreparations = new ArrayList<>();

        // then
        checkSearchFolder(rootFolder.getId(), rootPreparations, SortAndOrderHelper.Sort.CREATION_DATE.camelName());
        checkSearchFolder(threePrepsFolder.getId(), threePreparations, SortAndOrderHelper.Sort.CREATION_DATE.camelName());
        checkSearchFolder(noPrepsFolder.getId(), noPreparations, SortAndOrderHelper.Sort.CREATION_DATE.camelName());
    }

    @Test
    public void test_TDP_2158() throws Exception {
        // given
        final Folder rootFolder = folderRepository.addFolder(home.getId(), "/root");
        final List<String> rootPreparations = new ArrayList<>(1);
        rootPreparations.add(clientTest.createPreparation(createTestPreparation("prep_2", "1234"), rootFolder.getId()).getId());

        final Folder threePrepsFolder = folderRepository.addFolder(rootFolder.getId(), "three_preps");
        final List<String> threePreparations = new ArrayList<>(3);
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_2", "1234"), threePrepsFolder.getId()).getId());
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_3", "1234"), threePrepsFolder.getId()).getId());
        threePreparations
                .add(clientTest.createPreparation(createTestPreparation("prep_4", "1234"), threePrepsFolder.getId()).getId());

        final Folder noPrepsFolder = folderRepository.addFolder(threePrepsFolder.getId(), "no_prep");
        List<String> noPreparations = new ArrayList<>();

        // then
        checkSearchFolder(rootFolder.getId(), rootPreparations, SortAndOrderHelper.Sort.CREATION_DATE.camelName());
        checkSearchFolder(threePrepsFolder.getId(), threePreparations, SortAndOrderHelper.Sort.NAME.camelName());
        checkSearchFolder(noPrepsFolder.getId(), noPreparations, SortAndOrderHelper.Sort.CREATION_DATE.camelName());
    }

    /**
     * Check that the folder search is correct.
     *
     * @param folderId the folder to search.
     * @param expectedIds the expected preparations id.
     * @throws IOException if an error occurs.
     */
    private void checkSearchFolder(final String folderId, final List<String> expectedIds, String sort) throws IOException {
        // when
        final Response response = given() //
                .queryParam("folderId", folderId) //
                .queryParam("sort", sort) //
                .queryParam("order", "asc") //
                .when().expect().statusCode(200).log().ifError() //
                .get("/preparations/search");

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.reader().readTree(response.asInputStream());
        assertTrue(rootNode.isArray());
        assertEquals(expectedIds.size(), rootNode.size());
        final List<String> actualIds = StreamSupport.stream(rootNode.spliterator(), false).map(n -> n.get("id").asText())
                .collect(Collectors.toList());
        assertEquals(expectedIds, actualIds);
    }

    @Test
    public void shouldSearchByName() throws Exception {
        // given
        final String tdpId = createPreparation("1234", "talendDataPrep");
        final String dpId = createPreparation("4567", "dataPrep");
        final String pId = createPreparation("8901", "prep");

        // when then
        checkSearchByName("prep", false, asList(pId, dpId, tdpId));
    }

    private void checkSearchByName(String name, boolean exactMatch, List<String> expectedIds) throws IOException {
        // when
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("exactMatch", exactMatch) //
                .queryParam("sort", "creationDate") //
                .queryParam("order", "asc") //
                .when().expect().statusCode(200).log().ifError() //
                .get("/preparations/search");

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.reader().readTree(response.asInputStream());
        assertTrue(rootNode.isArray());
        assertEquals(expectedIds.size(), rootNode.size());
        final List<String> actualIds = StreamSupport.stream(rootNode.spliterator(), false).map(n -> n.get("id").asText())
                .collect(Collectors.toList());
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
        for (int i = 0; i < 5; i++) {
            final long now = new Date().getTime();
            Preparation preparation = new Preparation(UUID.randomUUID().toString(), "12345", Step.ROOT_STEP.id(),
                    versionService.version().getVersionId());
            preparation.setName("prep-" + i);
            preparation.setLastModificationDate(now);
            repository.add(preparation);
            preparationIds.add(0, preparation.id()); // last modified preparation is first
            Thread.sleep(100); // to make sure the last modification date is older
        }

        // when
        final InputStream responseContent = when().get("/preparations/details").asInputStream();
        final List<Preparation> actual = mapper.readValue(responseContent, new TypeReference<List<Preparation>>() {
        });

        // then
        assertEquals(preparationIds.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(preparationIds.get(i), actual.get(i).getId());
        }
    }

    @Test
    public void list() throws Exception {
        // given
        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[]"));
        final Preparation preparation1 = new Preparation("#18875", "1234", Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation1.setCreationDate(0);
        repository.add(preparation1);

        when().get("/preparations").then().statusCode(HttpStatus.OK.value()).body(sameJSONAs("[\"#18875\"]"));
        final Preparation preparation2 = new Preparation("#483275", "5678", Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation2.setCreationDate(0);
        repository.add(preparation2);

        // when
        final List<String> list = when().get("/preparations").jsonPath().getList("");

        // then
        assertThat(list, hasItems("#18875", "#483275"));
    }

    @Test
    public void getDetails() throws Exception {
        // given
        final Preparation preparation = new Preparation("8b6281c5e99c41313a83777c3ab43b06adda9e5c", "1234", Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation.setCreationDate(0);
        preparation.setLastModificationDate(12345);
        repository.add(preparation);

        // when
        final String preparationDetails = when().get("/preparations/{id}/details", preparation.id()).asString();

        // then
        final InputStream expected = PreparationControllerTest.class.getResourceAsStream("preparation_1234.json");
        assertThat(preparationDetails, sameJSONAsFile(expected));
    }

    @Test
    public void getDetailsWithSpecificStepId() throws Exception {

        // given
        final String preparationContent = IOUtils.toString(this.getClass().getResourceAsStream("base_preparation.json"), "UTF-8");
        final String preparationId = clientTest.createPreparationWithAPI(preparationContent);

        final String step = IOUtils.toString(this.getClass().getResourceAsStream("actions/append_copy_firstname.json"), "UTF-8");
        clientTest.addStep(preparationId, step);
        final String wantedStepId = clientTest.addStep(preparationId, step);
        clientTest.addStep(preparationId, step);

        // when
        final PreparationMessage details = clientTest.getDetails(preparationId, wantedStepId);

        // then
        assertNotNull(details);
        assertEquals(wantedStepId, details.getHeadId());
        assertEquals(3, details.getSteps().size());
    }

    @Test
    public void getDetailsWithSpecificStepIdShouldReturn404WhenStepIdNotFound() throws Exception {

        // given
        final String preparationContent = IOUtils.toString(this.getClass().getResourceAsStream("base_preparation.json"), "UTF-8");
        final String preparationId = clientTest.createPreparationWithAPI(preparationContent);

        try {
            // when
            clientTest.getDetails(preparationId, "toutouyoutou");
            fail("well done, you managed to get the details out of an unknown step !");
        } catch (MockTDPException e) {
            // then
            assertEquals(404, e.getStatusCode());
        }

    }

    @Test
    public void cannotCopyUnknownPreparation() throws Exception {

        // given
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
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");

        final String originalId = clientTest.createPreparation(createTestPreparation("test_name", "1234"), fromFolder.getId())
                .getId();
        // Change the author, to make it different from system user.
        repository.get(originalId, Preparation.class).setAuthor("tagada");

        // when
        final Response response = given() //
                .queryParam("name", "the new preparation") //
                .queryParam("destination", toFolder.getId()) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations/{id}/copy", originalId);
        final String copyId = response.asString();

        // then
        assertThat(response.getStatusCode(), is(200));
        assertThatPreparationIsFirstInsideFolder(copyId, toFolder.getId());
        assertEquals("the new preparation", repository.get(copyId, Preparation.class).getName());

        // Assert that the author is the system user, and not the original author of the prep:
        assertEquals(System.getProperty("user.name"), repository.get(copyId, Preparation.class).getAuthor());
        // row metadata its nullity after copy caused https://jira.talendforge.org/browse/TDP-3379
        assertNotNull(repository.get(copyId, Preparation.class).getRowMetadata());
    }

    private FolderEntry assertThatPreparationIsFirstInsideFolder(String expectedPreparationId, String folderId) {
        try (Stream<FolderEntry> folderEntriesStream = folderRepository.entries(folderId, PREPARATION)) {
            final Iterator<FolderEntry> iterator = folderEntriesStream.iterator();
            assertTrue(iterator.hasNext());
            final FolderEntry copy = iterator.next();
            assertEquals(copy.getContentId(), expectedPreparationId);
            return copy;
        }
    }

    @Test
    public void cannotCopyIfAnExistingPreparationAlreadyExists() throws Exception {
        // given
        final String name = "my preparation";
        final Folder folder = folderRepository.addFolder(home.getId(), "great_folder");

        final String originalId = clientTest.createPreparation(createTestPreparation(name, "1234"), folder.getId()).getId();

        // when
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("destination", folder.getId()) //
                .when() //
                .expect().statusCode(409).log().ifError() //
                .post("/preparations/{id}/copy", originalId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void shouldCopyWithDefaultParameters() throws Exception {
        // given
        final Folder folder = folderRepository.addFolder(home.getId(), "yet_another_folder");
        final String originalId = clientTest.createPreparation(createTestPreparation("prep_1", "1234"), folder.getId()).getId();
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");

        // when
        final Response response = given() //
                .queryParam("destination", toFolder.getId()) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations/{id}/copy", originalId);
        final String copyId = response.asString();

        // then
        assertThat(response.getStatusCode(), is(200));
        final Iterator<FolderEntry> iterator;
        try (Stream<FolderEntry> folderEntriesStream = folderRepository.entries(toFolder.getId(), PREPARATION)) {
            iterator = folderEntriesStream.iterator();

            boolean found = false;
            while (iterator.hasNext()) {
                final FolderEntry entry = iterator.next();
                if (entry.getContentId().equals(copyId)) {
                    found = true;
                    assertEquals("prep_1 copy", repository.get(entry.getContentId(), Preparation.class).getName());
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void shouldMove() throws Exception {
        // given
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");

        final String originalId = clientTest.createPreparation(createTestPreparation("test_move", "7535"), fromFolder.getId())
                .getId();

        // when
        final Response response = given() //
                .queryParam("folder", fromFolder.getId()) //
                .queryParam("destination", toFolder.getId()) //
                .queryParam("newName", "moved preparation") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/move", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
        assertThatPreparationIsFirstInsideFolder(originalId, toFolder.getId());
        assertEquals("moved preparation", repository.get(originalId, Preparation.class).getName());
    }

    @Test
    public void cannotMovePreparationIfTargetPathAndNameExists() throws Exception {
        // given
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");

        final String name = "super preparation";
        final String preparationId = clientTest
                .createPreparation(createTestPreparation("another preparation", "7535"), fromFolder.getId()).getId();
        clientTest.createPreparation(createTestPreparation(name, "7384"), toFolder.getId());

        // when
        final Response response = given() //
                .queryParam("folder", fromFolder.getId()) //
                .queryParam("destination", toFolder.getId()) //
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
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");
        final String originalId = clientTest.createPreparation(createTestPreparation("yap", "7535"), fromFolder.getId()).getId();

        // when
        final Response response = given() //
                .queryParam("folder", fromFolder.getId()) //
                .queryParam("destination", toFolder.getId()) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/move", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
        assertThatPreparationIsFirstInsideFolder(originalId, toFolder.getId());
        assertEquals("yap", repository.get(originalId, Preparation.class).getName());
    }

    @Test
    public void cannotMovePreparationThatDoesntExist() throws Exception {
        // given
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");

        // when
        final Response response = given() //
                .queryParam("newName", "moved preparation") //
                .queryParam("folder", fromFolder.getId()) //
                .queryParam("destination", toFolder.getId()) //
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
        final Folder bar = folderRepository.addFolder(home.getId(), "/foo/bar");
        final String barEntry = clientTest.createPreparation(createTestPreparation("youpi", "4824"), bar.getId()).getId();

        // when
        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/preparations/{id}/folder", barEntry);

        // then
        assertThat(response.getStatusCode(), is(200));
        try (InputStream responseAsInputStream = response.asInputStream()) {
            final Folder actual = mapper.readValue(responseAsInputStream, Folder.class);
            assertEquals(bar.getId(), actual.getId());
        }
    }

    @Test
    public void locateUnknownPreparationShouldSend404() throws Exception {
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
        applyTransformation(referenceId, "actions/append_upper_case.json");
        applyTransformation(referenceId, "actions/append_lower_case.json");
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
        assertNotEquals(preparation.getHeadId(), reference.getHeadId());
        assertEquals(preparation.getSteps().size(), reference.getSteps().size());
        assertThat(preparation.getSteps().get(0).getId(), is(Step.ROOT_STEP.id()));
        assertEquals(preparation.getSteps().get(1).getContent(), reference.getSteps().get(1).getContent());
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
        applyTransformation(referenceId, "actions/append_upper_case.json");
        applyTransformation(referenceId, "actions/append_lower_case.json");

        final String preparationId = createPreparation("42321", "preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");

        // when
        final Response response = given() //
                .param("from", referenceId) //
                .when() //
                // .expect().statusCode(409).log().ifError() //
                .put("/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(409));
    }

    @Test
    public void shouldNotCopyStepsBecauseReferencePreparationIsNotFound() throws Exception {
        // given
        final String preparationId = createPreparation("154753", "preparation");
        final Preparation expected = repository.get(preparationId, Preparation.class);

        // when
        final Response response = given() //
                .param("from", "not to be found") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/steps/copy", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
        final Preparation actual = repository.get(preparationId, Preparation.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldLockAFreshlyLockedPreparation() throws Exception {

        // given
        final String preparationId = createPreparation("154753", "preparation");

        // when
        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/lock", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void shouldLockAlreadyLockedPreparation() throws Exception {

        // given
        final String preparationId = createPreparation("154753", "preparation");

        // when
        given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/lock", preparationId);

        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/lock", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void shouldUnlockAPreviouslyLockedPreparation() throws Exception {

        // given
        final String preparationId = createPreparation("154753", "preparation");

        // when
        given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/lock", preparationId);

        final Response response = given() //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/unlock", preparationId);

        // then
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void shouldReleaseALockAfterAMove() throws Exception {

        // given
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final Folder toFolder = folderRepository.addFolder(home.getId(), "to");
        final String originalId = clientTest.createPreparation(createTestPreparation("yap", "7535"), fromFolder.getId()).getId();
        Preparation expected = repository.get(originalId, Preparation.class);

        // when
        final Response response = given() //
                .queryParam("folder", fromFolder.getId()) //
                .queryParam("destination", toFolder.getId()) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .put("/preparations/{id}/move", originalId);

        // then
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void shouldGetPreparation() throws Exception {
        // given
        final Folder fromFolder = folderRepository.addFolder(home.getId(), "from");
        final String preparationId = clientTest.createPreparation(createTestPreparation("yap", "7535"), fromFolder.getId())
                .getId();
        final Preparation preparation = repository.get(preparationId, Preparation.class);

        final String expected = "{" + "\"id\":\"" + preparation.getId() + "\"," + "\"app-version\":\""
                + preparation.getAppVersion() + "\"," + "\"dataSetId\":\"7535\","
                + "\"rowMetadata\":{\"nextId\":0,\"columns\":[]}," + "\"author\":\"" + preparation.getAuthor() + "\","
                + "\"name\":\"yap\"," + "\"creationDate\":" + preparation.getCreationDate() + "," + "\"lastModificationDate\":"
                + preparation.getCreationDate() + "," + "\"headId\":\"f6e172c33bdacbc69bca9d32b2bd78174712a171\"" + "}";

        // when
        final Response response = given() //
                .queryParam("id", preparationId) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/preparations/{id}", preparationId);

        // then
        assertThat(response.asString(), sameJSONAs(expected).allowingExtraUnexpectedFields());
    }

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------LIFECYCLE----------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------
    @Test
    public void create() throws Exception {
        // given
        assertThat(repository.list(Preparation.class).count(), is(0L));
        final String preparationId = createPreparationWithAPI(
                "{\"name\": \"test_name\", \"dataSetId\": \"1234\", \"rowMetadata\":{\"columns\":[]}}}}");
        assertThat(repository.list(Preparation.class).count(), is(1L));

        // when
        final Optional<Preparation> first = repository.list(Preparation.class).findFirst();
        assertThat(first.isPresent(), is(true));
        final Preparation preparation = first.get();

        // then
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("test_name"));
        assertThat(preparation.getAuthor(), is(System.getProperty("user.name")));
        assertThat(preparation.getAppVersion(), is(versionService.version().getVersionId()));
    }

    @Test
    public void createInFolder() throws Exception {
        // given
        final String path = "/test/create/preparation";
        final Folder folder = folderRepository.addFolder(home.getId(), path);
        final Iterator<FolderEntry> iterator;
        try (Stream<FolderEntry> folderEntriesStream = folderRepository.entries(folder.getId(), PREPARATION)) {
            iterator = folderEntriesStream.iterator();
            assertThat(iterator.hasNext(), is(false));
        }

        // when
        final String preparationId = clientTest
                .createPreparation(createTestPreparation("another_preparation", "75368"), folder.getId()).id();

        // then
        final FolderEntry entry = assertThatPreparationIsFirstInsideFolder(preparationId, folder.getId());
        assertThat(entry.getContentType(), is(PREPARATION));
    }

    @Test
    public void createWithSpecialCharacters() throws Exception {
        // given
        final List<Preparation> list = repository.list(Preparation.class).collect(Collectors.toList());
        assertThat(list.size(), is(0));

        // when
        final String preparationId = clientTest.createPreparation(createTestPreparation("éàçè", "1234")).id();

        // then
        final Collection<Preparation> preparations = repository.list(Preparation.class).collect(Collectors.toList());
        assertThat(preparationId, is(preparationId));
        assertThat(preparations.size(), is(1));

        final Preparation preparation = preparations.iterator().next();
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void delete() throws Exception {
        // given
        assertThat(repository.list(Preparation.class).count(), is(0L));

        final String preparationId = createPreparationWithAPI(
                "{\"name\": \"test_name\", \"dataSetId\": \"1234\", \"rowMetadata\":{\"columns\":[]}}}}");
        final Collection<Preparation> preparations = repository.list(Preparation.class).collect(Collectors.toList());
        assertThat(repository.list(Preparation.class).count(), is(1L));

        final Preparation preparation = preparations.iterator().next();
        assertThat(preparation.id(), is(preparationId));
        assertThat(preparation.getName(), is("test_name"));
        assertThat(doesPreparationExists(preparationId), is(true));

        // when
        clientTest.deletePreparation(preparationId);

        // then
        assertThat(repository.list(Preparation.class).count(), is(0L));
        assertThat(doesPreparationExists(preparationId), is(false));
    }

    private boolean doesPreparationExists(String preparationId) {
        try (Stream<FolderEntry> folderEntriesStream = folderRepository.findFolderEntries(preparationId, PREPARATION)) {
            return folderEntriesStream.iterator().hasNext();
        }
    }

    @Test
    public void testDeleteCleanUp() throws Exception {
        // given
        assertThat(repository.list(Preparation.class).count(), is(0L));
        final String preparationId = createPreparationWithAPI(
                "{\"name\": \"test_name\", \"dataSetId\": \"1234\", \"rowMetadata\":{\"columns\":[]}}}}");
        applyTransformation(preparationId, "actions/append_copy_lastname.json");

        assertThat(repository.list(Preparation.class).count(), is(1L));
        assertThat(repository.list(Step.class).count(), is(2L));
        assertThat(repository.list(PreparationActions.class).count(), is(2L));

        // when
        clientTest.deletePreparation(preparationId);

        // then
        assertThat(repository.list(Preparation.class).count(), is(0L));
        assertThat(repository.list(Step.class).count(), is(1L));
        assertThat(repository.list(PreparationActions.class).count(), is(1L));
    }

    @Test
    public void testDeleteCleanUpWithSharedSteps() throws Exception {
        // given
        assertThat(repository.list(Preparation.class).count(), is(0L));
        final String preparationId1 = createPreparationWithAPI(
                "{\"name\": \"test_name\", \"dataSetId\": \"1234\", \"rowMetadata\":{\"columns\":[]}}}}");
        applyTransformation(preparationId1, "actions/append_copy_lastname.json");
        final String preparationId2 = createPreparationWithAPI(
                "{\"name\": \"test_name\", \"dataSetId\": \"1234\", \"rowMetadata\":{\"columns\":[]}}}");
        applyTransformation(preparationId2, "actions/append_copy_lastname.json");

        assertThat(repository.list(Preparation.class).count(), is(2L));
        assertThat(repository.list(Step.class).count(), is(3L));
        assertThat(repository.list(PreparationActions.class).count(), is(2L));

        // when
        clientTest.deletePreparation(preparationId1);

        // then
        assertThat(repository.list(Preparation.class).count(), is(1L));
        assertThat(repository.list(Step.class).count(), is(2L));
        assertThat(repository.list(PreparationActions.class).count(), is(2L));
    }

    @Test
    public void update() throws Exception {
        assertThat(repository.list(Preparation.class).count(), is(0L));
        final String preparationId = clientTest.createPreparation(createTestPreparation("test_name", "1234")).getId();

        final Preparation createdPreparation = repository.list(Preparation.class).iterator().next();
        assertThat(createdPreparation.getId(), is(preparationId));
        final long oldModificationDate = createdPreparation.getLastModificationDate();

        // Test preparation details update
        final String updatedId = given().contentType(ContentType.JSON) //
                .body(createTestPreparation("test_name_updated", "1234")) //
                .when() //
                .put("/preparations/{id}", preparationId) //
                .asString();

        // Preparation id should not change (name is not part of preparation id).
        assertThat(updatedId, is(preparationId));
        final Collection<Preparation> preparations = repository.list(Preparation.class).collect(Collectors.toList());
        assertThat(preparations.size(), is(1));
        final Preparation preparation = preparations.iterator().next();
        assertThat(preparation.getName(), is("test_name_updated"));
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));
        assertThat(preparation.getAppVersion(), is(versionService.version().getVersionId()));
    }

    @Test
    public void updateWithSpecialArguments() throws Exception {
        // given
        final Preparation createdPreparation = new Preparation("#123", versionService.version().getVersionId());
        createdPreparation.setDataSetId("1234");
        createdPreparation.setName("test_name");
        createdPreparation.setRowMetadata(new RowMetadata());

        final String preparationId = clientTest.createPreparation(createdPreparation).getId();

        // when
        final String updatedId = given().contentType(ContentType.JSON.withCharset(UTF_8))
                .body(createTestPreparation("éàçè", "1234")).when().put("/preparations/{id}", preparationId).asString();

        // then
        // Preparation id should not change (new name)
        assertThat(updatedId, is(preparationId));
        final Collection<Preparation> preparations = repository.list(Preparation.class).collect(Collectors.toList());
        assertThat(preparations.size(), is(1));
        final Preparation preparation = preparations.iterator().next();
        assertThat(preparation.id(), is(updatedId));
        assertThat(preparation.getName(), is("éàçè"));
    }

    @Test
    public void shouldChangePreparationHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

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
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");

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

        assertThat(preparation.getHeadId(), is(Step.ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "actions/append_copy_lastname.json");

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(preparation.getHeadId(), Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headAction.getActions(), hasSize(1));
        assertThat(headAction.getActions().get(0).getName(), is("copy"));
    }

    @Test
    public void shouldAddMultipleActionStepAfterHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getHeadId(), is(Step.ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "actions/append_multi_upper_case.json");

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(preparation.getHeadId(), Step.class);
        final Step lastBeforeHead = repository.get(head.getParent(), Step.class);
        final PreparationActions headAction = repository.get(head.getContent(), PreparationActions.class);
        final PreparationActions lastBeforeHeadAction = repository.get(lastBeforeHead.getContent(), PreparationActions.class);

        // first step : contains only uppercase on lastname
        assertThat(lastBeforeHeadAction.getActions(), hasSize(1));
        assertThat(lastBeforeHeadAction.getActions().get(0).getName(), is("uppercase"));
        assertThat(lastBeforeHeadAction.getActions().get(0).getParameters().get("column_name"), is("lastname"));

        // second step : contains first step actions + uppercase on firstname
        assertThat(headAction.getActions(), hasSize(2));
        assertThat(headAction.getActions().get(0).getName(), is("uppercase"));
        assertThat(headAction.getActions().get(0).getParameters().get("column_name"), is("lastname"));
        assertThat(headAction.getActions().get(1).getName(), is("uppercase"));
        assertThat(headAction.getActions().get(1).getParameters().get("column_name"), is("firstname"));
    }

    @Test
    public void shouldAddActionWithFilterStepAfterHead() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        assertThat(preparation.getHeadId(), is(Step.ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "actions/append_copy_lastname_filter.json");

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(preparation.getHeadId(), Step.class);
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

        assertThat(preparation.getHeadId(), is(Step.ROOT_STEP.getId()));

        // when
        applyTransformation(preparationId, "actions/append_copy_lastname.json");

        // then
        Optional<Step> first = repository.list(Step.class) //
                .filter(s -> s.getParent() != null && Objects.equals(s.getParent(), Step.ROOT_STEP.getId())) //
                .findFirst();

        assertTrue(first.isPresent());

        final Step head = first.get();
        assertThat(head.getParent(), is(Step.ROOT_STEP.getId()));
        assertThat(head.getDiff().getCreatedColumns(), hasSize(1));
        assertThat(head.getDiff().getCreatedColumns(), hasItem("0004"));
    }

    @Test
    public void shouldReturnErrorWhenScopeIsNotConsistentOnAppendTransformation() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");

        // when
        final Response request = given()
                .body(IOUtils.toString(
                        PreparationControllerTest.class.getResourceAsStream("error/incomplete_transformation_list_params.json"),
                        UTF_8))//
                .contentType(ContentType.JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId);

        // then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------UPDATE STEP----------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldModifySingleAction() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        try (InputStream inputStream = PreparationControllerTest.class
                .getResourceAsStream("actions/append_update_upper_case.json")) {
            given().body(IOUtils.toString(inputStream, UTF_8))//
                    .contentType(ContentType.JSON)//
                    .when()//
                    .put("/preparations/{id}/actions/{action}", preparationId, firstStepId);

            // then
            preparation = repository.get(preparationId, Preparation.class);
            assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

            final Step head = repository.get(preparation.getHeadId(), Step.class);
            assertThat(head.getParent(), is(Step.ROOT_STEP.getId()));
        }
    }

    @Test
    public void shouldModifyLastAction() throws Exception {
        // Initial preparation
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when : update second (last) step
        given().body(IOUtils
                .toString(PreparationControllerTest.class.getResourceAsStream("actions/append_update_upper_case.json"), UTF_8))
                .contentType(ContentType.JSON).when().put("/preparations/{id}/actions/{action}", preparationId, secondStepId);

        // then
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getLastModificationDate(), is(greaterThan(oldModificationDate)));

        final Step head = repository.get(preparation.getHeadId(), Step.class);
        assertThat(head.getParent(), is(firstStepId));
    }

    @Test
    public void shouldModifyFirstAction() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when
        given().body(IOUtils
                .toString(PreparationControllerTest.class.getResourceAsStream("actions/append_update_upper_case.json"), UTF_8))
                .contentType(ContentType.JSON).when()
                .put("/preparations/{id}/actions/{action}", preparation.id(), "a41184275b046d86c8d98d413ed019bc0a7f3c49");

        // then
        preparation = repository.get(preparationId, Preparation.class);
        assertThat(preparation.getHeadId(), is(secondStepId));
        assertThat(preparation.getLastModificationDate(), is(greaterThanOrEqualTo(oldModificationDate)));

        final Step head = repository.get(secondStepId, Step.class);
        assertThat(head.getParent(), is(firstStepId));

        final Step first = repository.get(firstStepId, Step.class);
        assertThat(first.getParent(), is(Step.ROOT_STEP.getId()));
    }

    @Test
    public void updateAction_shouldSaveUpdatedStepDiffAndShiftColumnsIds() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");

        AppendStep firstStep = step(null, action("copy", paramsColAction("0003", "birth")));
        clientTest.addStep(preparationId, firstStep);
        AppendStep secondStep = step(null,
                action("split", params("column_id", "0001", "column_name", "firstname", "scope", "column", "limit", "2")));
        clientTest.addStep(preparationId, secondStep);

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(3, stepIds.size());
        assertEquals(singletonList("0004"), repository.get(stepIds.get(1), Step.class).getDiff().getCreatedColumns());
        assertEquals(asList("0005", "0006"), repository.get(stepIds.get(2), Step.class).getDiff().getCreatedColumns());

        // when : +1 column
        given().body(step(diff("0004", "0005", "0006"), action("split", paramsColAction("0001", "firstname")))) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/preparations/{id}/actions/{action}", preparationId, stepIds.get(1));

        // then
        final String newHeadId = repository.get(preparationId, Preparation.class).getHeadId();
        final List<String> stepIdsAfter = preparationUtils.listStepsIds(newHeadId, repository);
        assertThatStepHasCreatedColumns(stepIdsAfter.get(1), "0004", "0005", "0006");
        assertThatStepHasCreatedColumns(stepIdsAfter.get(2), "0007", "0008");
    }

    @Test
    public void shouldReturnErrorWhenScopeIsNotConsistentOnTransformationUpdate() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        final String stepId = applyTransformation(preparationId, "actions/append_upper_case.json");

        // when
        final Response request = given()
                .body(IOUtils.toString(
                        PreparationControllerTest.class.getResourceAsStream("error/incomplete_transformation_params.json"),
                        UTF_8))//
                .contentType(ContentType.JSON)//
                .when()//
                .put("/preparations/{id}/actions/{action}", preparationId, stepId);

        // then
        request.then()//
                .statusCode(400)//
                .assertThat()//
                .body("code", is("TDP_BASE_MISSING_ACTION_SCOPE"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------DELETE STEP----------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldDeleteSingleStep() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        final String firstStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

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
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0004", "lastname_copy"))));

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(3, stepIds.size());

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, stepIds.get(1))//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparationAfter = repository.get(preparationId, Preparation.class);
        final List<String> stepIdsAfter = preparationUtils.listStepsIds(preparationAfter.getHeadId(), repository);
        assertEquals(1, stepIdsAfter.size());
    }

    @Test
    public void shouldShiftColumnCreatedAfterStepWithAllActionsParametersOnThoseSteps() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0005", "lastname_copy"))));

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(4, stepIds.size());

        // when : delete second step in single mode
        when().delete("/preparations/{id}/actions/{action}", preparationId, stepIds.get(1))//
                .then()//
                .statusCode(200);

        // then
        final Preparation preparationAfter = repository.get(preparationId, Preparation.class);
        final String headId = preparationAfter.getHeadId();
        Step head = repository.get(headId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));
        Action secondCopyAction = headActions.getActions().get(0);
        assertThat(secondCopyAction.getName(), is("copy"));
        assertThat(secondCopyAction.getParameters().get("column_id"), is("0001"));
        Action upperCaseAction = headActions.getActions().get(1);
        assertThat(upperCaseAction.getName(), is("uppercase"));
        assertThat(upperCaseAction.getParameters().get("column_id"), is("0004"));
    }

    @Test
    public void shouldThrowExceptionWhenStepToDeleteIsRoot() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");
        applyTransformation(preparationId, "actions/append_lower_case.json");

        // when: delete ROOT
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, Step.ROOT_STEP.getId());

        // then
        response.then().statusCode(403).assertThat().body("code", is("TDP_PS_PREPARATION_ROOT_STEP_CANNOT_BE_DELETED"));
    }

    @Test
    public void shouldThrowExceptionWhenStepDoesntExist() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");
        applyTransformation(preparationId, "actions/append_lower_case.json");

        // when : delete unknown step
        final Response response = when().delete("/preparations/{id}/actions/{action}", preparationId, "azerty");

        // then
        response.then().statusCode(404).assertThat().body("code", is("TDP_PS_PREPARATION_STEP_DOES_NOT_EXIST"));
    }

    @Test
    public void shouldUpdateModificationDateOnDeleteStep() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");
        final String secondStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Preparation preparation = repository.get(preparationId, Preparation.class);
        final long oldModificationDate = preparation.getLastModificationDate();

        // when: delete LOWERCASE
        when().delete("/preparations/{id}/actions/{action}", preparation.id(), secondStepId);

        // then
        preparation = repository.get(preparation.id(), Preparation.class);
        assertThat(oldModificationDate, lessThan(preparation.getLastModificationDate()));
    }

    @Test
    public void shouldTurnInconsistentPreparationToConsistentState() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0005", "lastname_copy"))));
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0005", "lastname_copy"))));

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(4, stepIds.size());
        final Step step4 = repository.get(stepIds.get(3), Step.class);
        final Step step3 = repository.get(stepIds.get(2), Step.class);
        final PreparationActions actionStep4 = repository.get(step4.getContent(), PreparationActions.class);
        final PreparationActions actionStep3 = repository.get(step3.getContent(), PreparationActions.class);
        repository.remove(actionStep4);
        repository.remove(actionStep3);

        // when
        Response response = when().get("/preparations/{id}/details", preparationId);

        // then
        assertNotNull(response.path("metadata"));
        response.then().assertThat().body("metadata[0].name", is("copy"));
        Preparation newPreparation = repository.get(preparationId, Preparation.class);
        String newHead = stepIds.get(1);
        assertEquals(newHead, newPreparation.getHeadId());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------STEPS REORDERING-------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void shouldReturnANotFoundForNonExistingPreparation() {
        // @formatter:off
        given()
            .queryParam("parentStepId", Step.ROOT_STEP.getId())
        .when()
            .post("/preparations/{id}/steps/{stepId}/order", "DoesNotExist", Step.ROOT_STEP.id())//
        .then()//
            .statusCode(404);
        // @formatter:on
    }

    @Test
    public void shouldReturnANotFoundForNonExistingStep() {
        final String preparationId = createPreparation("1234", "My preparation");
        // @formatter:off
        given()
            .queryParam("parentStepId", Step.ROOT_STEP.getId())
        .when()
            .post("/preparations/{id}/steps/{stepId}/order", preparationId, "DoesNotExist")//
        .then()//
            .statusCode(404);
        // @formatter:on
    }

    @Test
    public void shouldMoveAStepToFirstPositionIfLegal() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "actions/append_copy_lastname.json");
        applyTransformation(preparationId, "actions/append_rename_copy_lastname.json");
        final String headStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));

        // @formatter:off
        given()
            .queryParam("parentStepId", Step.ROOT_STEP.getId())
        .when().post("/preparations/{id}/steps/{stepId}/order", preparationId, copyStepId)//
            .then()//
            .statusCode(200);
        // @formatter:on
        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getHeadId();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("copy"));
        assertThat(headActions.getActions().get(1).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));
    }

    @Test
    public void shouldMoveAStepToLastPositionIfLegal() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        final String upperCaseStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        applyTransformation(preparationId, "actions/append_copy_lastname.json");
        applyTransformation(preparationId, "actions/append_rename_copy_lastname.json");
        final String headStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));

        // when : delete second step in single mode
        // @formatter:off
        given()
            .queryParam("parentStepId", headStepId)
        .when()
            .post("/preparations/{id}/steps/{stepId}/order", preparationId, upperCaseStepId)//
        .then()//
            .statusCode(200);
        // @formatter:on
        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getHeadId();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));

        assertThat(headActions.getActions().get(0).getName(), is("copy"));
        assertThat(headActions.getActions().get(1).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(2).getName(), is("lowercase"));
        assertThat(headActions.getActions().get(3).getName(), is("uppercase"));
    }

    @Test
    public void shouldMoveAStepToNonBoundaryPositionIfLegal() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        final String upperCaseStepId = applyTransformation(preparationId, "actions/append_upper_case.json");
        final String copyStepId = applyTransformation(preparationId, "actions/append_copy_lastname.json");
        applyTransformation(preparationId, "actions/append_rename_copy_lastname.json");
        final String headStepId = applyTransformation(preparationId, "actions/append_lower_case.json");

        Step head = repository.get(headStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));
        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));

        // when : delete second step in single mode
        // @formatter:off
        given()
            .queryParam("parentStepId", copyStepId)
        .when()
            .post("/preparations/{id}/steps/{stepId}/order", preparationId, upperCaseStepId)//
        .then()//
            .statusCode(200);
        // @formatter:on
        // then
        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparation.getHeadId();

        head = repository.get(newHeadStepId, Step.class);
        headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(4));

        assertThat(headActions.getActions().get(0).getName(), is("copy"));
        assertThat(headActions.getActions().get(1).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(2).getName(), is("rename_column"));
        assertThat(headActions.getActions().get(3).getName(), is("lowercase"));
    }

    @Test
    public void shouldNotMoveAStepIfItUseANotYetCreatedColumn() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("copy", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0005", "lastname_copy"))));

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(4, stepIds.size());

        // when : delete second step in single mode
        given().queryParam("parentStepId", stepIds.get(1)).when()
                .post("/preparations/{id}/steps/{stepId}/order", preparationId, stepIds.get(3))//
                // TDP-3624
                .then().statusCode(409);

        final Preparation preparationAfter = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparationAfter.getHeadId();

        Step head = repository.get(newHeadStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(3));

        assertThat(headActions.getActions().get(0).getName(), is("copy"));
        assertThat(headActions.getActions().get(1).getName(), is("copy"));
        assertThat(headActions.getActions().get(2).getName(), is("uppercase"));
    }

    @Test
    public void shouldNotMoveAStepIfItUsesADeletedColumn() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "My preparation");
        clientTest.addStep(preparationId, step(null, action("uppercase", paramsColAction("0001", "lastname"))));
        clientTest.addStep(preparationId, step(null, action("delete_column", paramsColAction("0001", "lastname"))));

        final Preparation preparation = repository.get(preparationId, Preparation.class);
        final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);
        assertEquals(3, stepIds.size());

        // when : delete second step in single mode
        given().queryParam("parentStepId", stepIds.get(0)).when()
                .post("/preparations/{id}/steps/{stepId}/order", preparationId, stepIds.get(2))//
                // TDP-3624
                .then().statusCode(409);

        final Preparation preparationAfter = repository.get(preparationId, Preparation.class);
        final String newHeadStepId = preparationAfter.getHeadId();

        Step head = repository.get(newHeadStepId, Step.class);
        PreparationActions headActions = repository.get(head.getContent(), PreparationActions.class);
        assertThat(headActions.getActions(), hasSize(2));

        assertThat(headActions.getActions().get(0).getName(), is("uppercase"));
        assertThat(headActions.getActions().get(1).getName(), is("delete_column"));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------CHECKS-------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    public void shouldReturnHTTP404WhenNoPreparationUseDataset() throws Exception {
        // given
        final String datasetId = "3214a6748bc4f9674c85";
        final String preparationId = createPreparation("other_dataset", "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");

        repository.get(preparationId, Preparation.class);

        // when
        final Response response = when().head("/preparations/use/dataset/{id}", datasetId);

        // then
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldReturnHTTP203WhenPreparationIsOnDataset() throws Exception {
        // given
        final String datasetId = "3214a6748bc4f9674c85";
        final String preparationId = createPreparation(datasetId, "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");

        repository.get(preparationId, Preparation.class);

        // when
        final Response response = when().head("/preparations/use/dataset/{id}", datasetId);

        // then
        assertThat(response.getStatusCode(), is(204));
    }

    @Test
    @Ignore("This test is flawed, it may be corrected but this behavior is already tested through PreparationAPI.")
    // TODO: this test is not testing what it says it is testing:
    // The 204 comes from the use of the dataset as preparation base, not in lookup
    public void shouldReturnHTTP204WhenPreparationHasLookupOnDataset() throws Exception {
        // given
        final String datasetId = "3214a6748bc4f9674c85";
        final String preparationId = createPreparation(datasetId, "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");

        final String lookupDatasetId = "687468453436851";
        final Map<String, String> parametersOnDataset = new HashMap<>();
        parametersOnDataset.put("lookup_ds_id", lookupDatasetId);
        final Map<String, String> parametersWithoutDataset = new HashMap<>();
        parametersWithoutDataset.put("other", "other");

        final List<Action> action1 = singletonList(Action.Builder.builder().withParameters(parametersWithoutDataset).build());
        final List<Action> action2 = singletonList(Action.Builder.builder().withParameters(parametersOnDataset).build());

        final PreparationActions prepAction1 = new PreparationActions().append(action1);
        final PreparationActions prepAction2 = new PreparationActions().append(action2);

        repository.add(prepAction1);
        repository.add(prepAction2);

        // when
        final Response response = when().head("/preparations/use/dataset/{id}", lookupDatasetId);

        // then
        assertThat(response.getStatusCode(), is(204));
    }

    @Test
    public void preparationsThatUseDataset_shouldDeleteDatasetIfUsedInLookupNotUsed() throws Exception {
        // given
        final String datasetId = "3214a6748bc4f9674c85";
        final String preparationId = createPreparation(datasetId, "My preparation");
        applyTransformation(preparationId, "actions/append_upper_case.json");

        final String lookupDatasetId = "687468453436851";
        final Map<String, String> parametersOnDataset = new HashMap<>();
        parametersOnDataset.put("lookup_ds_id", lookupDatasetId);
        final Map<String, String> parametersWithoutDataset = new HashMap<>();
        parametersWithoutDataset.put("other", "other");

        final List<Action> action1 = singletonList(Action.Builder.builder().withParameters(parametersWithoutDataset).build());
        final List<Action> action2 = singletonList(Action.Builder.builder().withParameters(parametersOnDataset).build());

        final PreparationActions prepAction1 = new PreparationActions().append(action1);
        final PreparationActions prepAction2 = new PreparationActions().append(action2);

        repository.add(prepAction1);
        repository.add(prepAction2);

        // when
        final Response response = when().head("/preparations/use/dataset/{id}", lookupDatasetId);

        // then
        // Because the lookup action is not used in any preparation:
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldMakePreparationNotDistributed() throws Exception {
        // given
        final String preparationId = createPreparation("1234", "my preparation");
        applyTransformation(preparationId, "actions/append_make_line_header.json"); // Make line header can't be distributed
        applyTransformation(preparationId, "actions/append_upper_case.json");
        Preparation preparation = repository.get(preparationId, Preparation.class);

        // when
        final String preparationDetails = when().get("/preparations/{id}/details", preparation.id()).asString();

        // then
        assertThat(JsonPath.given(preparationDetails).get("allowDistributedRun"), is(false));
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
        final Preparation preparation = new Preparation(UUID.randomUUID().toString(), datasetId, Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation.setName(name);
        preparation.setCreationDate(0);
        RowMetadata rowMetadata = new RowMetadata();
        ColumnMetadata firstColumn = new ColumnMetadata();
        firstColumn.setId("0001");
        ColumnMetadata secondColumn = new ColumnMetadata();
        secondColumn.setId("0002");
        ColumnMetadata thirdColumn = new ColumnMetadata();
        thirdColumn.setId("0003");
        rowMetadata.setColumns(asList(firstColumn, secondColumn, thirdColumn));
        preparation.setRowMetadata(rowMetadata);
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
        try (InputStream inputStream = PreparationControllerTest.class.getResourceAsStream(transformationFilePath)) {
            given().body(IOUtils.toString(inputStream, UTF_8)) //
                    .contentType(ContentType.JSON) //
                    .when() //
                    .post("/preparations/{id}/actions", preparationId) //
                    .then() //
                    .statusCode(200);

            final Preparation preparation = repository.get(preparationId, Preparation.class);
            return preparation.getHeadId();
        }
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

    private static Preparation createTestPreparation(String name, String datasetId) {
        Preparation preparation = new Preparation();
        preparation.setName(name);
        preparation.setDataSetId(datasetId);
        preparation.setRowMetadata(new RowMetadata());
        return preparation;
    }

}
