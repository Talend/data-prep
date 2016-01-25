package org.talend.dataprep.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.folder.FolderEntry;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FolderAPITest extends ApiServiceTestBase {

    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void cleanupFolder() throws Exception {
        FileUtils.deleteDirectory(Paths.get("target/test/store/folders").toFile());
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
        assertThat(folders.get(0).getId(), is("beer"));
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
        assertThat(folders.get(0).getId(), is("beer/alcohol"));
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
        final FolderEntry folderEntry = new FolderEntry(DataSet.class.getName(), "6f8a54051bc454", "/beer");

        //when
        final FolderEntry createdEntry = createFolderEntry(folderEntry);

        //then
        final List<FolderEntry> entries = getFolderEntries("/beer");
        assertThat(entries, hasSize(1));
        assertThat(entries, contains(createdEntry));
    }

    @Test
    public void should_remove_entry_from_folder() throws IOException {
        //given
        createFolder("beer");
        final FolderEntry firstFolderEntry = new FolderEntry(DataSet.class.getName(), "6f8a54051bc454", "/beer");
        final FolderEntry secondFolderEntry = new FolderEntry(DataSet.class.getName(), "32ac4646aa98b51", "/beer");
        final FolderEntry firstCreatedEntry = createFolderEntry(firstFolderEntry);
        final FolderEntry secondCreatedEntry = createFolderEntry(secondFolderEntry);

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
        final FolderEntry folderEntry = new FolderEntry(DataSet.class.getName(), "6f8a54051bc454", "/beer");
        createFolderEntry(folderEntry);

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

        final FolderEntry folderEntry1 = new FolderEntry("dataset", dataSetId1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry("dataset", dataSetId2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry("dataset", dataSetId3, "/foo");
        createFolderEntry(folderEntry1);
        createFolderEntry(folderEntry2);
        createFolderEntry(folderEntry3);

        final ObjectMapper mapper = new ObjectMapper();

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

        final FolderEntry folderEntry1 = new FolderEntry("dataset", dataSetId1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry("dataset", dataSetId2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry("dataset", dataSetId3, "/foo");
        createFolderEntry(folderEntry1);
        createFolderEntry(folderEntry2);
        createFolderEntry(folderEntry3);

        final ObjectMapper mapper = new ObjectMapper();

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

        final FolderEntry folderEntry1 = new FolderEntry("dataset", dataSetId1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry("dataset", dataSetId2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry("dataset", dataSetId3, "/foo");
        createFolderEntry(folderEntry1);
        createFolderEntry(folderEntry2);
        createFolderEntry(folderEntry3);

        final ObjectMapper mapper = new ObjectMapper();

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

        final FolderEntry folderEntry1 = new FolderEntry("dataset", dataSetId1, "/foo");
        final FolderEntry folderEntry2 = new FolderEntry("dataset", dataSetId2, "/foo");
        final FolderEntry folderEntry3 = new FolderEntry("dataset", dataSetId3, "/foo");
        createFolderEntry(folderEntry1);
        createFolderEntry(folderEntry2);
        createFolderEntry(folderEntry3);

        final ObjectMapper mapper = new ObjectMapper();

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
        final List<Folder> folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
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
        return objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    protected List<Folder> getFolderContent(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    protected FolderEntry createFolderEntry(final FolderEntry folderEntry) throws JsonProcessingException {
        final Response response = RestAssured.given() //
                .body(objectMapper.writer().writeValueAsBytes(folderEntry)) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return response.as(FolderEntry.class);
    }

    protected void removeFolderEntry(final String contentId) {
        final Response response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .pathParam("contentType", DataSet.class.getName()) //
                .pathParam("id", contentId) //
                .delete("/api/folders/entries/{contentType}/{id}");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
    }

    protected List<FolderEntry> getFolderEntries(final String path) throws IOException {
        final Response response = RestAssured.given() //
                .queryParam("path", path) //
                .queryParam("contentType", DataSet.class.getName()) //
                .get("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return objectMapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });
    }
}
