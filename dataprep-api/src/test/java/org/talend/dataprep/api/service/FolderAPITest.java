package org.talend.dataprep.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.folder.Folder;

@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=file",
        "folder.store.file.location=target/test/store/folders" })
public class FolderAPITest extends ApiServiceTestBase {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void add_then_list_folders() throws Exception {

        // create foo folder under root
        Folder foo = Folder.Builder.folder().name("foo").build();

        ObjectMapper objectMapper = new ObjectMapper();

         Response response = RestAssured.given() //
                .content(foo) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders");

        Folder created = response.as(Folder.class);

        logger.info("created: {}", created);

        // create beer under foo
        Folder beer = Folder.Builder.folder().name("beer").build();

        response = RestAssured.given() //
                .content(foo) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders?parentId={id}", created.getId());

        created = response.as(Folder.class);

        logger.info("created: {}", created);


        // create wine under foo
        Folder wine = Folder.Builder.folder().name("wine").build();

        response = RestAssured.given() //
                .content(foo) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders?parentId={id}", created.getId());

        created = response.as(Folder.class);

        logger.info("created: {}", created);

        response = RestAssured.given() //
                .when() //
                .get("/api/folders/root/childs");

        logger.info("response: {}", response.asString());

    }

}
