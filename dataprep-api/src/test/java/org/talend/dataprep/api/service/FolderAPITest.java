//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.DATASET;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.folder.FolderInfo;
import org.talend.dataprep.api.folder.FolderTreeNode;
import org.talend.dataprep.exception.error.FolderErrorCodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;

/**
 * Unit tests for the folder API.
 */
public class FolderAPITest extends ApiServiceTestBase {
    private Folder home;

    @Before
    public void cleanupFolder() throws Exception {
        folderRepository.clear();
        home = folderRepository.getHome();
    }

    @Test
    public void should_create_folder_in_root() throws IOException {
        //given
        final List<Folder> originalFolders = getFolderChildren(home.getId());
        assertThat(originalFolders, hasSize(0));

        //when
        createFolder("beer", home.getId());

        //then
        final List<Folder> folders = getFolderChildren(home.getId());
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("beer"));
        assertThat(folders.get(0).getPath(), is("/beer"));
    }

    @Test
    public void should_create_nested_folder() throws IOException {
        //given
        createFolder("beer", home.getId());
        final Folder beer = getFolder(home.getId(), "beer");
        final List<Folder> originalFolders = getFolderChildren(beer.getId());
        assertThat(originalFolders, hasSize(0));

        //when
        createFolder("/beer/alcohol", home.getId());

        //then
        final List<Folder> folders = getFolderChildren(beer.getId());
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("alcohol"));
        assertThat(folders.get(0).getPath(), is("/beer/alcohol"));
    }

    @Test
    public void should_remove_empty_folder() throws IOException {
        //given
        createFolder("beer", home.getId());
        final Folder beer = getFolder(home.getId(), "beer");

        //when
        final Response response = removeFolder(beer.getId());

        //then
        assertThat(response.getStatusCode(), is(200));
        final List<Folder> folders = getFolderChildren(home.getId());
        assertThat(folders, is(empty()));
    }

    @Test
    public void should_return_conflict_on_non_empty_folder_remove() throws IOException {
        //given
        createFolder("beer", home.getId());
        final Folder beer = getFolder(home.getId(), "beer");
        final FolderEntry folderEntry = new FolderEntry(DATASET, "6f8a54051bc454");
        createFolderEntry(folderEntry, beer.getId());

        //when
        final Response response = removeFolder(beer.getId());

        //then
        assertThat(response.getStatusCode(), is(409));
        final String content = response.asString();
        assertTrue(StringUtils.isNoneBlank(content));
        assertTrue(content.contains(FolderErrorCodes.FOLDER_NOT_EMPTY.name()));

        final List<Folder> folders = getFolderChildren(home.getId());
        assertThat(folders, hasSize(1));
    }

    /**
     * This test does not check the whole content (this is already done by unit tests in lower services) but make sure
     * the plumbing is ok.
     */
    @Test
    public void shouldListPreparationsInFolder() throws Exception {
        // given
        folderRepository.addFolder(home.getId(), "/one");
        folderRepository.addFolder(home.getId(), "/two");
        testClient.createPreparationFromFile("dataset/dataset.csv", "yet another preparation", home.getId());
        testClient.createPreparationFromFile("dataset/dataset.csv", "prep 2", home.getId());
        testClient.createPreparationFromFile("dataset/dataset.csv", "preparation 3 !", home.getId());

        // when
        final Response response = given() //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/api/folders/{id}/preparations", home.getId());

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
            // check for dataset
            assertNotNull(dataset);
            assertTrue(dataset.has("dataSetId"));
            assertTrue(dataset.has("dataSetName"));
            assertTrue(dataset.has("dataSetNbRow"));
            // check for owner
            assertTrue(preparation.has("owner"));
            assertFalse(preparation.get("owner").isNull());
        }
    }

    /**
     * See https://jira.talendforge.org/browse/TDP-3456
     */
    @Test
    public void listPreparationsByFolder_shouldListMoreThan32Preparations_TDP3456() throws Exception {
        // given
        int numberOfPreparations = 33;
        for (int preparationId = 0; preparationId < numberOfPreparations; preparationId++) {
            testClient.createPreparationFromFile("dataset/dataset.csv", "preparation " + preparationId, home.getId());
        }

        // when
        final Response response = given() //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/api/folders/{id}/preparations", home.getId());

        // then
        assertThat(response.getStatusCode(), is(200));
        final JsonNode rootNode = mapper.readTree(response.asInputStream());

        final JsonNode preparations = rootNode.get("preparations");
        assertNotNull(preparations);
        assertEquals(numberOfPreparations, preparations.size());
    }

    @Test
    public void should_rename_folder() throws IOException {
        //given
        createFolder("beer", home.getId());
        final Folder beer = getFolder(home.getId(), "beer");

        final String name = "new beer";

        //when
        final Response response = given() //
                .body(name) //
                .when() //
                .put("/api/folders/{id}/name", beer.getId());

        //then
        assertThat(response.getStatusCode(), is(200));
        final Folder updatedBeer = getFolder(home.getId(), name);
        assertNotNull(updatedBeer);
    }

    @Test
    public void search_folders() throws Exception {
        // given
        final boolean strict = true;
        final boolean nonStrict = false;
        createFolder("foo", home.getId());
        createFolder("bar", home.getId());
        createFolder("foo/beer", home.getId());
        createFolder("foo/wine", home.getId());
        createFolder("foo/wine/toto", home.getId());
        createFolder("foo/wine/titi", home.getId());
        createFolder("foo/wine/thetiti", home.getId());
        createFolder("foo/wine/yupTITI", home.getId());
        createFolder("foo/wine/yeahTITI", home.getId());
        createFolder("foo/wine/Goodwine", home.getId());
        createFolder("foo/wine/verygoodWInE", home.getId());

        assertOnSearch("foo", nonStrict, 1);
        assertOnSearch("wine", nonStrict, 3);
        assertOnSearch("tIti", nonStrict, 4);
        assertOnSearch("GoOd", nonStrict, 2);

        assertOnSearch("titi", strict, 1);
        assertOnSearch("good", strict, 0);
    }
    @Test
    public void shouldReturnEntireFolderTree() throws Exception {
        // given
        //                        HOME
        //              ___________|____________
        //              |                       |
        //             first                  second
        //          ____|____                   |
        //          |        |                  |
        //first child 1   first child 2     second child
        //                                      |
        //                                      |
        //                                  second child child

        createFolder("first", home.getId());
        createFolder("second", home.getId());

        final Folder firstFolder = getFolder(home.getId(), "first");
        final Folder secondFolder = getFolder(home.getId(), "second");
        createFolder( "first child one", firstFolder.getId());
        createFolder("first child two", firstFolder.getId());
        createFolder("second child", secondFolder.getId());

        final Folder secondChildFolder = getFolder(secondFolder.getId(), "second child");
        createFolder("second child child", secondChildFolder.getId());

        // when
        final Response response = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/tree");

        // then
        final FolderTreeNode tree = mapper.readValue(response.asString(), new TypeReference<FolderTreeNode>() {
        });
        assertTree(tree, "/", new String[]{"/first", "/second"});

        final FolderTreeNode firstFolderNode = getChild(tree, "first");
        final FolderTreeNode secondFolderNode = getChild(tree, "second");
        assertTree(firstFolderNode, "/first", new String[]{"/first/first child one", "/first/first child two"});
        assertTree(secondFolderNode, "/second", new String[]{"/second/second child"});

        final FolderTreeNode secondChildFolderNode = getChild(secondFolderNode, "second child");
        assertTree(secondChildFolderNode, "/second/second child", new String[]{"/second/second child/second child child"});
    }

    @Test
    public void shouldFolderMetadataWithHierarchy() throws Exception {
        // given
        //                        HOME
        //              ___________|____________
        //              |                       |
        //             first                  second
        //          ____|____                   |
        //          |        |                  |
        //first child 1   first child 2     second child
        //                                      |
        //                                      |
        //                                  second child child

        createFolder("first", home.getId());
        createFolder("second", home.getId());

        final Folder firstFolder = getFolder(home.getId(), "first");
        final Folder secondFolder = getFolder(home.getId(), "second");
        createFolder( "first child one", firstFolder.getId());
        createFolder("first child two", firstFolder.getId());
        createFolder("second child", secondFolder.getId());

        final Folder secondChildFolder = getFolder(secondFolder.getId(), "second child");
        createFolder("second child child", secondChildFolder.getId());

        final Folder firstChildTwo = getFolder(firstFolder.getId(), "first child two");

        // when
        final Response response = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/{id}", firstChildTwo.getId());

        // then
        final FolderInfo infos = mapper.readValue(response.asString(), new TypeReference<FolderInfo>() {
        });
        final List<Folder> hierarchy = StreamSupport.stream(infos.getHierarchy().spliterator(), false).collect(toList());
        assertThat(infos.getFolder(), equalTo(firstChildTwo));
        assertThat(hierarchy, hasSize(2));
        assertThat(hierarchy.get(0).getId(), equalTo(home.getId()));
        assertThat(hierarchy.get(1).getId(), equalTo(firstFolder.getId()));
    }

    private void assertTree(final FolderTreeNode node, final String nodePath, final String[] expectedChildrenPaths) {
        assertThat(node.getFolder().getPath(), is(nodePath));
        assertThat(node.getChildren(), hasSize(expectedChildrenPaths.length));
        final List<String> actualChildrenPaths = node.getChildren()
                .stream()
                .map((folderTreeNode -> folderTreeNode.getFolder().getPath()))
                .collect(toList());
        assertThat(actualChildrenPaths, containsInAnyOrder(expectedChildrenPaths));
    }

    private FolderTreeNode getChild(final FolderTreeNode tree, final String childName) {
        return tree.getChildren()
                .stream()
                .filter((child) -> child.getFolder().getName().equals(childName))
                .findFirst()
                .orElse(null);
    }

    private void assertOnSearch(final String searchQuery, final boolean strict, final int expectedSize) throws Exception {
        //when
        final Response response = given() //
                .queryParam("name", searchQuery) //
                .queryParam("strict", strict) //
                .when() //
                .get("/api/folders/search");
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        //then
        assertThat(folders, hasSize(expectedSize));
    }

    private void createFolder(final String path, final String parentId) {
        final Response response = given() //
                .queryParam("parentId", parentId) //
                .queryParam("path", path) //
                .when() //
                .put("/api/folders");

        assertThat(response.getStatusCode(), is(200));
    }

    private Response removeFolder(final String id) throws UnsupportedEncodingException {
        return given() //
                .when() //
                .delete("/api/folders/" + id);
    }

    private List<Folder> getFolderChildren(final String id) throws IOException {
        final Response response = given() //
                .queryParam("parentId", id) //
                .when() //
                .get("/api/folders");

        assertThat(response.getStatusCode(), is(200));
        return mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }

    private FolderEntry createFolderEntry(final FolderEntry folderEntry, String folderId) throws JsonProcessingException {
        return folderRepository.addFolderEntry(folderEntry, folderId);
    }

    private Folder getFolder(final String parentId, final String name) throws IOException {
        return getFolderChildren(parentId) //
                .stream() //
                .filter((folder) -> folder.getName().equals(name)) //
                .findFirst() //
                .orElse(null);
    }
}
