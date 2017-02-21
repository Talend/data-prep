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
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.dataprep.format.export.ExportFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

/**
 * Unit test for Export API.
 */
public class ExportAPITest extends ApiServiceTestBase {

    @Test
    public void get_all_export_types() throws Exception {
        String actual = RestAssured.when().get("/api/export/formats").asString();
        final String expectedContent = IOUtils.toString(this.getClass().getResourceAsStream("export/export_type.json"));
        JSONAssert.assertEquals(expectedContent, actual, false);
    }

    @Test
    public void testExportCsvFromDataset() throws Exception {
        // given
        final String datasetId = createDataset("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_default_separator.csv"));

        // when
        final String export = given().formParam("exportType", "CSV").formParam("datasetId", datasetId).when().get("/api/export")
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
        final String datasetId = createDataset("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_default_separator.csv"));

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
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
        final String datasetId = createDataset("export/export_dataset.csv", "testHeaders", "text/csv");

        // when
        final Response response = given() //
                .formParam("exportType", "CSV") //
                .formParam("datasetId", datasetId) //
                .when() //
                .get("/api/export");

        // then
        assertTrue(response.getContentType().startsWith("text/csv"));
        assertEquals(response.getHeader("Content-Disposition"), "attachment; filename=\"testHeaders.csv\"");
    }

    @Test
    public void testExportCsvFromPreparationStep() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");
        applyActionFromFile(preparationId, "export/upper_case_firstname.json");
        applyActionFromFile(preparationId, "export/upper_case_lastname.json");
        applyActionFromFile(preparationId, "export/delete_city.json");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_preparation_uppercase_firstname.csv"));

        final PreparationMessageForTest preparationMessage = mapper.readValue(
                given().get("/api/preparations/{preparation}/details", preparationId).asInputStream(),
                PreparationMessageForTest.class);
        final List<String> steps = preparationMessage.getSteps();

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
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
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");
        applyActionFromFile(preparationId, "export/make_header.json");
        applyActionFromFile(preparationId, "export/upper_case_lastname.json");

        final String expectedExport = IOUtils.toString(
                this.getClass().getResourceAsStream("export/expected_export_preparation_header_uppercase_firstname.csv"));

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
        assertEquals(expectedExport, export);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-1364
     */
    @Test
    public void testExportCsvWithNewColumns() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/split_cars.csv", "testSplitExport", "text/csv");
        applyActionFromFile(preparationId, "export/split.json");

        final String expectedExport = IOUtils.toString(this.getClass().getResourceAsStream("export/split_cars_expected.csv"));

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
        final String datasetId = createDataset("export/export_dataset.csv", "testExport", "text/csv");
        final String preparationId = createPreparationFromDataset(datasetId, "preparation");

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export") //
                .asString();

        // then
        final InputStream expectedInput = this.getClass().getResourceAsStream("export/expected_export_default_separator.csv");
        final String expectedExport = IOUtils.toString(expectedInput);
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvWithSpecifiedSeparator() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_space_separator.csv"));

        // when
        final String export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csvSeparator", " ") //
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
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedSemiColonExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_semicolon_separator.csv"));
        final String expectedSpaceExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_space_separator.csv"));

        // when
        final String export1 = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csvSeparator", ";") //
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
                .formParam(ExportFormat.PREFIX + "csvSeparator", " ") //
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
                .formParam("csvSeparator", ";") //
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
        final Response response = given().formParam("exportType", "CSV").formParam("csvSeparator", ";")
                .formParam("stepId", "head").when().get("/api/export");

        // then
        response.then().statusCode(400);
    }

    @Test
    public void testExport_with_filename() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        String fileName = "beerisgoodforyou";

        // when
        final Response export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csvSeparator", ";") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .formParam(ExportFormat.PREFIX + "fileName", fileName) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat(contentDispositionHeaderValue).contains("filename=\"" + fileName);

    }

    @Test
    public void testExport_default_filename() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        String fileName = "testExport.csv";

        // when
        final Response export = given() //
                .formParam("exportType", "CSV") //
                .formParam(ExportFormat.PREFIX + "csvSeparator", ";") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat(contentDispositionHeaderValue).contains("filename=\"" + fileName);

    }

    @Test
    public void testDataSetExports() throws Exception {
        // given
        final String datasetId = createDataset("export/export_dataset.csv", "testExport", "text/csv");

        // when
        final Response exportFormats = given().get("/api/export/formats/datasets/" + datasetId);

        // then
        final JsonNode dataSetResponseNode = mapper.readTree(exportFormats.asInputStream());
        assertEquals(2, dataSetResponseNode.size());
    }

    @Test
    public void testPreparationExports() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        // when
        final Response exportFormats = given().get("/api/export/formats/preparations/" + preparationId);

        // then
        final JsonNode preparationResponseNode = mapper.readTree(exportFormats.asInputStream());
        assertEquals(2, preparationResponseNode.size());
    }
}
