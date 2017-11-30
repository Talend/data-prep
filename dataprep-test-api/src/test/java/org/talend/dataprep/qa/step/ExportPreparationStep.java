package org.talend.dataprep.qa.step;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.step.export.ExportStep;
import org.talend.dataprep.qa.util.export.ExportParamAnalyzer;
import org.talend.dataprep.qa.util.export.ExportType;
import org.talend.dataprep.qa.util.export.MandatoryParameters;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

/**
 * Step dealing with preparation
 */
public class ExportPreparationStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportPreparationStep.class);

    @Autowired
    ExportParamAnalyzer epAnalyzer;

    @When("^I export the preparation with parameters :$")
    public void whenIExportThePreparationWithCustomParametersInto(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);

        ExportType exportType = epAnalyzer.detectExportType(params);
//        EnumMap<MandatoryParameters, String> mParams = epAnalyzer.extractMandatoryParameters(params);

        ExportStep exporter = epAnalyzer.getExporter(exportType);
        exporter.export(params);
    }

}
