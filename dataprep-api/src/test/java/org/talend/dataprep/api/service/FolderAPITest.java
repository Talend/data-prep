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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.folder.FolderEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.talend.dataprep.api.preparation.Preparation;

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
        createFolder("drink");
        createFolder("/drink/beer");
        createFolder("/drink/wine");
        createFolder("/drink/wine/bordeaux");

        //when
        final List<Folder> folders = getAllFolders();

        //then
        assertThat(folders, hasSize(4));
        assertThat(folders.get(0).getPath(), is("drink"));
        assertThat(folders.get(1).getPath(), is("drink/wine/bordeaux"));
        assertThat(folders.get(2).getPath(), is("drink/wine"));
        assertThat(folders.get(3).getPath(), is("drink/beer"));
    }

    @Test
    public void should_add_entry_in_folder() throws IOException {
        //given
        createFolder("beer");
        final FolderEntry folderEntry = new FolderEntry(FolderEntry.ContentType.DATASET, "6f8a54051bc454");

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

        final FolderEntry firstFolderEntry = new FolderEntry(FolderEntry.ContentType.DATASET, "6f8a54051bc454");
        final FolderEntry firstCreatedEntry = createFolderEntry(firstFolderEntry, "/beer");

        final FolderEntry secondFolderEntry = new FolderEntry(FolderEntry.ContentType.DATASET, "32ac4646aa98b51");
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
        final FolderEntry folderEntry = new FolderEntry(FolderEntry.ContentType.DATASET, "6f8a54051bc454");
        createFolderEntry(folderEntry, "/beer");

        //when
        final Response response = removeFolder("/beer");

        //then
        assertThat(response.getStatusCode(), is(409));
        final List<Folder> folders = getFolderContent("/");
        assertThat(folders, hasSize(1));
    }

    @Test
    public void should_fetch_folder_content_ordered_by_date_desc() throws Exception {
        //given
        createFolder("foo/beer");
        createFolder("foo/bar");

        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "cccc", "text/csv");

        final FolderEntry folderEntry1 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId1);
        createFolderEntry(folderEntry1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId2);
        createFolderEntry(folderEntry2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId3);
        createFolderEntry(folderEntry3, "/foo");

        final ObjectMapper mapper = builder.build();

        //when
        String list = when() //
                .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "desc", "foo") //
                .asString();

        //then
        final FolderContent folderContent = mapper.readValue(list, FolderContent.class);
        assertThat(folderContent.getDatasets(), hasSize(3));
        assertThat(folderContent.getDatasets().get(0).getId(), is(dataSetId3));
        assertThat(folderContent.getDatasets().get(1).getId(), is(dataSetId2));
        assertThat(folderContent.getDatasets().get(2).getId(), is(dataSetId1));
    }

    @Test
    public void should_fetch_folder_content_ordered_by_date_asc() throws Exception {
        //given
        createFolder("foo");

        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "cccc", "text/csv");

        final FolderEntry folderEntry1 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId1);
        createFolderEntry(folderEntry1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId2);
        createFolderEntry(folderEntry2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId3);
        createFolderEntry(folderEntry3, "/foo");

        final ObjectMapper mapper = builder.build();

        //when
        String list = when() //
                .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "asc", "foo") //
                .asString();

        //then
        final FolderContent folderContent = mapper.readValue(list, FolderContent.class);
        assertThat(folderContent.getDatasets(), hasSize(3));
        assertThat(folderContent.getDatasets().get(0).getId(), is(dataSetId1));
        assertThat(folderContent.getDatasets().get(1).getId(), is(dataSetId2));
        assertThat(folderContent.getDatasets().get(2).getId(), is(dataSetId3));
    }

    @Test
    public void should_fetch_folder_content_ordered_by_name_asc() throws Exception {
        //given
        createFolder("foo");

        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "cccc", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        final FolderEntry folderEntry1 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId1);
        createFolderEntry(folderEntry1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId2);
        createFolderEntry(folderEntry2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId3);
        createFolderEntry(folderEntry3, "/foo");

        final ObjectMapper mapper = builder.build();

        //when
        String list = when() //
                .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "name", "asc", "foo") //
                .asString();

        //then
        final FolderContent folderContent = mapper.readValue(list, FolderContent.class);
        assertThat(folderContent.getDatasets(), hasSize(3));
        assertThat(folderContent.getDatasets().get(0).getId(), is(dataSetId1));
        assertThat(folderContent.getDatasets().get(1).getId(), is(dataSetId3));
        assertThat(folderContent.getDatasets().get(2).getId(), is(dataSetId2));
    }

    @Test
    public void should_fetch_folder_content_ordered_by_name_desc() throws Exception {
        //given
        createFolder("foo");

        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "cccc", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");

        final FolderEntry folderEntry1 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId1);
        createFolderEntry(folderEntry1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId2);
        createFolderEntry(folderEntry2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry(FolderEntry.ContentType.DATASET, dataSetId3);
        createFolderEntry(folderEntry3, "/foo");

        final ObjectMapper mapper = builder.build();

        //when
        String list = when() //
                .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "name", "desc", "foo") //
                .asString();

        //then
        final FolderContent folderContent = mapper.readValue(list, FolderContent.class);
        assertThat(folderContent.getDatasets(), hasSize(3));
        assertThat(folderContent.getDatasets().get(0).getId(), is(dataSetId2));
        assertThat(folderContent.getDatasets().get(1).getId(), is(dataSetId3));
        assertThat(folderContent.getDatasets().get(2).getId(), is(dataSetId1));
    }

    @Test
    public void add_then_search_folders() throws Exception {
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

    protected void assertOnSearch(String searchQuery, int expectedSize) throws Exception {
        //when
        final Response response = RestAssured.given() //
                .queryParam("pathName", searchQuery).when() //
                .get("/api/folders/search");
        final List<Folder> folders = builder.build().readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        //then
        Assertions.assertThat(folders).hasSize(expectedSize);
    }

    protected void createFolder(final String path) {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }

    protected Response removeFolder(final String path) {
        return RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .delete("/api/folders");
    }

    protected List<Folder> getAllFolders() throws IOException {
        final Response response = RestAssured.given() //
                .when() //
                .get("/api/folders/all");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return builder.build().readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    protected List<Folder> getFolderContent(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return builder.build().readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    protected FolderEntry createFolderEntry(final FolderEntry folderEntry, String path) throws JsonProcessingException {
        return folderRepository.addFolderEntry(folderEntry, path);
    }

    protected void removeFolderEntry(final String contentId) {
        final Response response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .pathParam("contentType", FolderEntry.ContentType.DATASET) //
                .pathParam("id", contentId) //
                .delete("/api/folders/entries/{contentType}/{id}");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }

    protected List<FolderEntry> getFolderEntries(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .queryParam("contentType", FolderEntry.ContentType.DATASET) //
                .get("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return builder.build().readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------INVENTORY------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------


    @Test
    public void shouldReturnMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testInventoryOfPreparations", "text/csv");

        // when
        String inventory = given().queryParam("path", "/").queryParam("name", "Inventory").get("/api/inventory/search")
                .asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inventory);
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventoryOfPreparations", preparationList.get(0).getName());
    }

    @Test
    public void shouldReturnMatchingPreparationsWithSpaceWhenPerformingInventory() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "testInventory OfPreparations", "text/csv");

        // when
        String inventory = given().queryParam("path", "/").queryParam("name", "Inventory ").get("/api/inventory/search")
                .asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inventory);
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(1));
        assertEquals("testInventory OfPreparations", preparationList.get(0).getName());
    }

    @Test
    public void shouldNotReturnNonMatchingPreparationsWhenPerformingInventory() throws IOException {
        // given
        final String preparationId = createPreparationFromFile("t-shirt_100.csv", "nonMatchingPreparation", "text/csv");

        // when
        String inventory = given().queryParam("path", "/").queryParam("name", "Inventory").get("/api/inventory/search")
                .asString();

        // then
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inventory);
        JsonNode preparations = rootNode.get("preparations");
        List<Preparation> preparationList = mapper.readValue(preparations.toString(), new TypeReference<List<Preparation>>(){});
        assertThat(preparationList.size(), is(0));
    }
}
