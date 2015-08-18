package org.talend.dataprep.dataset.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.path.json.JsonPath.from;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetGovernance.Certification;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.location.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.schema.CSVFormatGuess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

public class DataSetServiceTests extends DataSetBaseTest {

    private static final String T_SHIRT_100_CSV_EXPECTED_JSON = "../t-shirt_100.csv.expected.json";

    private static final String T_SHIRT_100_CSV = "../t-shirt_100.csv";

    private static final String US_STATES_TO_CLEAN_CSV = "../us_states_to_clean.csv";

    private static final String TAGADA2_CSV = "../tagada2.csv";

    private static final String TAGADA_CSV = "../tagada.csv";

    private static final String EMPTY_LINES2_JSON = "../empty_lines2.json";

    private static final String EMPTY_LINES2_CSV = "../empty_lines2.csv";

    private static final String METADATA_JSON = "../metadata.json";

    @Test
    public void CORSHeaders() throws Exception {
        when().get("/datasets").then().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT")
                .header("Access-Control-Max-Age", "3600")
                .header("Access-Control-Allow-Headers", "x-requested-with, Content-Type");
    }

    @Test
    public void list() throws Exception {
        when().get("/datasets").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));
        // Adds 1 data set to store
        String id1 = UUID.randomUUID().toString();
        final DataSetMetadata metadata = metadata().id(id1).name("name1").author("anonymous").created(0)
                .formatGuessId(new CSVFormatGuess().getBeanId()).build();

        metadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(metadata);

        String expected = "[{\"id\":\"" + id1
                + "\",\"name\":\"name1\",\"records\":0,\"author\":\"anonymous\",\"nbLinesHeader\":0,\"nbLinesFooter\":0,\"created\":0}]";

        InputStream content = when().get("/datasets").asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs(expected).allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

        // Adds a new data set to store
        String id2 = UUID.randomUUID().toString();
        DataSetMetadata metadata2 = metadata().id(id2).name("name2").author("anonymous").created(0)
                .formatGuessId(new CSVFormatGuess().getBeanId()).build();
        metadata2.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(metadata2);
        when().get("/datasets").then().statusCode(HttpStatus.OK.value());
        String response = when().get("/datasets").asString();
        List<String> ids = from(response).get("id");
        assertThat(ids, hasItems(id1, id2));
        // check favorites
        List<Boolean> favoritesResp = from(response).get("favorite"); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertFalse(favoritesResp.get(0));
        assertFalse(favoritesResp.get(1));

        // add favorite
        UserData userData = new UserData("anonymousUser");
        HashSet<String> favorites = new HashSet<>();
        favorites.add(id1);
        favorites.add(id2);
        userData.setFavoritesDatasets(favorites);
        userDataRepository.setUserData(userData);

        favoritesResp = from(when().get("/datasets").asString()).get("favorite"); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertTrue(favoritesResp.get(0));
        assertTrue(favoritesResp.get(1));

    }

    @Test
    public void create() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        // the next call may fail due to timing issues : TODO // make this synchronized somehow
        assertQueueMessages(dataSetId);
    }

    @Test
    public void createEmptyLines() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(EMPTY_LINES2_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        final String content = when().get("/datasets/{id}/content", dataSetId).asString();
        assertThat(content, sameJSONAsFile(DataSetServiceTests.class.getResourceAsStream(EMPTY_LINES2_JSON)));
    }

    @Test
    public void get() throws Exception {

        String expectedId = insertEmptyDataSet();

        List<String> ids = from(when().get("/datasets").asString()).get("");
        assertThat(ids.size(), is(1));
        int statusCode = when().get("/datasets/{id}/content", expectedId).getStatusCode();
        assertTrue("statusCode is:" + statusCode,
                statusCode == HttpStatus.ACCEPTED.value() || statusCode == HttpStatus.OK.value());
    }

    @Test
    public void testFavorite() {
        // given
        final String datasetId = UUID.randomUUID().toString();
        final DataSetMetadata dataSetMetadata = metadata().id(datasetId).formatGuessId(new CSVFormatGuess().getBeanId()).build();
        dataSetMetadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);
        contentStore.storeAsRaw(dataSetMetadata, new ByteArrayInputStream(new byte[0]));

        final UserData userData = new UserData("anonymousUser");
        userDataRepository.setUserData(userData);
        final Set<String> favorites = new HashSet<>();
        favorites.add(datasetId);

        boolean isFavorite = from(when().get("/datasets/{id}/content", datasetId).asString()).get("metadata.favorite");
        assertFalse(isFavorite);

        // when
        userData.setFavoritesDatasets(favorites);
        userDataRepository.setUserData(userData);

        // then
        isFavorite = from(when().get("/datasets/{id}/content", datasetId).asString()).get("metadata.favorite");
        assertTrue(isFavorite);
    }

    @Test
    public void sample() throws Exception {
        // given
        String dataSetId = createCSVDataSet(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV));
        // when
        String sample = requestDataSetSample(dataSetId, "17");
        // then
        assertEquals(17, getNumberOfRecords(sample));
    }

    @Test
    public void sampleWithNegativeSize() throws Exception {
        // given
        String dataSetId = createCSVDataSet(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV));
        // when
        String sample = requestDataSetSample(dataSetId, "-1");
        // then
        assertEquals(100, getNumberOfRecords(sample));
    }

    @Test
    public void sampleWithSizeIsZero() throws Exception {
        // given
        String dataSetId = createCSVDataSet(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV));
        // when
        String sample = requestDataSetSample(dataSetId, "0");
        // then
        assertEquals(100, getNumberOfRecords(sample));
    }

    @Test(expected = java.lang.AssertionError.class)
    public void sampleWithDecimalSize() throws Exception {
        // given
        String dataSetId = createCSVDataSet(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV));
        // when
        String sample = requestDataSetSample(dataSetId, "10.5");
        // then expect error (400 bad request)
    }

    @Test(expected = java.lang.AssertionError.class)
    public void sampleWithBadContent() throws Exception {
        // given
        String dataSetId = createCSVDataSet(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV));
        // when
        String sample = requestDataSetSample(dataSetId, "ghqmkdhjsgf");
        // then expect error (400 bad request)
    }

    @Test
    public void delete() throws Exception {
        String expectedId = UUID.randomUUID().toString();

        DataSetMetadata dataSetMetadata = metadata().id(expectedId).formatGuessId(new CSVFormatGuess().getBeanId()).build();

        dataSetMetadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);

        List<String> ids = from(when().get("/datasets").asString()).get("");
        assertThat(ids.size(), is(1));
        int before = dataSetMetadataRepository.size();
        when().delete("/datasets/{id}", expectedId).then().statusCode(HttpStatus.OK.value());
        int after = dataSetMetadataRepository.size();
        logger.debug("delete before {} after {}", before, after);
        assertThat(before - after, is(1));
    }

    @Test
    public void updateRawContent() throws Exception {
        String dataSetId = "123456";
        given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV))).when()
                .put("/datasets/{id}/raw", dataSetId).then().statusCode(HttpStatus.OK.value());
        List<String> ids = from(when().get("/datasets").asString()).get("id");
        assertThat(ids, hasItem(dataSetId));
        assertQueueMessages(dataSetId);
    }

    @Test
    public void updateMetadataContentWithWrongDatasetId() throws Exception {
        assertThat(dataSetMetadataRepository.get("3d72677c-e2c9-4a34-8c58-959a56ec8643"), nullValue());
        given().contentType(JSON) //
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(METADATA_JSON))) //
                .when() //
                .put("/datasets/{id}", "3d72677c-e2c9-4a34-8c58-959a56ec8643") //
                .then() //
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void previewNonDraft() throws Exception {
        // Create a data set
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
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
                .statusCode(HttpStatus.OK.value());
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

        // Talend_Desk-Tableau_de_Bord-011214.xls

        String dataSetId = given()
                .body(IOUtils
                        .toByteArray(DataSetServiceTests.class.getResourceAsStream("../Talend_Desk-Tableau_de_Bord-011214.xls")))
                .when().post("/datasets").asString();


        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        dataSetMetadataRepository.add(dataSetMetadata);

        ObjectMapper objectMapper = new ObjectMapper();

        String json = given().contentType(JSON).get("/datasets/{id}/preview?sheetName=Leads", dataSetId).asString();

        DataSet dataSet = objectMapper.reader(DataSet.class).readValue(json);

        Assertions.assertThat(dataSet.getColumns()).isNotNull().isNotEmpty().isNotEmpty().hasSize(14);

        json = given().contentType(JSON).get("/datasets/{id}/preview?sheetName=Tableau de bord", dataSetId)
                .asString();

        dataSet = objectMapper.reader(DataSet.class).readValue(json);

        Assertions.assertThat(dataSet.getColumns()).isNotNull().isNotEmpty().isNotEmpty().hasSize(7);

    }

    @Test
    public void should_get_content_from_semi_colon_csv() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();

        // then
        final String contentAsString = IOUtils.toString(content);
        final InputStream expected = DataSetServiceTests.class.getResourceAsStream("../content/test1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void should_get_content_from_coma_csv() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA2_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();

        // then
        final String contentAsString = IOUtils.toString(content);
        final InputStream expected = DataSetServiceTests.class.getResourceAsStream("../content/test1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void should_get_content_from_updated_dataset() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // given: update content
        given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw");
        assertQueueMessages(dataSetId);

        // when
        final InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();
        final String contentAsString = IOUtils.toString(content);

        // then
        final InputStream expected = DataSetServiceTests.class.getResourceAsStream("../content/test2.json");
        assertThat(contentAsString, sameJSONAsFile(expected));

        // Update name
        String expectedName = "testOfADataSetName";
        given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw?name=" + expectedName);
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertThat(dataSetMetadata.getName(), is(expectedName));
    }

    @Test
    public void should_update_dataset_name() throws Exception {
        // given
        final String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);

        // when
        final String expectedName = "testOfADataSetName";
        given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../tagada3.csv")))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/" + dataSetId + "/raw?name=" + expectedName);

        // then
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, notNullValue());
        assertThat(dataSetMetadata.getName(), is(expectedName));
    }

    /**
     * Test the import of a csv file with a really low separator coefficient variation.
     *
     * @see org.talend.dataprep.schema.LineBasedFormatGuesser
     */
    @Test
    public void testLowSeparatorOccurencesInCSV() throws Exception {

        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../avengers.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();

        assertQueueMessages(dataSetId);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream("../avengers_expected.json");
        String datasetContent = given().when().get("/datasets/{id}/content?metadata=false&columns=true", dataSetId).asString();

        assertThat(datasetContent, sameJSONAsFile(expected));
    }

    /**
     * Test the import of an excel file that is also detected as csv file. See
     * https://jira.talendforge.org/browse/TDP-258
     *
     * @see org.talend.dataprep.schema.LineBasedFormatGuesser
     */
    @Test
    public void testXlsFileThatIsAlsoParsedAsCSV() throws Exception {

        String dataSetId = given()
                .body(IOUtils.toByteArray(this.getClass().getResourceAsStream("../TDP-375_xsl_read_as_csv.xls")))
                // .queryParam("Content-Type", "application/vnd.ms-excel")
                .when().post("/datasets").asString();

        assertQueueMessages(dataSetId);

        String json = given().when().get("/datasets/{id}/metadata", dataSetId).asString();
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode = mapper.reader().readTree(json);
        final JsonNode metadata = rootNode.get("metadata");

        // only interested in the parser --> excel parser must be used !
        assertEquals(metadata.get("type").asText(), "application/vnd.ms-excel");
        assertEquals(metadata.get("formatGuess").asText(), "formatGuess#xls");
        assertEquals(metadata.get("records").asText(), "500");
    }

    @Test
    public void testQuotes() throws Exception {
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../bands_quotes.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream("../test_quotes.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void testQuotesAndCarriageReturn() throws Exception {
        String dataSetId = given()
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../bands_quotes_and_carriage_return.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream("../test_quotes_and_carriage_return.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-71
     */
    @Test
    public void empty_lines_and_missing_values() throws Exception {
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(US_STATES_TO_CLEAN_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=false&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream("../us_states_to_clean.csv_expected.json");
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void nbLines() throws Exception {
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs("{\"metadata\":{\"records\":2,\"nbLinesHeader\":1,\"nbLinesFooter\":0}}")
                .allowingExtraUnexpectedFields().allowingAnyArrayOrdering());
    }

    @Test
    public void nbLines2() throws Exception {
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV_EXPECTED_JSON);
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void nbLinesUpdate() throws Exception {
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        assertQueueMessages(dataSetId);
        InputStream content = when().get("/datasets/{id}/content?metadata=true&columns=false", dataSetId).asInputStream();
        String contentAsString = IOUtils.toString(content);

        assertThat(contentAsString, sameJSONAs("{\"metadata\":{\"records\":2,\"nbLinesHeader\":1,\"nbLinesFooter\":0}}")
                .allowingExtraUnexpectedFields().allowingAnyArrayOrdering());

        given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV)))
                .queryParam("Content-Type", "text/csv").when().put("/datasets/{id}/raw", dataSetId).asString();

        assertQueueMessages(dataSetId);

        content = when().get("/datasets/{id}/content?metadata=true&columns=false", dataSetId).asInputStream();
        contentAsString = IOUtils.toString(content);

        InputStream expected = DataSetServiceTests.class.getResourceAsStream(T_SHIRT_100_CSV_EXPECTED_JSON);
        assertThat(contentAsString, sameJSONAsFile(expected));
    }

    @Test
    public void getMetadata() throws Exception {
        DataSetMetadata.Builder builder = DataSetMetadata.Builder.metadata().id("1234");
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
                .formatGuessId(new CSVFormatGuess().getBeanId()) //
                .mediaType("text/csv");

        DataSetMetadata metadata = builder.build();
        metadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");

        dataSetMetadataRepository.add(metadata);
        String contentAsString = when().get("/datasets/{id}/metadata", "1234").asString();
        InputStream expected = DataSetServiceTests.class.getResourceAsStream("../metadata1.json");
        assertThat(contentAsString, sameJSONAsFile(expected));

        Boolean isFavorites = from(contentAsString).get("metadata.favorite");
        assertFalse(isFavorites);

        // add favorite
        UserData userData = new UserData("anonymousUser");
        HashSet<String> favorites = new HashSet<>();
        favorites.add("1234");
        userData.setFavoritesDatasets(favorites);
        userDataRepository.setUserData(userData);

        contentAsString = when().get("/datasets/{id}/metadata", "1234").asString();
        isFavorites = from(contentAsString).get("metadata.favorite");
        assertTrue(isFavorites);

    }

    @Test
    public void getEmptyMetadata() throws Exception {
        DataSetMetadata metadata = dataSetMetadataRepository.get("9876");
        assertNull(metadata);
        int statusCode = when().get("/datasets/{id}/metadata", "9876").statusCode();

        assertThat(statusCode, is(HttpServletResponse.SC_NO_CONTENT));

    }

    /**
     * Check that the error listing service returns a list parsable of error codes. The content is not checked
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void shouldListErrors() throws Exception {
        String errors = when().get("/datasets/errors").asString();

        ObjectMapper mapper = new ObjectMapper();
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
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, CoreMatchers.notNullValue());
        assertEquals(Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, CoreMatchers.notNullValue());
        assertEquals(Certification.PENDING, dataSetMetadata.getGovernance().getCertificationStep());
        assertThat(dataSetMetadata.getRow().getColumns(), not(empty()));
    }

    @Test
    public void testCertify() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV)))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, CoreMatchers.notNullValue());
        int originalNbLines = dataSetMetadata.getContent().getNbRecords(); // to check later if no modified
        assertEquals(Certification.NONE, dataSetMetadata.getGovernance().getCertificationStep());

        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());
        when().put("/datasets/{id}/processcertification", dataSetId).then().statusCode(HttpStatus.OK.value());
        dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadata, CoreMatchers.notNullValue());
        assertEquals(Certification.CERTIFIED, dataSetMetadata.getGovernance().getCertificationStep());
        assertEquals(originalNbLines, dataSetMetadata.getContent().getNbRecords());
    }

    @Test
    public void testGetFavoritesDatasetList() {
        when().get("/datasets/favorites").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));
        String dsId1 = UUID.randomUUID().toString();
        String dsId2 = UUID.randomUUID().toString();
        // this test assumes that the default user id is "anonymousUser"
        UserData userData = new UserData("anonymousUser");
        HashSet<String> favorites = new HashSet<>();
        favorites.add(dsId1);
        favorites.add(dsId2);
        userData.setFavoritesDatasets(favorites);
        userDataRepository.setUserData(userData);
        List<String> favoritesResp = from(when().get("/datasets/favorites").asString()).get();
        assertEquals(2, favoritesResp.size());
        assertThat(favoritesResp, hasItems(dsId1, dsId2));
    }

    @Test
    public void testSetUnsetFavoriteDataSet() throws IOException {
        String dsId1 = UUID.randomUUID().toString();
        String dsId2 = UUID.randomUUID().toString();

        when().get("/datasets/favorites").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));
        dataSetMetadataRepository.add(new DataSetMetadata(dsId1, null, null, 0, null));
        dataSetMetadataRepository.add(new DataSetMetadata(dsId2, null, null, 0, null));
        // check set
        when().put("/datasets/{id}/favorite", dsId1).then().statusCode(HttpStatus.OK.value());
        when().put("/datasets/{id}/favorite?unset=false", dsId2).then().statusCode(HttpStatus.OK.value());
        List<String> favoritesResp = from(when().get("/datasets/favorites").asString()).get(); //$NON-NLS-1$
        assertEquals(2, favoritesResp.size());
        assertThat(favoritesResp, hasItems(dsId1, dsId2));
        // check unset
        when().put("/datasets/{id}/favorite?unset=true", dsId2).then().statusCode(HttpStatus.OK.value());
        favoritesResp = from(when().get("/datasets/favorites").asString()).get();
        assertEquals(1, favoritesResp.size());
        assertThat(favoritesResp, hasItem(dsId1));
        // check wrong datasetId
        String wrongDsId = UUID.randomUUID().toString();
        assertThat(dataSetMetadataRepository.get(wrongDsId), nullValue());
        given().contentType(JSON) //
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(METADATA_JSON))) //
                .when() //
                .put("/datasets/{id}/favorite", wrongDsId) //
                .then() //
                .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    public void testFavoritesTransientNotStored() {
        String expectedDsId = UUID.randomUUID().toString();

        DataSetMetadata dataSetMetadataToBeSet = new DataSetMetadata(expectedDsId, "", "", 0, null);
        dataSetMetadataToBeSet.setFavorite(true);
        dataSetMetadataRepository.add(dataSetMetadataToBeSet);
        DataSetMetadata dataSetMetadataGet = dataSetMetadataRepository.get(expectedDsId);
        assertNotNull(dataSetMetadataGet);
        assertFalse(dataSetMetadataGet.isFavorite());
    }

    @Test
    public void updateDatasetColumn_should_update_domain() throws Exception {
        //given
        final String dataSetId = given() //
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        final ColumnMetadata column = dataSetMetadata.getRow().getColumns()
                .stream()
                .filter(col -> col.getId().equals("0002"))
                .findFirst()
                .get();
        final SemanticDomain jsoDomain = new SemanticDomain("JSO", "JSO label", 1.0F);
        column.getSemanticDomains().add(jsoDomain);

        assertThat(column.getDomain(), is("First Name"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(2.0F));

        //when
        final Response res = given() //
                .body("{\"domain\": \"JSO\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        //then
        res.then().statusCode(200);
        assertThat(column.getDomain(), is("JSO"));
        assertThat(column.getDomainLabel(), is("JSO label"));
        assertThat(column.getDomainFrequency(), is(1.0F));
    }

    @Test
    public void updateDatasetColumn_should_update_type() throws Exception {
        //given
        final String dataSetId = given() //
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        final ColumnMetadata column = dataSetMetadata.getRow().getColumns()
                .stream()
                .filter(col -> col.getId().equals("0002"))
                .findFirst()
                .get();

        assertThat(column.getDomain(), is("First Name"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(2.0F));
        assertThat(column.getType(), is("string"));

        //when
        final Response res = given() //
                .body("{\"type\": \"integer\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        //then
        res.then().statusCode(200);
        assertThat(column.getDomain(), is("First Name"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(2.0F));
        assertThat(column.getType(), is("integer"));
    }

    @Test
    public void updateDatasetColumn_should_clear_domain() throws Exception {
        //given
        final String dataSetId = given() //
                .body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream(TAGADA_CSV))) //
                .queryParam("Content-Type", "text/csv") //
                .when() //
                .post("/datasets") //
                .asString();

        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        final ColumnMetadata column = dataSetMetadata.getRow().getColumns()
                .stream()
                .filter(col -> col.getId().equals("0002"))
                .findFirst()
                .get();

        assertThat(column.getDomain(), is("First Name"));
        assertThat(column.getDomainLabel(), is("First Name"));
        assertThat(column.getDomainFrequency(), is(2.0F));

        //when
        final Response res = given() //
                .body("{\"domain\": \"\"}") //
                .when() //
                .contentType(JSON) //
                .post("/datasets/{dataSetId}/column/{columnId}", dataSetId, "0002");

        //then
        res.then().statusCode(200);
        assertThat(column.getDomain(), is(""));
        assertThat(column.getDomainLabel(), is(""));
        assertThat(column.getDomainFrequency(), is(0.0F));
    }

    @Test
    public void datePattern() throws Exception {
        int before = dataSetMetadataRepository.size();
        String dataSetId = given().body(IOUtils.toString(DataSetServiceTests.class.getResourceAsStream("../date_time_pattern.csv")))
                .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
        int after = dataSetMetadataRepository.size();
        assertThat(after - before, is(1));
        assertQueueMessages(dataSetId);
        final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(dataSetMetadata);
        final ColumnMetadata column = dataSetMetadata.getRow().getById("0001");

        assertThat(column.getType(), is("date"));
        assertThat(column.getDomain(), is(SemanticCategoryEnum.UNKNOWN.getDisplayName()));
        assertThat(column.getStatistics(), sameJSONAsFile(DataSetServiceTests.class.getResourceAsStream("../date_time_pattern_expected.json")));

    }

    private String insertEmptyDataSet() {
        String datasetId = UUID.randomUUID().toString();
        DataSetMetadata dataSetMetadata = metadata().id(datasetId).formatGuessId(new CSVFormatGuess().getBeanId()).build();
        dataSetMetadata.getContent().addParameter(CSVFormatGuess.SEPARATOR_PARAMETER, ";");
        dataSetMetadataRepository.add(dataSetMetadata);
        contentStore.storeAsRaw(dataSetMetadata, new ByteArrayInputStream(new byte[0]));
        return datasetId;
    }

    private String createCSVDataSet(InputStream content) throws Exception {
        String dataSetId = given().body(IOUtils.toString(content)).queryParam("Content-Type", "text/csv").when().post("/datasets")
                .asString();
        assertQueueMessages(dataSetId);
        return dataSetId;
    }

    private String requestDataSetSample(String dataSetId, String sampleSize) {
        return given() //
                .expect() //
                .statusCode(200) //
                // .log().ifValidationFails() //
                .when() //
                .get("/datasets/{id}/content?metadata=false&columns=false&sample={sampleSize}", dataSetId, sampleSize) //
                .asString();

    }

    private long getNumberOfRecords(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        JsonNode records = rootNode.findPath("records");
        return records.size();
    }
}
