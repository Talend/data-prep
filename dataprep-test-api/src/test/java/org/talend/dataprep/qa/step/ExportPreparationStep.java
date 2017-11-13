package org.talend.dataprep.qa.step;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

/**
 * Step dealing with preparation
 */
public class ExportPreparationStep extends DataPrepStep {

    /** {@link cucumber.api.DataTable} key for preparationName value. */
    private static final String PREPARATION_NAME = "preparationName";

    private static final String DATASET_NAME = "datasetName";

    private static final String FILE_NAME = "fileName";

    private static final String CSV_ESCAPE_CHARACTER_PARAM = "exportType";

    private static final String CSV_FIELDS_DELIMITER = "csv_fields_delimiter";

    private static final String CSV_ENCLOSURE_CHARACTER_PARAM = "csv_enclosure_delimiter";

    private static final String CSV_ENCLOSURE_MODE_PARAM = "csv_enclosure_char";

    private static final String CSV_CHARSET_PARAM = "csv_charset";

    private static final String CSV_EXPORT = "CSV";

    private static final String EXPORT_TYPE = "exportType";

    private static final String FILENAME = "filename";

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportPreparationStep.class);

    @When("^I export the preparation \"(.*)\" on the dataset \"(.*)\" and export the result in \"(.*)\" temporary file.$")
    public void whenIExportThePreparationInto(String preparationName, String datasetName, String filename) throws IOException {
        LOGGER.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName,
                datasetName, filename);
        String datasetId = context.getDatasetId(datasetName);
        String preparationId = context.getPreparationId(preparationName);
        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        final InputStream csv = api
                .executeFullExport("CSV", datasetId, preparationId, steps.get(steps.size() - 1), ";", filename)
                .asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

    @When("^I export the preparation \"(.*)\" on the dataset \"(.*)\" and export the result with \"(.*)\" as escape character in \"(.*)\" temporary file.$")
    public void whenIExportThePreparationWithACustomEscapeCharacterInto(String preparationName, String datasetName,
            String escapeCharacter, String filename) throws IOException {
        LOGGER.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName,
                datasetName, filename);
        String datasetId = context.getDatasetId(datasetName);
        String preparationId = context.getPreparationId(preparationName);
        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        final InputStream csv = api.executeFullExport("CSV", datasetId, preparationId, steps.get(steps.size() - 1), ";",
                filename, escapeCharacter, null, null, null).asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

    @When("^I export the preparation with custom parameters :$")
    public void whenIExportThePreparationWithCustomParametersInto(DataTable dataTable) throws IOException {

        Map<String, String> params = dataTable.asMap(String.class, String.class);

        String preparationName = params.get(PREPARATION_NAME);
        String preparationId = context.getPreparationId(preparationName);
        String datasetName = params.get(DATASET_NAME);
        String filename = params.get(FILE_NAME);
        String datasetId = context.getDatasetId(datasetName);
        String escapeCharacter = params.get(CSV_ESCAPE_CHARACTER_PARAM);
        String delimiter = params.get(CSV_FIELDS_DELIMITER);
        String enclosureCharacter = params.get(CSV_ENCLOSURE_CHARACTER_PARAM);
        String enclosureMode = params.get(CSV_ENCLOSURE_MODE_PARAM);
        String charset = params.get(CSV_CHARSET_PARAM);

        LOGGER.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName,
                datasetName, filename);

        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        final InputStream csv = api.executeFullExport(CSV_EXPORT, datasetId, preparationId, steps.get(steps.size() - 1),
                delimiter, filename, escapeCharacter, enclosureCharacter, enclosureMode, charset).asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

}
