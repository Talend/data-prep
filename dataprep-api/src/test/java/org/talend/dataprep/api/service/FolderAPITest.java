package org.talend.dataprep.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.folder.Folder;

import java.nio.file.Paths;

public class FolderAPITest extends ApiServiceTestBase {

    Logger logger = LoggerFactory.getLogger(getClass());


    @Before
    public void cleanupFolder() throws Exception {
        FileUtils.deleteDirectory(Paths.get("target/test/store/folders").toFile());
    }

    @Test
    public void add_then_list_folders() throws Exception {

        // create foo folder under root
        Response response = RestAssured.given() //
                .queryParam("path", "foo")
                .when() //
                .get("/api/folders/add");

        Folder created = response.as(Folder.class);

        logger.info("created: {}", created);

        // create beer under foo
        response = RestAssured.given() //
                .queryParam("path", "beer") //
                .queryParam("parentPath", "foo") //
                .when() //
                .get("/api/folders/add");

        created = response.as(Folder.class);

        logger.info("created: {}", created);


        // create wine under foo
        response = RestAssured.given() //
                .queryParam("path", "wine") //
                .queryParam("parentPath", "foo") //
                .when() //
                .get("/api/folders/add");

        created = response.as(Folder.class);

        logger.info("created: {}", created);

        response = RestAssured.given() //
                .when() //
                .get("/api/folders/childs");

        logger.info("response: {}", response.asString());

    }

}
