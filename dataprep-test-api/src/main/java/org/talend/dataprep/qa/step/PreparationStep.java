package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Step dealing with preparation
 */
public class PreparationStep extends DataPrepStep implements En {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PreparationStep.class);

    /**
     * Default constructor
     */
    public PreparationStep() {

        Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$", (String preparationName, String datasetName) -> {
            LOG.debug("I create a preparation with name {}", preparationName);
            String homeFolder = dpah.getHomeFolder();
            String preparationId = dpah.createPreparation(context.getDatasetId(datasetName), preparationName, homeFolder)
                    .then().statusCode(200)
                    .extract().body().asString();
            context.storePreparationRef(preparationId, preparationName);
        });

        When("^I export the preparation \"(.*)\" on the dataset \"(.*)\" and export the result in \"(.*)\" temporary file.$",
                (String preparationName, String datasetName, String filename) -> {
                    LOG.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName, datasetName, filename);
                    String datasetId = context.getDatasetId(datasetName);
                    String preparationId = context.getPreparationId(preparationName);
                    List<String> steps = dpah.getPreparation(preparationId)
                            .then().statusCode(200)
                            .extract().body().jsonPath().getJsonObject("steps");
                    String body = dpah.executeFullRunExport("CSV", datasetId, preparationId, steps.get(steps.size() - 1), ";", filename)
                            .then()
                            .extract().body().asString();
                    // store the body content in a temporary File
                    // FIXME : Quick and dirty split
                    String[] filenameChunks = filename.split("\\.");

                    try {
                        Path path = Files.createTempFile(filenameChunks[0], "." + filenameChunks[1]);
                        File tempFile = path.toFile();
                        Files.write(path, body.getBytes());
                        tempFile.deleteOnExit();
                        context.storeTempFile(filename, tempFile);
                    } catch (IOException ioException) {
                        LOG.error("Cannot create temporary file.", ioException);
                    }
                    LOG.debug("youhou");
                });


        Then("^I check that exported preparation equals \"(.*)\"$", (String expectedCSVFileName) -> {
            LOG.debug("I check taht export preparation equals {}", expectedCSVFileName);
        });
    }
}
