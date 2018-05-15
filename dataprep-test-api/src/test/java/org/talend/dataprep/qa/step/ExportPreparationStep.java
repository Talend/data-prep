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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

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

    @Then("^I check that \"(.*)\" available export formats are :$")
    public void thenIReceivedTheRightExportFormatList(String preparationName, DataTable dataTable) throws IOException {
        String preparationId = context.getPreparationId(suffixName(preparationName));
        Response apiResponse = api.getExportFormats(preparationId);
        ExportFormatMessage[] exportFormats =
                objectMapper.readValue(apiResponse.getBody().asString(), ExportFormatMessage[].class);

        List<String> exportFormatsIds = Arrays
                .stream(exportFormats) //
                .map(ExportFormatMessage::getId) //
                .collect(Collectors.toList());
        Assert.assertTrue(exportFormatsIds.containsAll(dataTable.asList(String.class)));
    }
}
