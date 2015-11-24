package org.talend.dataprep.api.service;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.folder.FolderEntry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FolderAPITest extends ApiServiceTestBase {

    ObjectMapper objectMapper = new ObjectMapper();

    Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void cleanupFolder() throws Exception {
        FileUtils.deleteDirectory(Paths.get("target/test/store/folders").toFile());
    }

    @Test
    public void add_then_remove_folder() throws Exception {

        // create foo folder under root
        Response response = RestAssured.given() //
                .queryParam("path", "foo").when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        Folder created = response.as(Folder.class);

        logger.info("created: {}", created);

        // create beer under foo
        response = RestAssured.given() //
                .queryParam("path", "foo/beer") //
                .when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        created = response.as(Folder.class);

        logger.info("created: {}", created);

        // create wine under foo
        response = RestAssured.given() //
                .queryParam("path", "foo/wine") //
                .when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        created = response.as(Folder.class);

        logger.info("created: {}", created);

        response = RestAssured.given() //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        logger.info("response: {}", response.asString());

        response = RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        List<Folder> folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        List<Folder> expected = Lists.newArrayList(Folder.Builder.folder().path("foo/beer").name( "beer" ).build(), //
                Folder.Builder.folder().path("foo/wine").name( "wine" ).build());

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2) //
            .usingElementComparatorOnFields( "path", "name" ).containsAll(expected);

        // requesting all folders
        response = RestAssured.given() //
                .when() //
                .get("/api/folders/all");

        folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        expected = Lists.newArrayList(Folder.Builder.folder().path("foo/beer").build(), //
                Folder.Builder.folder().path("foo/wine").build(), //
                Folder.Builder.folder().path("foo").build());

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(3) //
                .usingElementComparatorOnFields("path").containsAll(expected);

        response = RestAssured.given() //
                .queryParam("path", "foo/wine") //
                .when() //
                .delete("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        response = RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        expected = Lists.newArrayList(Folder.Builder.folder().path("foo/beer").build());

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1) //
                .usingElementComparatorOnFields("path").containsAll(expected);

        response = RestAssured.given() //
                .queryParam("path", "foo") //
                .when() //
                .delete("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        response = RestAssured.given() //
                .when() //
                .get("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        folders = objectMapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });

        Assertions.assertThat(folders).isNotNull().isEmpty();
    }

    @Test
    public void add_then_remove_folderentry() throws Exception {

        // create beer folder under root
        Response response = RestAssured.given() //
                .queryParam("path", "beer").when() //
                .put("/api/folders");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        FolderEntry folderEntry = new FolderEntry(DataSet.class.getName(), Long.toString(System.nanoTime()), "/beer");

        // create a folderentry in this directory
        response = RestAssured.given() //
                .body(objectMapper.writer().writeValueAsBytes(folderEntry)) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        FolderEntry created = response.as(FolderEntry.class);

        // search the previously created folderentry

        response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .queryParam("contentType", DataSet.class.getName()) //
                .get("/api/folders/entries");

        logger.info("search response: {}", response.asString());

        List<FolderEntry> folderEntries = objectMapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });

        Assertions.assertThat(folderEntries).isNotNull().isNotEmpty().hasSize(1).contains(created);

        String contentId = Long.toString(System.nanoTime());

        FolderEntry anOtherfolderEntry = new FolderEntry(DataSet.class.getName(), contentId, "/beer");

        response = RestAssured.given() //
                .body(objectMapper.writer().writeValueAsBytes(anOtherfolderEntry)) //
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/folders/entries");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);

        // search the previously created folderentries

        response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .queryParam("contentType", DataSet.class.getName()) //
                .get("/api/folders/entries");

        logger.info("search response: {}", response.asString());

        folderEntries = objectMapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });

        Assertions.assertThat(folderEntries).isNotNull() //
                .isNotEmpty() //
                .hasSize(2) //
                .contains(created, anOtherfolderEntry);

        // delete folder entry
        // "/api/folders/entries/{contentType}/{id}"
        // RequestParam path

        response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .pathParam("contentType", DataSet.class.getName()) //
                .pathParam("id", anOtherfolderEntry.getContentId()) //
                .delete("/api/folders/entries/{contentType}/{id}");

        logger.info("search response: {}", response.asString());

        response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .queryParam("contentType", DataSet.class.getName()) //
                .get("/api/folders/entries");

        folderEntries = objectMapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });

        Assertions.assertThat(folderEntries).isNotNull() //
                .isNotEmpty() //
                .hasSize(1) //
                .contains(created);

        response = RestAssured.given() //
                .queryParam("path", "beer") //
                .when() //
                .delete("/api/folders");

        // delete the folder
        response = RestAssured.given() //
                .queryParam("path", "/beer") //
                .queryParam("contentType", DataSet.class.getName()) //
                .get("/api/folders/entries");

        folderEntries = objectMapper.readValue(response.asString(), new TypeReference<List<FolderEntry>>() {
        });

        Assertions.assertThat(folderEntries).isNotNull().isEmpty();
    }


    //-----------------------------------
    // dataset list content
    //-----------------------------------

    @Test
    public void testDataSetListWithDateOrderWithinFolder() throws Exception {

        // create folders
        // create beer under foo
        Response response = RestAssured.given() //
            .queryParam("path", "foo/beer") //
            .when() //
            .put("/api/folders");

        response = RestAssured.given() //
            .queryParam("path", "foo/bar") //
            .when() //
            .put("/api/folders");

        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "cccc", "text/csv");

        FolderEntry folderEntry = new FolderEntry( "dataset", dataSetId1, "/foo");

        // create a folderentry in this directory
        response = RestAssured.given() //
            .body(mapper.writer().writeValueAsBytes(folderEntry)) //
            .contentType( ContentType.JSON) //
            .when() //
            .put("/api/folders/entries");

        folderEntry = new FolderEntry( "dataset", dataSetId3, "/foo");

        // create a folderentry in this directory
        response = RestAssured.given() //
            .body(mapper.writer().writeValueAsBytes(folderEntry)) //
            .contentType( ContentType.JSON) //
            .when() //
            .put("/api/folders/entries");

        // when (sort by date, order is desc)
        String list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "desc", "foo")  //
            .asString();

        // then
        FolderContent folderContent = mapper.readValue( list, FolderContent.class );

        String[] expectedIds = new String[] {dataSetId3, dataSetId1};
        int i = 0;
        Iterator<DataSetMetadata> iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }

        Assertions.assertThat( folderContent.getFolders() ).isNotNull() //
            .isNotEmpty().hasSize( 2 );

        // when (sort by date, order is desc)
        list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "asc", "foo") //
            .asString();

        // then
        folderContent = mapper.readValue( list, FolderContent.class );
        expectedIds = new String[] {dataSetId1, dataSetId3};
        i = 0;
        iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }


        Assertions.assertThat( folderContent.getFolders() ).isNotNull() //
            .isNotEmpty().hasSize( 2 );

        response = RestAssured.given() //
            .queryParam("path", "foo") //
            .when() //
            .delete("/api/folders");
    }

    @Test
    public void testDataSetListWithNameOrderWithinFolder() throws Exception {

        // create folders
        // create beer under foo
        Response response = RestAssured.given() //
            .queryParam("path", "foo/beer") //
            .when() //
            .put("/api/folders");

        response = RestAssured.given() //
            .queryParam("path", "foo/bar") //
            .when() //
            .put("/api/folders");

        final ObjectMapper mapper = new ObjectMapper();
        // given
        final String dataSetId1 = createDataset("dataset/dataset.csv", "aaaa", "text/csv");
        Thread.sleep(100);
        final String dataSetId2 = createDataset("dataset/dataset.csv", "bbbb", "text/csv");
        Thread.sleep(100);
        final String dataSetId3 = createDataset("dataset/dataset.csv", "cccc", "text/csv");

        FolderEntry folderEntry = new FolderEntry( "dataset", dataSetId1, "/foo");

        // create a folderentry in this directory
        response = RestAssured.given() //
            .body(mapper.writer().writeValueAsBytes(folderEntry)) //
            .contentType( ContentType.JSON) //
            .when() //
            .put("/api/folders/entries");

        folderEntry = new FolderEntry( "dataset", dataSetId3, "/foo");

        // create a folderentry in this directory
        response = RestAssured.given() //
            .body(mapper.writer().writeValueAsBytes(folderEntry)) //
            .contentType( ContentType.JSON) //
            .when() //
            .put("/api/folders/entries");

        // when (sort by date, order is desc)
        String list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "name", "desc", "foo") //
            .asString();

        // then
        FolderContent folderContent = mapper.readValue( list, FolderContent.class );
        String[] expectedIds = new String[] {dataSetId3, dataSetId1};
        int i = 0;
        Iterator<DataSetMetadata> iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }


        Assertions.assertThat( folderContent.getFolders() ).isNotNull() //
            .isNotEmpty().hasSize( 2 );

        // when (sort by date, order is desc)
        list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "asc", "foo").asString();

        // then
        folderContent = mapper.readValue( list, FolderContent.class );
        expectedIds = new String[] {dataSetId1, dataSetId3};
        i = 0;
        iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }


        Assertions.assertThat( folderContent.getFolders() ).isNotNull() //
            .isNotEmpty().hasSize( 2 );


        // test delete dataset
        response = when().delete("/api/datasets/" + dataSetId1);

        Assertions.assertThat( response.getStatusCode() ).isEqualTo( 200 );

        list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "asc", "foo").asString();

        // then
        folderContent = mapper.readValue( list, FolderContent.class );

        expectedIds = new String[] {dataSetId3};
        i = 0;
        iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }

        Assertions.assertThat( folderContent.getDatasets() ).isNotNull() //
            .isNotEmpty().hasSize( 1 );

        // rename folder foo to beer
        response =  RestAssured.given() //
            .queryParam( "path", "foo" ) //
            .queryParam( "newPath", "beer" ) //
            .put("/api/folders/rename");

        Assertions.assertThat( response.getStatusCode() ).isEqualTo( 200 );

        list = when() //
            .get("/api/folders/datasets?sort={sort}&order={order}&folder={folder}", "date", "asc", "beer").asString();

        // then
        folderContent = mapper.readValue( list, FolderContent.class );

        expectedIds = new String[] {dataSetId3};
        i = 0;
        iterator = folderContent.getDatasets().iterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getId(), is(expectedIds[i++]));
        }

        response = RestAssured.given() //
            .queryParam("path", "beer") //
            .when() //
            .delete("/api/folders");

        Assertions.assertThat( response.getStatusCode() ).isEqualTo( 200 );


    }

}
