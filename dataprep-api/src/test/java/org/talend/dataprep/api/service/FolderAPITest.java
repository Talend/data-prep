package org.talend.dataprep.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.folder.Folder;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FolderAPITest extends ApiServiceTestBase {

    Logger logger = LoggerFactory.getLogger(getClass());


    @Before
    public void cleanupFolder() throws Exception {
        FileUtils.deleteDirectory(Paths.get("target/test/store/folders").toFile());
    }

    @Test
    public void add_then_remove_folder() throws Exception {

        // create foo folder under root
        Response response = RestAssured.given() //
                .queryParam("path", "foo")
                .when() //
                .put("/api/folders");

        Folder created = response.as(Folder.class);

        logger.info("created: {}", created);

        // create beer under foo
        response = RestAssured.given() //
                .queryParam("path", "foo/beer") //
                .when() //
                .put("/api/folders");

        created = response.as(Folder.class);

        logger.info("created: {}", created);


        // create wine under foo
        response = RestAssured.given() //
                .queryParam("path", "foo/wine") //
                .when() //
                .put("/api/folders");

        created = response.as(Folder.class);

        logger.info("created: {}", created);

        response = RestAssured.given() //
                .when() //
                .get("/api/folders");

        logger.info("response: {}", response.asString());

        response = RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .get("/api/folders");


        ObjectMapper objectMapper = new ObjectMapper();


        List<Folder> folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>(){});

        List<Folder> expected = Lists.newArrayList(Folder.Builder.folder().path("foo/beer/").build(), //
                Folder.Builder.folder().path("foo/wine/").build());

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2) //
                .usingElementComparatorOnFields("path").containsAll(expected);


        RestAssured.given() //
                .queryParam("path", "foo/wine") //
                .when() //
                .delete("/api/folders");


        response = RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .get("/api/folders");


        folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>(){});

        expected = Lists.newArrayList(Folder.Builder.folder().path("foo/beer/").build());

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1) //
                .usingElementComparatorOnFields("path").containsAll(expected);


        RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .delete("/api/folders");

        response = RestAssured.given() //
                .when() //
                .get("/api/folders");

        folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>(){});

        Assertions.assertThat(folders).isNotNull().isEmpty();
    }

}
