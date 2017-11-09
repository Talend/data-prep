// ============================================================================
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

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.format.export.ExportFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.talend.dataprep.transformation.format.CSVFormat;

/**
 * Unit test for Export API.
 */
public class ExportAPITest extends ApiServiceTestBase {

    @Test
    public void get_all_export_types() throws Exception {
        String actual = RestAssured.when().get("/api/export/formats").asString();
        final String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("export/export_type.json"),
                UTF_8);
        JSONAssert.assertEquals(expectedContent, actual, false);
    }

    @Test
    public void testExportCsvFromDataset() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/export_dataset.csv", "testExport");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_default_separator.csv"), UTF_8);

        // when
        final String export = given()
                .formParam("exportType", "CSV")
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("datasetId", datasetId)
                .when().get("/api/export")
                .asString();

        // then
        assertEquals(expectedExport, export);
    }

    /**
     * @see <a href="https://jira.talendforge.org/browse/TDP-2313">TDP-2313_trying_to_export_a_dataset_does_not_work</a>
     * @throws Exception
     */
    @Test
    public void TDP_2313() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/export_dataset.csv", "testExport");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_default_separator.csv"), UTF_8);

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", "") //
                .formParam("stepId", "") //
                .formParam("datasetId", datasetId)
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        assertEquals(expectedExport, export);
    }

    @Test
    public void checkHeaders() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/export_dataset.csv", "testHeaders");

        // when
        final Response response = given() //
                .formParam("exportType", "CSV") //
                .formParam("datasetId", datasetId) //
                .when() //
                .get("/api/export");

        // then
        assertTrue(response.getContentType().startsWith("text/csv"));
        assertEquals(response.getHeader("Content-Disposition"), "attachment; filename*=UTF-8''testHeaders.csv");
    }

    @Test
    public void checkUTF8() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/_UTF-8 住所.csv", "_UTF-8 住所");

        // when
        final Response response = given() //
                .formParam("exportType", "CSV") //
                .formParam("datasetId", datasetId) //
                .when() //
                .get("/api/export");

        // then
        assertTrue(response.getContentType().startsWith("text/csv"));
        // Expect URL encoded filename
        assertEquals(response.getHeader("Content-Disposition"), "attachment; filename*=UTF-8''_UTF-8%20%E4%BD%8F%E6%89%80.csv");
    }

    @Test
    public void testExportCsvFromPreparationStep() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());
        testClient.applyActionFromFile(preparationId, "export/upper_case_firstname.json");
        testClient.applyActionFromFile(preparationId, "export/upper_case_lastname.json");
        testClient.applyActionFromFile(preparationId, "export/delete_city.json");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_preparation_uppercase_firstname.csv"),
                UTF_8);

        final EnrichedPreparation preparationMessage = mapper.readValue(
                given().get("/api/preparations/{preparation}/details", preparationId).asInputStream(),
                EnrichedPreparation.class);
        final List<String> steps = preparationMessage.getSteps();

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", steps.get(1)) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvFromPreparationStepWithMakeLineAsHeader() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());
        testClient.applyActionFromFile(preparationId, "export/make_header.json");
        testClient.applyActionFromFile(preparationId, "export/upper_case_lastname.json");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_preparation_header_uppercase_firstname.csv"),
                UTF_8);

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        assertEquals(expectedExport, export);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1364
     */
    @Test
    public void testExportCsvWithNewColumns() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/split_cars.csv", "testSplitExport", home.getId());
        testClient.applyActionFromFile(preparationId, "export/split.json");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/split_cars_expected.csv"),
                UTF_8);

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        assertFalse(expectedExport.isEmpty());
    }

    @Test
    public void testExportCsvWithDefaultSeparator() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/export_dataset.csv", "testExport");
        final String preparationId = testClient.createPreparationFromDataset(datasetId, "preparation", home.getId());

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        final InputStream expectedInput = this.getClass().getResourceAsStream("export/expected_export_default_separator.csv");
        final String expectedExport = IOUtils.toString(expectedInput, UTF_8);
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvWithSpecifiedSeparator() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_space_separator.csv"), UTF_8);

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csv_fields_delimiter", " ") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export").asString();

        // then
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvWithSeparatorChange() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());

        final String expectedSemiColonExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_semicolon_separator.csv"), UTF_8);
        final String expectedSpaceExport = IOUtils.toString(this.getClass().getResourceAsStream("export/expected_export_space_separator.csv"), UTF_8);

        // when
        final String export1 = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csv_fields_delimiter", ";") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export").asString();

        // then
        assertEquals(expectedSemiColonExport, export1);

        // when
        final String export2 = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csv_fields_delimiter", " ") //
                .formParam(ExportFormat.PREFIX + CSVFormat.Parameters.ENCLOSURE_MODE, CSVFormat.Parameters.ENCLOSURE_ALL_FIELDS) //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export").asString();

        // then
        assertEquals(expectedSpaceExport, export2);
    }

    @Test
    public void testExportCsvWithBadBodyInput_noExportType() throws Exception {
        // when
        final Response response = given() //
                .formParam("csv_fields_delimiter", ";") //
                .formParam("preparationId", "4552157454657") //
                .formParam("stepId", "head") //
                .when() //
                .get("/api/export");

        // then
        response.then().statusCode(400);
    }

    @Test
    public void testExportCsvWithBadBodyInput_noPrepId_noDatasetId() throws Exception {
        // when
        final Response response = given().formParam("exportType", "CSV").formParam("csv_fields_delimiter", ";")
                .formParam("stepId", "head").when().get("/api/export");

        // then
        response.then().statusCode(400);
    }

    @Test
    public void testExport_with_filename() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());

        String fileName = "beerisgoodforyou";

        // when
        final Response export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csv_fields_delimiter", ";") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .formParam(ExportFormat.PREFIX + "fileName", fileName) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat(contentDispositionHeaderValue).contains("filename*=UTF-8''" + fileName);

    }

    @Test
    public void testExport_default_filename() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());

        String fileName = "testExport.csv";

        // when
        final Response export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csv_fields_delimiter", ";") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat(contentDispositionHeaderValue).contains("filename*=UTF-8''" + fileName);

    }

    @Test
    public void testDataSetExports() throws Exception {
        // given
        final String datasetId = testClient.createDataset("export/export_dataset.csv", "testExport");

        // when
        final Response exportFormats = given().get("/api/export/formats/datasets/" + datasetId);

        // then
        final JsonNode dataSetResponseNode = mapper.readTree(exportFormats.asInputStream());
        assertEquals(2, dataSetResponseNode.size());
    }

    @Test
    public void testPreparationExports() throws Exception {
        // given
        final String preparationId = testClient.createPreparationFromFile("export/export_dataset.csv", "testExport", home.getId());

        // when
        final Response exportFormats = given().get("/api/export/formats/preparations/" + preparationId);

        // then
        final JsonNode preparationResponseNode = mapper.readTree(exportFormats.asInputStream());
        assertEquals(2, preparationResponseNode.size());
    }
}
