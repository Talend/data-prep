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

package org.talend.dataprep.dataset.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.path.json.JsonPath.from;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.dataset.*;
import org.talend.dataprep.api.dataset.DataSetGovernance.Certification;
import org.talend.dataprep.api.dataset.location.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.schema.csv.CSVFormatFamily;
import org.talend.dataprep.security.Security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;

public class DataSetServiceTest extends DataSetBaseTest {

    private static final String T_SHIRT_100_CSV_EXPECTED_JSON = "../t-shirt_100.csv.expected.json";

    private static final String T_SHIRT_100_CSV = "../t-shirt_100.csv";

    private static final String US_STATES_TO_CLEAN_CSV = "../us_states_to_clean.csv";

    private static final String TAGADA2_CSV = "../tagada2.csv";

    private static final String TAGADA_CSV = "../tagada.csv";

    private static final String EMPTY_LINES2_JSON = "../empty_lines2.json";

    private static final String EMPTY_LINES2_CSV = "../empty_lines2.csv";

    private static final String METADATA_JSON = "../metadata.json";

    private static final String DATASET_WITH_NUL_CHAR_CSV = "../dataset_with_NUL_char.csv";

    /** DataPrep jackson ready to use builder. */
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Security security;

    @Autowired
    private VersionService versionService;

    @Test
    public void CORSHeaders() throws Exception {
        given().header("Origin", "fake.host.to.trigger.cors")
                .when()
                .get("/datasets").then().header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
    }


