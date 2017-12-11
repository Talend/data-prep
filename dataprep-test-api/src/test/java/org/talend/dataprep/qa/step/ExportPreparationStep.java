package org.talend.dataprep.qa.step;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.step.export.ExportSampleStep;
import org.talend.dataprep.qa.util.export.ExportParamAnalyzer;
import org.talend.dataprep.qa.util.export.ExportType;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

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

}
