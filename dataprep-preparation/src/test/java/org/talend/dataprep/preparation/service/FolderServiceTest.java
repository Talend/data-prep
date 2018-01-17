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
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderInfo;
import org.talend.dataprep.api.folder.FolderTreeNode;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.preparation.BasePreparationTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;

/**
 * Unit/integration tests for the FolderService
 */
public class FolderServiceTest extends BasePreparationTest {

    @Test
    public void shouldListChildren() throws Exception {
        // given
        createFolder(home.getId(), "/bar");
        createFolder(home.getId(), "/toto");

        // when
        final List<Folder> folders = getFolderChildren(home.getId());

        // then
        final List<String> foldersNames = folders.stream().map(Folder::getName).collect(toList());
        assertThat(foldersNames.size(), is(2));
        assertThat(foldersNames, containsInAnyOrder("bar", "toto"));
    }

    @Test
    public void childrenShouldContainsPreparationsCount() throws Exception {
        // given
        createFolder(home.getId(), "foo");
        final Folder fooFolder = getFolder(home.getId(), "foo");

        long expectedNbPreparations = 12;
        for (int i = 0; i < expectedNbPreparations; i++) {
            Preparation preparation = new Preparation();
            preparation.setName("prep_" + i);
            preparation.setDataSetId("1234");
            preparation.setRowMetadata(new RowMetadata());
            clientTest.createPreparation(preparation, fooFolder.getId());
        }

        // when
        final List<Folder> folders = getFolderChildren(home.getId());

        // then
        assertThat(folders.size(), is(1));
        assertThat(folders.get(0).getNbPreparations(), is(expectedNbPreparations));
    }

    @Test
    public void shouldNotFindFolder() throws Exception {
        // when
        final Response response = given() //
                .expect().statusCode(404).log().ifError()//
                .when() //
                .get("/folders?parentId={parentId}", "unknownId");

        // then
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldSearchFolderByName() throws Exception {
        // given
        createFolder(home.getId(), "foo");
        createFolder(home.getId(), "bar");

        // when
        final Response response = given() //
                .queryParam("name", "foo") //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/search");

        // then
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("foo"));
    }

    @Test
    public void searchFolderShouldUpdateNbPreparationsFound() throws Exception {
        // given
        createFolder(home.getId(), "foo");
        final Folder foo = getFolder(home.getId(), "foo");
        Preparation preparation = new Preparation();
        preparation.setName("test_name");
        preparation.setDataSetId("1234");
        preparation.setRowMetadata(new RowMetadata());
        clientTest.createPreparation(preparation, foo.getId());

        // when
        final Response response = given() //
                .queryParam("name", "foo") //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/search");

        // then
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
        assertThat(folders, hasSize(1));
        final Folder folder = folders.get(0);
        assertThat(folder.getName(), is("foo"));
        assertEquals(1, folder.getNbPreparations());
    }

