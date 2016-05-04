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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.DATASET;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

/**
 * Unit tests for the folder API.
 */
public class FolderAPITest extends ApiServiceTestBase {

    @Before
    public void cleanupFolder() throws Exception {
        folderRepository.clear();
    }

    @Test
    public void should_create_folder_in_root() throws IOException {
        //given
        final List<Folder> originalFolders = getFolderContent("/");
        assertThat(originalFolders, hasSize(0));

        //when
        createFolder("beer");

        //then
        final List<Folder> folders = getFolderContent("/");
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("beer"));
        assertThat(folders.get(0).getPath(), is("beer"));
    }

    @Test
    public void should_create_nested_folder() throws IOException {
        //given
        createFolder("beer");
        final List<Folder> originalFolders = getFolderContent("/beer");
        assertThat(originalFolders, hasSize(0));

        //when
        createFolder("/beer/alcohol");

        //then
        final List<Folder> folders = getFolderContent("/beer");
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("alcohol"));
        assertThat(folders.get(0).getPath(), is("beer/alcohol"));
    }

    @Test
    public void should_fetch_all_folders() throws IOException {
        //given
        List<String> folders = Arrays.asList("drink", "/drink/beer", "/drink/wine", "/drink/wine/bordeaux");
        folders.forEach(f -> createFolder(f));

        //when
        final List<Folder> actual = getAllFolders();

        //then
        assertThat(folders, hasSize(4));
        containsInAnyOrder(actual, folders);
    }

    @Test
    public void should_add_entry_in_folder() throws IOException {
        //given
        createFolder("beer");
        final FolderEntry folderEntry = new FolderEntry(DATASET, "6f8a54051bc454");

        //when
        final FolderEntry createdEntry = createFolderEntry(folderEntry, "/beer");

        //then
        final List<FolderEntry> entries = getFolderEntries("/beer");
        assertThat(entries, hasSize(1));
        assertThat(entries, contains(createdEntry));
    }

    @Test
    public void should_remove_entry_from_folder() throws IOException {
        //given
        createFolder("beer");

        final FolderEntry firstFolderEntry = new FolderEntry(DATASET, "6f8a54051bc454");
        final FolderEntry firstCreatedEntry = createFolderEntry(firstFolderEntry, "/beer");

        final FolderEntry secondFolderEntry = new FolderEntry(DATASET, "32ac4646aa98b51");
        final FolderEntry secondCreatedEntry = createFolderEntry(secondFolderEntry, "/beer");

        final List<FolderEntry> entries = getFolderEntries("/beer");
        assertThat(entries, hasSize(2));
        assertThat(entries, containsInAnyOrder(firstCreatedEntry, secondCreatedEntry));

        //when
        removeFolderEntry(firstCreatedEntry.getContentId());

        //then
        final List<FolderEntry> updatedEntries = getFolderEntries("/beer");
        assertThat(updatedEntries, hasSize(1));
        assertThat(updatedEntries, contains(secondCreatedEntry));
    }

    @Test
    public void should_remove_empty_folder() throws IOException {
        //given
        createFolder("beer");

        //when
        final Response response = removeFolder("/beer");

        //then
        assertThat(response.getStatusCode(), is(200));
        final List<Folder> folders = getFolderContent("/");
        assertThat(folders, is(empty()));
    }

    @Test
    public void should_return_conflict_on_non_empty_folder_remove() throws IOException {
        //given
        createFolder("beer");
        final FolderEntry folderEntry = new FolderEntry(DATASET, "6f8a54051bc454");
        createFolderEntry(folderEntry, "/beer");

        //when
        final Response response = removeFolder("/beer");

        //then
        assertThat(response.getStatusCode(), is(409));
        final List<Folder> folders = getFolderContent("/");
        assertThat(folders, hasSize(1));
    }

    /**
     * This test does not check the whole content (this is already done by unit tests in lower services) but make sure
     * the plumbing is ok.
     */
    @Test
    public void shouldListPreparationsInFolder() throws Exception {
        // given
        folderRepository.addFolder("/one");
        folderRepository.addFolder("/two");
        createPreparationFromFile("dataset/dataset.csv", "yet another preparation", "text/csv", "/");
        createPreparationFromFile("dataset/dataset.csv", "prep 2", "text/csv", "/");
        createPreparationFromFile("dataset/dataset.csv", "preparation 3 !", "text/csv", "/");

        // when
        final Response response = given() //
                .queryParam("folder", "/") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/api/folders/preparations");

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.readTree(response.asInputStream());

        final JsonNode folders = rootNode.get("folders");
        assertNotNull(folders);
        assertEquals(2, folders.size());

        final JsonNode preparations = rootNode.get("preparations");
        assertNotNull(preparations);
        assertEquals(3, preparations.size());
        for (JsonNode preparation : preparations) {
            final JsonNode dataset = preparation.get("dataset");
            assertNotNull(dataset);
            assertTrue(dataset.has("dataSetId"));
            assertTrue(dataset.has("dataSetName"));
            assertTrue(dataset.has("dataSetNbRow"));
        }
    }

    @Test
    public void search_folders() throws Exception {
        createFolder("foo");
        createFolder("bar");
        createFolder("foo/beer");
        createFolder("foo/wine");
        createFolder("foo/wine/toto");
        createFolder("foo/wine/titi");
        createFolder("foo/wine/thetiti");
        createFolder("foo/wine/yupTITI");
        createFolder("foo/wine/yeahTITI");
        createFolder("foo/wine/Goodwine");
        createFolder("foo/wine/verygoodWInE");

        assertOnSearch("foo", 1);
        assertOnSearch("wine", 3);
        assertOnSearch("tIti", 4);
        assertOnSearch("GoOd", 2);
    }

    private void assertOnSearch(String searchQuery, int expectedSize) throws Exception {
        //when
        final Response response = RestAssured.given() //
                .queryParam("pathName", searchQuery).when() //
                .get("/api/folders/search");
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        //then
        Assertions.assertThat(folders).hasSize(expectedSize);
    }

    private void createFolder(final String path) {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }

    private Response removeFolder(final String path) {
        return RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .delete("/api/folders");
    }

    private List<Folder> getAllFolders() throws IOException {
        final Response response = RestAssured.given() //
                .when() //
                .get("/api/folders/all");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    private List<Folder> getFolderContent(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    private FolderEntry createFolderEntry(final FolderEntry folderEntry, String path) throws JsonProcessingException {
        return folderRepository.addFolderEntry(folderEntry, path);
    }

    private void removeFolderEntry(final String contentId) {
        final Response response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .pathParam("contentType", DATASET) //
                .pathParam("id", contentId) //
                .delete("/api/folders/entries/{contentType}/{id}");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }

    private List<FolderEntry> getFolderEntries(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .queryParam("contentType", DATASET) //
                .get("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return mapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });
    }

}