    @Test
    public void compatibleDatasetsList() throws Exception {
        when().get("/datasets/{id}/compatibledatasets", "1").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));

        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-19");
        String dataSetId2 = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-18");
        String dataSetId3 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-17");

        // when
        final String compatibleDatasetList = when().get("/datasets/{id}/compatibledatasets", dataSetId).asString();

        // then
        Assert.assertTrue(compatibleDatasetList.contains(dataSetId2));
        assertFalse(compatibleDatasetList.contains(dataSetId3));
    }

    @Test
    public void compatibleDatasetsListNameSort() throws Exception {
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-16");
        String dataSetId2 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-15");
        String dataSetId3 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-14");

        DataSetMetadata metadata1 = dataSetMetadataRepository.get(dataSetId);
        metadata1.setName("CCCC");
        dataSetMetadataRepository.add(metadata1);
        DataSetMetadata metadata2 = dataSetMetadataRepository.get(dataSetId2);
        metadata2.setName("BBBB");
        dataSetMetadataRepository.add(metadata2);
        DataSetMetadata metadata3 = dataSetMetadataRepository.get(dataSetId3);
        metadata3.setName("AAAA");
        dataSetMetadataRepository.add(metadata3);

        // when
        final String actual = when().get("/datasets/{id}/compatibledatasets?sort=name", dataSetId).asString();

        // Ensure order by name (most recent first)
        final Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"BBBB", "AAAA"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void compatibleDatasetsListDateSort() throws Exception {
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-13");
        String dataSetId2 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-12");
        String dataSetId3 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-11");

        DataSetMetadata metadata1 = dataSetMetadataRepository.get(dataSetId);
        metadata1.setName("CCCC");
        dataSetMetadataRepository.add(metadata1);
        DataSetMetadata metadata2 = dataSetMetadataRepository.get(dataSetId2);
        metadata2.setName("BBBB");
        dataSetMetadataRepository.add(metadata2);
        DataSetMetadata metadata3 = dataSetMetadataRepository.get(dataSetId3);
        metadata3.setName("AAAA");
        dataSetMetadataRepository.add(metadata3);

        // when
        final String actual = when().get("/datasets/{id}/compatibledatasets?sort=date", dataSetId).asString();

        // Ensure order by name (most recent first)
        final Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"AAAA", "BBBB"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void compatibleDatasetsListDateOrder() throws Exception {
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-10");
        String dataSetId2 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-9");
        String dataSetId3 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-8");

        DataSetMetadata metadata1 = dataSetMetadataRepository.get(dataSetId);
        metadata1.setName("CCCC");
        dataSetMetadataRepository.add(metadata1);
        DataSetMetadata metadata2 = dataSetMetadataRepository.get(dataSetId2);
        metadata2.setName("BBBB");
        dataSetMetadataRepository.add(metadata2);
        DataSetMetadata metadata3 = dataSetMetadataRepository.get(dataSetId3);
        metadata3.setName("AAAA");
        dataSetMetadataRepository.add(metadata3);

        // when
        final String actual = when().get("/datasets/{id}/compatibledatasets?sort=date&order=asc", dataSetId).asString();

        // Ensure order by name (most recent first)
        final Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"BBBB", "AAAA"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void compatibleDatasetsListNameOrder() throws Exception {
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-7");
        String dataSetId2 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-6");
        String dataSetId3 = createCSVDataSet(this.getClass().getResourceAsStream(TAGADA_CSV), "ds-5");

        DataSetMetadata metadata1 = dataSetMetadataRepository.get(dataSetId);
        metadata1.setName("CCCC");
        dataSetMetadataRepository.add(metadata1);
        DataSetMetadata metadata2 = dataSetMetadataRepository.get(dataSetId2);
        metadata2.setName("BBBB");
        dataSetMetadataRepository.add(metadata2);
        DataSetMetadata metadata3 = dataSetMetadataRepository.get(dataSetId3);
        metadata3.setName("AAAA");
        dataSetMetadataRepository.add(metadata3);

        // when
        final String actualASC = when().get("/datasets/{id}/compatibledatasets?sort=name&order=asc", dataSetId).asString();
        final String actualDESC = when().get("/datasets/{id}/compatibledatasets?sort=name&order=desc", dataSetId).asString();

        // Ensure order by name (most recent first)
        final Iterator<JsonNode> elements = mapper.readTree(actualASC).elements();
        String[] expectedNames = new String[]{"AAAA", "BBBB"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }

        mapper.readTree(actualDESC).elements();
        expectedNames = new String[]{"BBBB", "AAAA"};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void compatibleDatasetsListIllegalSort() throws Exception {
        when().get("/datasets/{id}/compatibledatasets?sort=aaaa", "0000").then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void compatibleDatasetsListIllegalOrder() throws Exception {
        when().get("/datasets/{id}/compatibledatasets?order=aaaa", "0000").then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void list() throws Exception {
        when().get("/datasets").then().statusCode(OK.value()).body(equalTo("[]"));
        // Adds 1 data set to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadataBuilder.metadata().id(id1).name("name1").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();

        metadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(metadata);

        String expected = "[{\"id\":\""
                + id1
                + "\",\"name\":\"name1\",\"records\":0,\"author\":\"anonymous\",\"nbLinesHeader\":0,\"nbLinesFooter\":0,\"created\":0}]";

        InputStream content = when().get("/datasets").asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs(expected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

        // Adds a new data set to store
        String id2 = UUID.randomUUID().toString();
        DataSetMetadata metadata2 = metadataBuilder.metadata().id(id2).name("name2").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        metadata2.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(metadata2);
        when().get("/datasets").then().statusCode(OK.value());
        String response = when().get("/datasets").asString();
        List<String> ids = from(response).get("id");
        assertThat(ids, hasItems(id1, id2));
        // check favorites
        List<Boolean> favoritesResp = from(response).get("favorite"); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertFalse(favoritesResp.get(0));
        assertFalse(favoritesResp.get(1));

        // add favorite
        UserData userData = new UserData(security.getUserId(), versionService.version().getVersionId());
        HashSet<String> favorites = new HashSet<>();
        favorites.add(id1);
        favorites.add(id2);
        userData.setFavoritesDatasets(favorites);
        userDataRepository.save(userData);

        favoritesResp = from(when().get("/datasets").asString()).get("favorite"); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertTrue(favoritesResp.get(0));
        assertTrue(favoritesResp.get(1));

    }

    @Test
    public void listNameSort() throws Exception {
        when().get("/datasets?sort=name").then().statusCode(OK.value()).body(equalTo("[]"));
        // Adds 2 data set metadata to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata1 = metadataBuilder.metadata().id(id1).name("AAAA").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata1);
        String id2 = UUID.randomUUID().toString();
        final DataSetMetadata metadata2 = metadataBuilder.metadata().id(id2).name("BBBB").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata2);
        // Ensure order by name (most recent first)
        String actual = when().get("/datasets?sort=name").asString();
        final Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[] { "BBBB", "AAAA" };
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void listDateSort() throws Exception {
        when().get("/datasets?sort=date").then().statusCode(OK.value()).body(equalTo("[]"));
        // Adds 2 data set metadata to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata1 = metadataBuilder.metadata().id(id1).name("AAAA").author("anonymous").created(20)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata1);
        String id2 = UUID.randomUUID().toString();
        final DataSetMetadata metadata2 = metadataBuilder.metadata().id(id2).name("BBBB").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata2);
        // Ensure order by date (most recent first)
        String actual = when().get("/datasets?sort=date").asString();
        final Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"AAAA", "BBBB"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void listDateOrder() throws Exception {
        when().get("/datasets?sort=date&order=asc").then().statusCode(OK.value()).body(equalTo("[]"));
        // Adds 2 data set metadata to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata1 = metadataBuilder.metadata().id(id1).name("AAAA").author("anonymous").created(20)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata1);
        String id2 = UUID.randomUUID().toString();
        final DataSetMetadata metadata2 = metadataBuilder.metadata().id(id2).name("BBBB").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata2);
        // Ensure order by date (most recent first)
        String actual = when().get("/datasets?sort=date&order=desc").asString();
        Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"AAAA", "BBBB"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
        // Ensure order by date (oldest first when no order value)
        actual = when().get("/datasets?sort=date").asString();
        elements = mapper.readTree(actual).elements();
        expectedNames = new String[]{"AAAA", "BBBB"};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
        // Ensure order by date (oldest first)
        actual = when().get("/datasets?sort=date&order=asc").asString();
        elements = mapper.readTree(actual).elements();
        expectedNames = new String[]{"BBBB", "AAAA"};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void listNameOrder() throws Exception {
        when().get("/datasets?sort=name&order=asc").then().statusCode(OK.value()).body(equalTo("[]"));
        // Adds 2 data set metadata to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata1 = metadataBuilder.metadata().id(id1).name("AAAA").author("anonymous").created(20)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata1);
        String id2 = UUID.randomUUID().toString();
        final DataSetMetadata metadata2 = metadataBuilder.metadata().id(id2).name("CCCC").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata2);
        String id3 = UUID.randomUUID().toString();
        final DataSetMetadata metadata3 = metadataBuilder.metadata().id(id3).name("bbbb").author("anonymous").created(0)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadataRepository.add(metadata3);
        // Ensure order by name (last character from alphabet first)
        String actual = when().get("/datasets?sort=name&order=desc").asString();
        Iterator<JsonNode> elements = mapper.readTree(actual).elements();
        String[] expectedNames = new String[]{"CCCC", "bbbb", "AAAA"};
        int i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
        // Ensure order by name (last character from alphabet first when no order value)
        actual = when().get("/datasets?sort=name").asString();
        elements = mapper.readTree(actual).elements();
        expectedNames = new String[]{"CCCC", "bbbb", "AAAA"};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
        // Ensure order by name (first character from alphabet first)
        actual = when().get("/datasets?sort=name&order=asc").asString();
        elements = mapper.readTree(actual).elements();
        expectedNames = new String[]{"AAAA", "bbbb", "CCCC"};
        i = 0;
        while (elements.hasNext()) {
            assertThat(elements.next().get("name").asText(), is(expectedNames[i++]));
        }
    }

    @Test
    public void listIllegalSort() throws Exception {
        when().get("/datasets?sort=aaaa").then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void listIllegalOrder() throws Exception {
        when().get("/datasets?order=aaaa").then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void create() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        // the next call may fail due to timing issues : TODO // make this synchronized somehow
        assertQueueMessages(dataSetId);
    }

    @Test
    public void cannotCreateWhenNameIsAlreadyUsed() throws Exception {
        // given
        String name = "youhou";
        createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), name);

        // when
        final Response response = given() //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(T_SHIRT_100_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .queryParam("name", name) //
                .when() //
                .expect().statusCode(409).log().ifError() //
                .post("/datasets");

        // then
        assertEquals(409, response.getStatusCode());
    }

    @Test
    public void shouldSearchDatasets() throws Exception {
        // given
        String ticId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "tic");
        String ticTacId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "tic tac");
        String ticTacTocId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "tic tac toc");
        String tutuId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "tutu");

        // when / then
        checkSearchResult("toto", emptyList());
        checkSearchResult("tic", asList(ticId, ticTacId, ticTacTocId));
        checkSearchResult("tac", asList(ticTacId, ticTacTocId));
        checkSearchResult("toc", asList(ticTacTocId));
    }

    private void checkSearchResult(String search, List<String> expectedIds) throws IOException {
        final Response response = given() //
                .queryParam("name", search) //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .get("/datasets/search");

        // then
        assertEquals(200, response.getStatusCode());
        final List<DataSetMetadata> metadataList = mapper.readValue(response.asString(), new TypeReference<List<DataSetMetadata>>() {});
        assertEquals(expectedIds.size(), metadataList.size());
        assertEquals(expectedIds.size(), metadataList.stream().filter(m -> expectedIds.contains(m.getId())).count());

    }

    @Test
    public void createEmptyLines() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(EMPTY_LINES2_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        final String content = when().get("/datasets/{id}/content", dataSetId).asString();
        assertThat(content, sameJSONAsFile(this.getClass().getResourceAsStream(EMPTY_LINES2_JSON)));
    }

    @Test
    public void get() throws Exception {

        String expectedId = insertEmptyDataSet();

        List<String> ids = from(when().get("/datasets").asString()).get("");
        assertThat(ids.size(), is(1));
        int statusCode = when().get("/datasets/{id}/content", expectedId).getStatusCode();
        assertTrue("statusCode is:" + statusCode, statusCode == HttpStatus.ACCEPTED.value() || statusCode == OK.value());
    }

    @Test
    public void getNotExistingDataset() throws Exception {
        int statusCode = when().get("/datasets/1234/content").getStatusCode();
        assertTrue("statusCode is:" + statusCode, statusCode == HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testFavorite() {
        // given
        final String datasetId = UUID.randomUUID().toString();
        final DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id(datasetId)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);
        contentStore.storeAsRaw(dataSetMetadata, new ByteArrayInputStream(new byte[0]));

        final UserData userData = new UserData(security.getUserId(), versionService.version().getVersionId());
        userDataRepository.save(userData);
        final Set<String> favorites = new HashSet<>();
        favorites.add(datasetId);

        boolean isFavorite = from(when().get("/datasets/{id}/content", datasetId).asString()).get("metadata.favorite");
        assertFalse(isFavorite);

        // when
        userData.setFavoritesDatasets(favorites);
        userDataRepository.save(userData);

        // then
        isFavorite = from(when().get("/datasets/{id}/content", datasetId).asString()).get("metadata.favorite");
        assertTrue(isFavorite);
    }

    @Test
    public void shouldCopy() throws Exception {
        // given
        String originalId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "original");

        // when
        final Response response = given() //
                .queryParam("copyName", "copy") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .post("/datasets/{id}/copy", originalId);

        // then
        assertEquals(200, response.getStatusCode());
        final String copyId = response.asString();
        final DataSetMetadata copy = dataSetMetadataRepository.get(copyId);
        assertNotNull(copy);
        assertEquals(9, copy.getRowMetadata().size());
    }

    @Test
    public void copyNothingShouldReturnNothing() throws Exception {
       // when
        final Response response = given() //
                .queryParam("copyName", "copy") //
                .when()//
                .expect().statusCode(200).log().ifError() //
                .post("/datasets/{id}/copy", "unknown_dataset");

        // then
        assertEquals(200, response.getStatusCode());
        assertTrue(response.asString().length() == 0);
    }

    @Test
    public void cannotCopyIfTheNameIsAlreadyUsed() throws Exception {
        // given
        String name = "taken";
        String originalId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), name);

        // when
        final Response response = given() //
                .queryParam("copyName", name) //
                .when()//
                .expect().statusCode(409).log().ifError() //
                .post("/datasets/{id}/copy", originalId);

        // then
        assertEquals(409, response.getStatusCode());
    }

    @Test
    public void sample() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-6");
        // when
        String sample = requestDataSetSample(dataSetId, true, "17");
        // then
        assertEquals(17, getNumberOfRecords(sample));
    }

    @Test
    public void sampleShouldUpdateStatistics() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-5");
        // when
        String sample = requestDataSetSample(dataSetId, true, "16");
        // then
        InputStream expected = this.getClass().getResourceAsStream("../t-shirt_100.csv_sample_16.expected.json");
        assertThat(sample, sameJSONAsFile(expected));
    }

    @Test
    public void sampleWithNegativeSize() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-4");
        // when
        String sample = requestDataSetSample(dataSetId, true, "-1");
        // then
        assertEquals(100, getNumberOfRecords(sample));
    }

    @Test
    public void sampleWithSizeIsZero() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), UUID.randomUUID().toString());
        // when
        String sample = requestDataSetSample(dataSetId, true, "0");
        // then
        assertEquals(100, getNumberOfRecords(sample));
    }

    @Test(expected = java.lang.AssertionError.class)
    public void sampleWithDecimalSize() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-2");
        // when
        requestDataSetSample(dataSetId, true, "10.5");
        // then expect error (400 bad request)
    }

    @Test(expected = java.lang.AssertionError.class)
    public void sampleWithBadContent() throws Exception {
        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(T_SHIRT_100_CSV), "ds-1");
        // when
        requestDataSetSample(dataSetId, true, "ghqmkdhjsgf");
        // then expect error (400 bad request)
    }

    @Test
    public void delete() throws Exception {
        String expectedId = UUID.randomUUID().toString();

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id(expectedId)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();

        dataSetMetadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);

        List<String> ids = from(when().get("/datasets").asString()).get("");
        assertThat(ids.size(), is(1));
        int before = dataSetMetadataRepository.size();
        when().delete("/datasets/{id}", expectedId).then().statusCode(OK.value());
        int after = dataSetMetadataRepository.size();
        logger.debug("delete before {} after {}", before, after);
        assertThat(before - after, is(1));
    }

    @Test
    public void updateRawContent() throws Exception {
        String dataSetId = "123456";
        given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV))).when()
                .put("/datasets/{id}/raw", dataSetId).then().statusCode(OK.value());
        List<String> ids = from(when().get("/datasets").asString()).get("id");
        assertThat(ids, hasItem(dataSetId));
        assertQueueMessages(dataSetId);
    }

    @Test
    public void updateRawContent_should_preserve_non_content_related_metadata_except_last_modification_date() throws Exception {
        //given
        final String dataSetId = "123456";
        given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .when()
                .put("/datasets/{id}/raw", dataSetId)
                .then()
                .statusCode(OK.value());

        String datasets = when().get("/datasets").asString();
        List<DataSetMetadata> datasetsMetadata = mapper.readValue(datasets, new TypeReference<ArrayList<DataSetMetadata>>() {});
        final DataSetMetadata original = datasetsMetadata.get(0);

        //when
        given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA2_CSV)))
                .when()
                .put("/datasets/{id}/raw", dataSetId)
                .then()
                .statusCode(OK.value());

        //then
        datasets = when().get("/datasets").asString();
        datasetsMetadata = mapper.readValue(datasets, new TypeReference<ArrayList<DataSetMetadata>>() {});
        final DataSetMetadata copy = datasetsMetadata.get(0);

        assertThat(copy.getId(), equalTo(original.getId()));
        assertThat(copy.getAppVersion(), equalTo(original.getAppVersion()));
        assertThat(copy.getAuthor(), equalTo(original.getAuthor()));
        assertThat(copy.getCreationDate(), equalTo(original.getCreationDate()));
        assertThat(copy.isFavorite(), equalTo(original.isFavorite()));
        assertThat(copy.getLocation(), equalTo(original.getLocation()));
    }

    @Test
    public void updateMetadataContentWithWrongDatasetId() throws Exception {
        assertThat(dataSetMetadataRepository.get("3d72677c-e2c9-4a34-8c58-959a56ec8643"), nullValue());
        given().contentType(JSON) //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(METADATA_JSON))) //
                .when() //
                .put("/datasets/{id}", "3d72677c-e2c9-4a34-8c58-959a56ec8643") //
                .then() //
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void previewNonDraft() throws Exception {
        // Create a data set
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        dataSetMetadata.setDraft(false); // Ensure it is no draft
        dataSetMetadataRepository.add(dataSetMetadata);
        // Should receive a 301 that redirects to the GET data set content operation
        given().redirects().follow(false).contentType(JSON).get("/datasets/{id}/preview", dataSetId) //
                .then() //
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value());
        // Should receive a 200 if code follows redirection
        given().redirects().follow(true).contentType(JSON).get("/datasets/{id}/preview", dataSetId) //
                .then() //
                .statusCode(OK.value());
    }

    @Test
    public void previewMissingMetadata() throws Exception {
        // Data set 1234 does not exist, should get empty content response.
        given().get("/datasets/{id}/preview", "1234") //
                .then() //
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void preview_multi_sheet_with_a_sheet_name() throws Exception {

        String dataSetId = createXlsDataSet(this.getClass().getResourceAsStream("../Talend_Desk-Tableau_de_Bord-011214.xls"));

        String json = given().contentType(JSON).get("/datasets/{id}/preview?sheetName=Leads", dataSetId).asString();
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(json);

        Assertions.assertThat(dataSet.getMetadata().getRowMetadata().getColumns()).isNotNull().isNotEmpty().hasSize(21);

        json = given().contentType(JSON).get("/datasets/{id}/preview?sheetName=Tableau de bord", dataSetId)
                .asString();

        dataSet = mapper.readerFor(DataSet.class).readValue(json);

        Assertions.assertThat(dataSet.getMetadata().getRowMetadata().getColumns()).isNotNull().isNotEmpty().hasSize(10);

    }

    @Test
    public void should_get_content_from_semi_colon_csv() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();

        // then
        final String contentAsString = IOUtils.toString(content);
        final InputStream expected = this.getClass().getResourceAsStream("../content/test1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void should_get_content_from_coma_csv() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA2_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();

        // then
        final String contentAsString = IOUtils.toString(content);
        final InputStream expected = this.getClass().getResourceAsStream("../content/test1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void should_get_content_from_updated_dataset() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // given: update content
        given().body(IOUtils.toString(this.getClass().getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw");
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();
        final String contentAsString = IOUtils.toString(content);

        // then
        final InputStream expected = this.getClass().getResourceAsStream("../content/test2.json");
        assertThat(contentAsString, sameJSONAsFile(expected));

        // Update name
        String expectedName = "testOfADataSetName";
        given().body(IOUtils.toString(this.getClass().getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw?name=" + expectedName);
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertThat(dataSetMetadata.getName(), is(expectedName));
    }

    @Test
    public void should_update_dataset_name() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // when
        final String expectedName = "testOfADataSetName";
        given().body(IOUtils.toString(this.getClass().getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw?name=" + expectedName);

        // then
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertThat(dataSetMetadata.getName(), is(expectedName));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1066
     */
    @Test
    public void shouldUpdateSeparatorWithHeader() throws Exception {

        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream("../avengers.psv"), "tpd-1066");
        InputStream metadataInput = when().get("/datasets/{id}/metadata", dataSetId).asInputStream();
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(metadataInput);
        DataSetMetadata metadata = dataSet.getMetadata();

        // when
        final Map<String, String> parameters = metadata.getContent().getParameters();
        parameters.put(CSVFormatFamily.SEPARATOR_PARAMETER, "|");
        parameters.remove(CSVFormatFamily.HEADER_COLUMNS_PARAMETER);
        final int statusCode = given() //
                .contentType(JSON) //
                .body(mapper.writer().writeValueAsString(metadata)) //
                .expect().statusCode(200).log().ifError() //
                .when().put("/datasets/{id}", dataSetId).getStatusCode();

        assertThat(statusCode, is(200));
        assertQueueMessages(dataSetId);

        // then
        InputStream expected = this.getClass().getResourceAsStream("../avengers_expected.json");
        String datasetContent = given().when().get("/datasets/{id}/content?metadata=true", dataSetId).asString();

        assertThat(datasetContent, sameJSONAsFile(expected));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1066
     */
    @Test
    public void shouldUpdateSeparatorWithoutHeader() throws Exception {

        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream("../tdp-1066_no_header.ssv"), "tdp-1066-2");
        InputStream metadataInput = when().get("/datasets/{id}/metadata", dataSetId).asInputStream();
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(metadataInput);
        DataSetMetadata metadata = dataSet.getMetadata();

        // then
        assertThat(metadata.getRowMetadata().getColumns().size(), is(2)); // ';' is guessed as separator ==> 2 columns

        // when
        final Map<String, String> parameters = metadata.getContent().getParameters();
        parameters.put(CSVFormatFamily.SEPARATOR_PARAMETER, " ");
        parameters.remove(CSVFormatFamily.HEADER_COLUMNS_PARAMETER);
        final int statusCode = given() //
                .contentType(JSON) //
                .body(mapper.writer().writeValueAsString(metadata)) //
                .expect().statusCode(200).log().ifError() //
                .when().put("/datasets/{id}", dataSetId).getStatusCode();

        assertThat(statusCode, is(200));
        assertQueueMessages(dataSetId);

        // then
        InputStream datasetContent = given().when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        final DataSet actual = mapper.readerFor(DataSet.class).readValue(datasetContent);
        final DataSetMetadata actualMetadata = actual.getMetadata();

        assertThat(actualMetadata.getRowMetadata().getColumns().size(), is(10)); // with ' ' as separator ==> 10 columns
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1066
     */
    @Test
    public void shouldUpdateLimit() throws Exception {

        // given
        String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream("../avengers.csv"), "tdp-1066-3");
        InputStream metadataInput = when().get("/datasets/{id}/metadata", dataSetId).asInputStream();
        DataSet dataSet = mapper.readerFor(DataSet.class).readValue(metadataInput);
        DataSetMetadata metadata = dataSet.getMetadata();

        // when
        metadata.getContent().setLimit(2L);
        final int statusCode = given() //
                .contentType(JSON) //
                .body(mapper.writer().writeValueAsString(metadata)) //
                .expect().statusCode(200).log().ifError() //
                .when().put("/datasets/{id}", dataSetId).getStatusCode();

        assertThat(statusCode, is(200));
        assertQueueMessages(dataSetId);

        // then
        InputStream expected = this.getClass().getResourceAsStream("../avengers_expected_limit_2.json");
        String datasetContent = given().when().get("/datasets/{id}/content?metadata=true", dataSetId).asString();

        assertThat(datasetContent, sameJSONAsFile(expected));
    }

    /**
     * Test the import of a csv file with a really low separator coefficient variation.
     *
     * @see CSVFormatGuesser
     */
    @Test
    public void testLowSeparatorOccurrencesInCSV() throws Exception {

        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream("../avengers.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();

        assertQueueMessages(dataSetId);

        InputStream expected = this.getClass().getResourceAsStream("../avengers_expected.json");
        String datasetContent = given().when().get("/datasets/{id}/content?metadata=true", dataSetId).asString();

        assertThat(datasetContent, sameJSONAsFile(expected));
    }

    /**
     * Test the import of an excel file that is also detected as csv file. See
     * https://jira.talendforge.org/browse/TDP-258
     *
     * @see CSVFormatGuesser
     */
    @Test
    public void testXlsFileThatIsAlsoParsedAsCSV() throws Exception {

        String dataSetId = given()
                .body(IOUtils.toByteArray(this.getClass().getResourceAsStream("../TDP-375_xsl_read_as_csv.xls")))
                .when().post("/datasets").asString();

        assertQueueMessages(dataSetId);

        String json = given().when().get("/datasets/{id}/metadata", dataSetId).asString();
        final JsonNode rootNode = mapper.reader().readTree(json);
        final JsonNode metadata = rootNode.get("metadata");

        // only interested in the parser --> excel parser must be used !
        assertEquals(metadata.get("type").asText(), "application/vnd.ms-excel");
        assertEquals(metadata.get("formatGuess").asText(), "formatGuess#xls");
        assertEquals(metadata.get("records").asText(), "500");
    }

    @Test
    public void testQuotes() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream("../bands_quotes.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = this.getClass().getResourceAsStream("../test_quotes.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void testQuotesAndCarriageReturn() throws Exception {
        String dataSetId = given()
                .body(IOUtils.toString(this.getClass().getResourceAsStream("../bands_quotes_and_carriage_return.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = this.getClass().getResourceAsStream("../test_quotes_and_carriage_return.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-71
     */
    @Test
    public void empty_lines_and_missing_values() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(US_STATES_TO_CLEAN_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = this.getClass().getResourceAsStream("../us_states_to_clean.csv_expected.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void nbLines() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs("{\"metadata\":{\"records\":2,\"nbLinesHeader\":1,\"nbLinesFooter\":0}}")
                .allowingExtraUnexpectedFields().allowingAnyArrayOrdering());
    }

    @Test
    public void nbLines2() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(T_SHIRT_100_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = this.getClass().getResourceAsStream(T_SHIRT_100_CSV_EXPECTED_JSON);
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void nbLinesUpdate() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs("{\"metadata\":{\"records\":2,\"nbLinesHeader\":1,\"nbLinesFooter\":0}}")
                .allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

        given().body(IOUtils.toString(this.getClass().getResourceAsStream(T_SHIRT_100_CSV)))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/{id}/raw", dataSetId).asString();

        assertQueueMessages(dataSetId);

        content = when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        contentAsString = IOUtils.toString(content);

        InputStream expected = this.getClass().getResourceAsStream(T_SHIRT_100_CSV_EXPECTED_JSON);
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void getMetadata() throws Exception {
        DataSetMetadataBuilder builder = metadataBuilder.metadata().id("1234");
        builder.row(ColumnMetadata.Builder//
                .column()//
                .id(1234)//
                .name("id")//
                .empty(0)//
                .invalid(0)//
                .valid(0)//
                .type(Type.STRING))//
                .created(0)//
                .name("name")//
                .author("author")//
                .footerSize(0) //
                .headerSize(1) //
                .qualityAnalyzed(true) //
                .schemaAnalyzed(true) //
                .formatGuessId(new CSVFormatFamily().getBeanId()) //
                .mediaType("text/csv");

        DataSetMetadata metadata = builder.build();
        metadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");

        dataSetMetadataRepository.add(metadata);
        String contentAsString = when().get("/datasets/{id}/metadata", "1234").asString();
        InputStream expected = this.getClass().getResourceAsStream("../metadata1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));

        Boolean isFavorites = from(contentAsString).get("metadata.favorite");
        assertFalse(isFavorites);

        // add favorite
        UserData userData = new UserData(security.getUserId(), versionService.version().getVersionId());
        HashSet<String> favorites = new HashSet<>();
        favorites.add("1234");
        userData.setFavoritesDatasets(favorites);
        userDataRepository.save(userData);

        contentAsString = when().get("/datasets/{id}/metadata", "1234").asString();
        isFavorites = from(contentAsString).get("metadata.favorite");
        assertTrue(isFavorites);

    }

    @Test
    public void getEmptyMetadata() throws Exception {
        DataSetMetadata metadata = dataSetMetadataRepository.get("9876");
        assertNull(metadata);
        int statusCode = when().get("/datasets/{id}/metadata", "9876").statusCode();

        assertThat(statusCode, is(HttpStatus.NO_CONTENT.value()));

    }

    /**
     * Check that the error listing service returns a list parsable of error codes. The content is not checked
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void shouldListErrors() throws Exception {
        String errors = when().get("/datasets/errors").asString();

        JsonNode rootNode = mapper.readTree(errors);

        assertTrue(rootNode.isArray());
        assertTrue(rootNode.size() > 0);
        for (final JsonNode errorCode : rootNode) {
            assertTrue(errorCode.has("code"));
            assertTrue(errorCode.has("http-status-code"));
        }
    }

    @Test
    public void testAskCertification() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertEquals(Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(OK.value());
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertEquals(Certification.PENDING, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRowMetadata().getColumns(), not(empty()));
    }

    @Test
    public void testCertify() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        long originalNbLines = dataSetMetadata.getContent().getNbRecords(); // to check later if no modified
        assertEquals(Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        // NONE -> PENDING
        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(OK.value());
        // PENDING -> CERTIFIED
        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(OK.value());
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertEquals(Certification.CERTIFIED, dataSetMetadata.getGovernance().getCertificationStep());
        assertEquals(originalNbLines, dataSetMetadata.getContent().getNbRecords());
    }

    @Test
    public void testGetFavoritesDatasetList() {
        when().get("/datasets/favorites").then().statusCode(OK.value()).body(equalTo("[]"));
        String dsId1 = UUID.randomUUID().toString();
        String dsId2 = UUID.randomUUID().toString();
        UserData userData = new UserData(security.getUserId(), versionService.version().getVersionId());
        HashSet<String> favorites = new HashSet<>();
        favorites.add(dsId1);
        favorites.add(dsId2);
        userData.setFavoritesDatasets(favorites);
        userDataRepository.save(userData);
        List<String> favoritesResp = from(when().get("/datasets/favorites").asString()).get();
        assertEquals(2, favoritesResp.size());
        assertThat(favoritesResp, hasItems(dsId1, dsId2));
    }

    @Test
    public void testSetUnsetFavoriteDataSet() throws IOException {
        String dsId1 = UUID.randomUUID().toString();
        String dsId2 = UUID.randomUUID().toString();

        when().get("/datasets/favorites").then().statusCode(OK.value()).body(equalTo("[]"));
        dataSetMetadataRepository.add(metadataBuilder.metadata().id(dsId1).build());
        dataSetMetadataRepository.add(metadataBuilder.metadata().id(dsId2).build());
        // check set
        when().put("/datasets/{id}/favorite", dsId1).then().statusCode(OK.value());
        when().put("/datasets/{id}/favorite?unset=false", dsId2).then().statusCode(OK.value());
        List<String> favoritesResp = from(when().get("/datasets/favorites").asString()).get(); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertThat(favoritesResp, hasItems(dsId1, dsId2));
        // check unset
        when().put("/datasets/{id}/favorite?unset=true", dsId2).then().statusCode(OK.value());
        favoritesResp = from(when().get("/datasets/favorites").asString()).get();
        assertEquals(1, favoritesResp.size());
        assertThat(favoritesResp, hasItem(dsId1));
        // check wrong datasetId
        String wrongDsId = UUID.randomUUID().toString();
        assertThat(dataSetMetadataRepository.get(wrongDsId), nullValue());
        given().contentType(JSON) //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(METADATA_JSON))) //
                .when() //
                .put("/datasets/{id}/favorite", wrongDsId) //
                .then() //
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    public void testFavoritesTransientNotStored() {
        String expectedDsId = UUID.randomUUID().toString();

        DataSetMetadata dataSetMetadataToBeSet = metadataBuilder.metadata().id(expectedDsId).build();
        dataSetMetadataToBeSet.setFavorite(true);
        dataSetMetadataRepository.add(dataSetMetadataToBeSet);
        DataSetMetadata dataSetMetadataGet = dataSetMetadataRepository.get(expectedDsId);
        assertNotNull(dataSetMetadataGet);
        assertFalse(dataSetMetadataGet.isFavorite());
    }

    @Test
    public void updateDatasetColumn_should_update_domain() throws Exception {
        // given
        final String dataSetId = given() //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        final ColumnMetadata column;

        // update the metadata in the repository (lock mechanism is needed otherwise semantic domain will be erased by
        // analysis)
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        DataSetMetadata dataSetMetadata;
        RowMetadata row;
        lock.lock();
        try {
            dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
            assertNotNull(dataSetMetadata);
            row = dataSetMetadata.getRowMetadata();
            assertNotNull(row);
            column = row.getById("0002");
            final SemanticDomain jsoDomain = new SemanticDomain("JSO", "JSO label", 1.0F);
            column.getSemanticDomains().add(jsoDomain);
            dataSetMetadataRepository.add(dataSetMetadata);
        } finally {
            lock.unlock();
        }

        assertThat(column.getDomain(), is("FIRST_NAME"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(100.0F));

        // when
        final Response res = given() //
                .body("{\"domain\": \"JSO\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        // then
        res.then().statusCode(200);
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        row = dataSetMetadata.getRowMetadata();
        assertNotNull(row);
        final ColumnMetadata actual = row.getById("0002");
        assertThat(actual.getDomain(), is("JSO"));
        assertThat(actual.getDomainLabel(), is("JSO label"));
        assertThat(actual.getDomainFrequency(), is(1.0F));
    }

    @Test
    public void updateDatasetColumn_should_update_type() throws Exception {
        // given
        final String dataSetId = given() //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        Assert.assertNotNull(dataSetMetadata);
        RowMetadata row = dataSetMetadata.getRowMetadata();
        assertNotNull(row);
        final ColumnMetadata column = row.getById("0002");

        assertThat(column.getDomain(), is("FIRST_NAME"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(100.0F));
        assertThat(column.getType(), is("string"));

        // when
        final Response res = given() //
                .body("{\"type\": \"integer\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        // then
        res.then().statusCode(200);
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        Assert.assertNotNull(dataSetMetadata);
        row = dataSetMetadata.getRowMetadata();
        assertNotNull(row);
        final ColumnMetadata actual = row.getById("0002");
        assertThat(actual.getDomain(), is("FIRST_NAME"));
        assertThat(actual.getDomainLabel(), is("First Name"));
        assertThat(actual.getDomainFrequency(), is(100.0F));
        assertThat(actual.getType(), is("integer"));
    }

    @Test
    public void updateDatasetColumn_should_clear_domain() throws Exception {
        // given
        final String dataSetId = given() //
                .body(IOUtils.toString(this.getClass().getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        RowMetadata row = dataSetMetadata.getRowMetadata();
        assertNotNull(row);
        final ColumnMetadata column = row.getById("0002");

        assertThat(column.getDomain(), is("FIRST_NAME"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(100.0F));

        // when
        final Response res = given() //
                .body("{\"domain\": \"\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        // then
        res.then().statusCode(200);
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        row = dataSetMetadata.getRowMetadata();
        assertNotNull(row);
        final ColumnMetadata actual = row.getById("0002");
        assertThat(actual.getDomain(), is(""));
        assertThat(actual.getDomainLabel(), is(""));
        assertThat(actual.getDomainFrequency(), is(0.0F));
    }

    @Test
    public void datePattern() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream("../date_time_pattern.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        final ColumnMetadata column = dataSetMetadata.getRowMetadata().getById("0001");

        assertThat(column.getType(), is("date"));
        assertThat(column.getDomain(), is(""));
        final Statistics statistics = mapper.readerFor(Statistics.class)
                .readValue(this.getClass().getResourceAsStream("../date_time_pattern_expected.json"));
        assertThat(column.getStatistics(), CoreMatchers.equalTo(statistics));
    }

    @Test
    public void should_remove_any_NUL_character() throws Exception {
        // given
        final String originalContent = IOUtils.toString(this.getClass().getResourceAsStream(DATASET_WITH_NUL_CHAR_CSV));
        assertThat(originalContent.chars().anyMatch((c) -> c == '\u0000'), is(true));
        final String dataSetId = createCSVDataSet(this.getClass().getResourceAsStream(DATASET_WITH_NUL_CHAR_CSV), "test");

        // when
        final String content = requestDataSetSample(dataSetId, false, "10");

        // then
        assertThat(content, not(containsString("\\u0000")));
    }

    @Test
    public void invalid_us_states() throws Exception {
        String dataSetId = given().body(IOUtils.toString(this.getClass().getResourceAsStream("../invalid_us_states.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        final DataSet dataset = mapper.readerFor(DataSet.class).readValue(contentAsString);
        assertThat(dataset, is(notNullValue()));
        assertThat(dataset.getMetadata().getRowMetadata().getColumns().isEmpty(), is(false));

        final ColumnMetadata column = dataset.getMetadata().getRowMetadata().getColumns().get(0);
        assertThat(column.getDomain(), is("US_STATE_CODE")); // us state code
        assertThat(column.getQuality().getInvalid(), is(2)); // 2 invalid values
    }

    @Test
    public void listEncodings() throws Exception {
        InputStream content = when().get("/datasets/encodings").asInputStream();
        final String contentAsString = IOUtils.toString(content);
        assertThat(contentAsString, not(isEmptyString()));
    }

    @Test
    public void listImports() throws Exception {
        // Given
        InputStream content = when().get("/datasets/imports").asInputStream();
        final String contentAsString = IOUtils.toString(content);

        // Then
        assertThat(contentAsString, not(is("[]"))); // There should be some exports available
    }

    private String insertEmptyDataSet() {
        String datasetId = UUID.randomUUID().toString();
        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id(datasetId)
                .formatGuessId(new CSVFormatFamily().getBeanId()).build();
        dataSetMetadata.getContent().addParameter(CSVFormatFamily.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);
        contentStore.storeAsRaw(dataSetMetadata, new ByteArrayInputStream(new byte[0]));
        return datasetId;
    }

    private String createCSVDataSet(InputStream content, String name) throws Exception {
        String dataSetId = given() //
                .body(IOUtils.toString(content)) //
                .queryParam("Content-Type", "text/csv") //
                .queryParam("name", name) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/datasets") //
                .asString();
        assertQueueMessages(dataSetId);
        return dataSetId;
    }

    private String createXlsDataSet(InputStream content) throws Exception {
        String dataSetId = given().body(IOUtils.toByteArray(content)).when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        return dataSetId;
    }

    private String requestDataSetSample(String dataSetId, boolean withMetadata, String sampleSize) {
        return given() //
                .expect() //
                .statusCode(200) //
                .when() //
                .get("/datasets/{id}/content?metadata={withMetadata}&sample={sampleSize}", dataSetId, withMetadata, sampleSize) //
                .asString();

    }

    private long getNumberOfRecords(String json) throws IOException {
        JsonNode rootNode = mapper.readTree(json);
        JsonNode records = rootNode.get("records");
        return records.size();
    }
}
