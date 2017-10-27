package org.talend.dataprep.qa.step;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

/**
 * Step dealing with preparation
 */
public class PreparationStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String preparationName, String datasetName) {
        LOGGER.debug("I create a preparation with name {}", preparationName);
        String homeFolder = api.getHomeFolder();
        final String datasetId = context.getDatasetId(datasetName);
        if (StringUtils.isBlank(datasetId)) {
            fail("could not find dataset id from name '" + datasetName + "' in the context");
        }
        String preparationId = api.createPreparation(datasetId, preparationName, homeFolder).then() //
                .statusCode(200) //
                .extract().body().asString();
        context.storePreparationRef(preparationId, preparationName);
    }

    @When("^I export the preparation \"(.*)\" on the dataset \"(.*)\" and export the result in \"(.*)\" temporary file.$")
    public void whenIExportThePreparationInto(String preparationName, String datasetName, String filename) throws IOException {
        LOGGER.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName,
                datasetName, filename);
        String datasetId = context.getDatasetId(datasetName);
        String preparationId = context.getPreparationId(preparationName);
        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        final InputStream csv = api
                .executeFullRunExport("CSV", datasetId, preparationId, steps.get(steps.size() - 1), ";", filename)
                .asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable){
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(params.get(PREPARATION_NAME));
        Response response = api.listPreparation("Lw=="); // FIXME : Hard coded folder...

    }
}
