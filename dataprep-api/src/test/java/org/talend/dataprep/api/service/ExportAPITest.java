package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

        final List<String> steps = given().get("/api/preparations/{preparation}/details", preparationId).jsonPath()
                .getList("steps");
        final String firstActionStep = steps.get(1);

        // when
        final String export = given().formParam("exportType", "CSV").formParam("preparationId", preparationId)
                .formParam("stepId", firstActionStep).when().get("/api/export").asString();

        // then
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvWithDefaultSeparator() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_default_separator.csv"));

        // when
        final String export = given().formParam("exportType", "CSV").formParam("preparationId", preparationId)
                .formParam("stepId", "head").when().get("/api/export").asString();

        // then
        assertEquals(expectedExport, export);
    }

    @Test
    public void testExportCsvWithSpecifiedSeparator() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        final String expectedExport = IOUtils
                .toString(this.getClass().getResourceAsStream("export/expected_export_semicolon_separator.csv"));

        // when
        final String export = given().formParam("exportType", "CSV").formParam("exportParameters.csvSeparator", ";")
                .formParam("preparationId", preparationId).formParam("stepId", "head").when().get("/api/export").asString();

        // then
        assertEquals(expectedExport, export);
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
                .formParam("exportParameters.csvSeparator", ";") //
                .formParam("preparationId", preparationId) //
                .formParam("stepId", "head") //
                .formParam("exportParameters.fileName", fileName) //
                .when() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat( contentDispositionHeaderValue ).contains( "filename=\"" + fileName );

    }

    @Test
    public void testExport_default_filename() throws Exception {
        // given
        final String preparationId = createPreparationFromFile("export/export_dataset.csv", "testExport", "text/csv");

        String fileName = "testExport.csv";

        // when
        final Response export = given() //
                .formParam("exportType", "CSV") //
                .formParam("exportParameters.csvSeparator", ";") //
            .formParam("preparationId", preparationId) //
            .formParam("stepId", "head") //
            .when() //
                .get("/api/export");

        // then
        String contentDispositionHeaderValue = export.getHeader("Content-Disposition");
        Assertions.assertThat(contentDispositionHeaderValue).contains("filename=\"" + fileName);

    }

}
