package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        When("^I full run the preparation \"(.*)\" on the dataset \"(.*)\" and export the result in \"(.*)\" file.$",
                (String preparationName, String datasetName, String filename) -> {
                    LOG.debug("I full run the preparation {} on the dataset {} and export the result in {} file.", preparationName, datasetName, filename);
//            dpah.executeFullRunExport();

                });


        Then("^I check that exported preparation equals \"(.*)\"$", (String expectedCSVFileName) -> {
            LOG.debug("I check taht export preparation equals {}", expectedCSVFileName);
        });
    }
}
