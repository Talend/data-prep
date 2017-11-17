package org.talend.dataprep.qa.step;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

/**
 * Step dealing with preparation
 */
public class ExportPreparationStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportPreparationStep.class);

    @When("^I export the preparation with parameters :$")
    public void whenIExportThePreparationWithCustomParametersInto(DataTable dataTable) throws IOException {

        Map<String, String> params = dataTable.asMap(String.class, String.class);

        // Preparation
        String preparationName = params.get(PREPARATION_NAME);
        String preparationId = context.getPreparationId(preparationName);

        // Dataset
        String datasetName = params.get(DATASET_NAME);
        String datasetId = context.getDatasetId(datasetName);

        // File exported
        String filename = params.get(FILE_NAME);

        LOGGER.debug("I execute a full run on the preparation {} from the dataset {} and export the result in {} file.", preparationName,
                datasetName, filename);

        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        final InputStream csv = api.executeFullExport(CSV_EXPORT, datasetId, preparationId, steps.get(steps.size() - 1),
                params.get(CSV_FIELDS_DELIMITER), filename, params.get(CSV_ESCAPE_CHARACTER_PARAM),
                params.get(CSV_ENCLOSURE_CHARACTER_PARAM), params.get(CSV_ENCLOSURE_MODE_PARAM), params.get(CSV_CHARSET_PARAM))
                .asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

}