    @Test
    public void shouldRemoveFolder() throws Exception {
        // given
        createFolder(home.getId(), "foo");
        createFolder(home.getId(), "bar");

        final Folder fooFolder = getFolder(home.getId(), "foo");

        // when
        given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .delete("/folders/" + fooFolder.getId());

        // then
        final List<Folder> folders = getFolderChildren(home.getId());
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("bar"));
    }

    @Test
    public void folderNotFoundShouldThrow404() {
        // when
        given() //
                .expect().statusCode(404).log().ifError()//
                .when() //
                .get("/folders/mlkjmlkjmlkj");
    }

    @Test
    public void shouldRenameFolderAndItsChildren() throws Exception {
        // given
        createFolder(home.getId(), "foo");

        final Folder fooFolder = getFolder(home.getId(), "foo");

        createFolder(fooFolder.getId(), "fooChildOne");
        createFolder(fooFolder.getId(), "fooChildTwo");

        // when
        given() //
                .body("faa").expect().statusCode(200).log().ifError()//
                .when() //
                .put("/folders/" + fooFolder.getId() + "/name");

        // then
        final List<Folder> folders = getFolderChildren(home.getId());
        assertThat(folders, hasSize(1));
        assertThat(folders.get(0).getName(), is("faa"));

        final List<Folder> children = getFolderChildren(folders.get(0).getId());
        assertThat(children, hasSize(2));
        final List<String> childrenPath = children.stream().map(Folder::getPath).collect(toList());
        assertThat(childrenPath, containsInAnyOrder("/faa/fooChildOne", "/faa/fooChildTwo"));
    }

    @Test
    public void shouldReturnEntireFolderTree() throws Exception {
        // @formatter:off
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
        // @formatter:on
        createFolder(home.getId(), "first");
        createFolder(home.getId(), "second");

        final Folder firstFolder = getFolder(home.getId(), "first");
        final Folder secondFolder = getFolder(home.getId(), "second");
        createFolder(firstFolder.getId(), "first child one");
        createFolder(firstFolder.getId(), "first child two");
        createFolder(secondFolder.getId(), "second child");

        final Folder secondChildFolder = getFolder(secondFolder.getId(), "second child");
        createFolder(secondChildFolder.getId(), "second child child");

        // when
        final Response response = given() //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/tree");

        // then
        final FolderTreeNode tree = mapper.readValue(response.asString(), new TypeReference<FolderTreeNode>() {
        });
        assertTree(tree, "/", new String[] { "/first", "/second" });

        final FolderTreeNode firstFolderNode = getChild(tree, "first");
        final FolderTreeNode secondFolderNode = getChild(tree, "second");
        assertTree(firstFolderNode, "/first", new String[] { "/first/first child one", "/first/first child two" });
        assertTree(secondFolderNode, "/second", new String[] { "/second/second child" });

        final FolderTreeNode secondChildFolderNode = getChild(secondFolderNode, "second child");
        assertTree(secondChildFolderNode, "/second/second child", new String[] { "/second/second child/second child child" });
    }

    @Test
    public void shouldFolderMetadataWithHierarchy() throws Exception {
        // @formatter:off
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
        // @formatter:on
        createFolder(home.getId(), "first");
        createFolder(home.getId(), "second");

        final Folder firstFolder = getFolder(home.getId(), "first");
        final Folder secondFolder = getFolder(home.getId(), "second");
        createFolder(firstFolder.getId(), "first child one");
        createFolder(firstFolder.getId(), "first child two");
        createFolder(secondFolder.getId(), "second child");

        final Folder secondChildFolder = getFolder(secondFolder.getId(), "second child");
        createFolder(secondChildFolder.getId(), "second child child");

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

    @Test
    public void shouldSearchFolders() throws Exception {
        // given
        final boolean strict = true;
        final boolean nonStrict = false;

        createFolder(home.getId(), "foo");
        createFolder(home.getId(), "foo/bar");
        createFolder(home.getId(), "foo/toto");
        createFolder(home.getId(), "foo/bar/tototo");

        // when / then
        assertSearch("toto", strict, new String[] { "/foo/toto" });
        assertSearch("tot", nonStrict, new String[] { "/foo/toto", "/foo/bar/tototo" });
    }

    private void assertSearch(final String name, final boolean strict, final String[] expectedResultPaths) throws IOException {
        final Response response = given() //
                .queryParam("name", name) //
                .queryParam("strict", strict) //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders/search");

        // then
        assertThat(response.getStatusCode(), is(200));
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
        final List<String> foldersNames = folders.stream().map(Folder::getPath).collect(toList());
        assertThat(foldersNames.size(), is(expectedResultPaths.length));
        assertThat(foldersNames, containsInAnyOrder(expectedResultPaths));
    }

    private void assertTree(final FolderTreeNode node, final String nodePath, final String[] expectedChildrenPaths) {
        assertThat(node.getFolder().getPath(), is(nodePath));
        assertThat(node.getChildren(), hasSize(expectedChildrenPaths.length));
        final List<String> actualChildrenPaths = node.getChildren().stream()
                .map((folderTreeNode -> folderTreeNode.getFolder().getPath())).collect(toList());
        assertThat(actualChildrenPaths, containsInAnyOrder(expectedChildrenPaths));
    }

    private FolderTreeNode getChild(final FolderTreeNode tree, final String childName) {
        return tree.getChildren().stream().filter((child) -> child.getFolder().getName().equals(childName)).findFirst()
                .orElse(null);
    }
}
