// ============================================================================
//
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

package org.talend.dataprep.preparation.service;

import static com.jayway.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.preparation.BasePreparationTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;

/**
 * Unit/integration tests for the FolderService
 */
public class FolderServiceTest extends BasePreparationTest {


    @Test
    public void shouldListChildren() throws Exception {
        // given
        createFolder("foo");
        createFolder("foo/bar");
        createFolder("foo/toto");

        // when
        final Response response = given() //
                .queryParam("path", "foo") //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders");

        // then
        assertThat(response.getStatusCode(), is(200));
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
        final List<String> foldersNames = folders.stream().map(Folder::getName).collect(toList());
        assertThat(foldersNames.size(), is(2));
        assertThat(foldersNames, containsInAnyOrder("bar", "toto"));
    }

    @Test
    public void childrenShouldCountPreparations() throws Exception {
        // given
        String foo = "/foo";
        createFolder(foo);
        String bar = foo + "/bar";
        createFolder(bar);

        long expectedNbPreparations = 12;
        for (int i=0; i<expectedNbPreparations; i++) {
            createPreparationWithAPI("{\"name\": \"prep_"+ i +"\", \"dataSetId\": \"1234\"}", bar);
        }

        // when
        final Response response = given() //
                .queryParam("path", foo) //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .get("/folders");

        // then
        assertThat(response.getStatusCode(), is(200));
        final List<Folder> folders = mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {});
        assertThat(folders.size(), is(1));
        final Folder folder = folders.get(0);
        assertThat(folder.getNbPreparations(), is(expectedNbPreparations));
    }

    @Test
    public void shouldNotFindFolder() throws Exception {
        // when
        final Response response = given().queryParam("path", "should/not/be/found") //
                .expect().statusCode(404).log().ifError()//
                .when() //
                .get("/folders");

        // then
        assertThat(response.getStatusCode(), is(404));
    }

    @Test
    public void shouldThrowAnExceptionIfBadContentTypeWhenAskingForFolderEntryRemoval() throws Exception {
        // given
        createFolder("foo");

        // when
        final Response response = given() //
                .queryParam("path", "foo") //
                .expect().statusCode(400).log().ifError()//
                .when() //
                .delete("/folders/entries/non-existent/1");

        // then
        assertThat(response.getStatusCode(), is(400));
        checkErrorResponse(response.asString());
    }

    @Test
    public void shouldThrowAnExceptionIfBadContentTypeWhenAskingForFolderEntries() throws Exception {
        // given
        createFolder("foo");

        // when
        final Response response = given() //
                .queryParam("path", "foo") //
                .queryParam("contentType", "non-existent") //
                .expect().statusCode(400).log().ifError() //
                .when() //
                .get("/folders/entries");

        // then
        assertThat(response.getStatusCode(), is(400));
        checkErrorResponse(response.asString());
    }

    private void createFolder(String path) {
        given().queryParam("path", path) //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .put("/folders").then().assertThat().statusCode(200);
    }

    private void checkErrorResponse(String response) throws IOException {
        JsonNode rootNode = mapper.readTree(response);
        assertTrue(rootNode.has("code"));
        assertTrue(rootNode.has("message"));
        assertTrue(rootNode.has("message_title"));
        assertTrue(rootNode.has("context"));
    }
}
