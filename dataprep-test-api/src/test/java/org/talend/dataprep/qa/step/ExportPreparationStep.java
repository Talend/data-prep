package org.talend.dataprep.qa.step;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.step.export.ExportSampleStep;
import org.talend.dataprep.qa.util.export.ExportParamAnalyzer;
import org.talend.dataprep.qa.util.export.ExportType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Step dealing with preparation
 */
public class ExportPreparationStep extends DataPrepStep {

    @Autowired
    ExportParamAnalyzer epAnalyzer;

    @When("^I export the preparation with parameters :$")
    public void whenIExportThePreparationWithCustomParametersInto(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);

        ExportType exportType = epAnalyzer.detectExportType(params);

        ExportSampleStep exporter = epAnalyzer.getExporter(exportType);
        if (exporter == null) {
            Assert.fail("No exporter available for " + exportType.getName() + " export type.");
        }
        exporter.exportSample(params);
    }

    @When("^I get the export formats for the preparation \"(.*)\"$")
    public void whenIGetExportFormat(String preparationName) throws IOException {
        String preparationId = context.getPreparationId(preparationName);

        Response apiResponse = api.getExportFormats(preparationId);

        ExportFormatMessage[] parameters = objectMapper.readValue(apiResponse.getBody().asString(), ExportFormatMessage[].class);
        context.storePreparationExportFormat(preparationName, parameters);
    }

    @Then("^I received the right \"(.*)\" export format for preparation \"(.*)\"$")
    public void thenIReceievedTheRightExportFormat(String exportFormatId, String preparationName) throws IOException {
        final InputStream expectedJson = ExportPreparationStep.class.getResourceAsStream("export/" + exportFormatId + ".json");
        ExportFormatMessage expectedExportFormat = objectMapper.readValue(expectedJson, ExportFormatMessage.class);

        ExportFormatMessage[] exportFormats = context.getExportFormatsByPreparationName(preparationName);
        ExportFormatMessage foundExportFormat = null;

        for (ExportFormatMessage exportFormat : exportFormats) {
            if (exportFormat.getId().equals(exportFormatId)) {
                foundExportFormat = exportFormat;
                break;
            }
        }

        Assert.assertNotNull(exportFormatId + " not found", foundExportFormat);
        Assert.assertEquals(expectedExportFormat, foundExportFormat);
    }
}
