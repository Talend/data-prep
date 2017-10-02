package org.talend.dataprep.qa.step.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.After;

/**
 * Storage for Before and After actions.
 */
public class GlobalStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalStep.class);

    // this After method has "order 1000" because we have first of all to delete all the data we have created
    @After(order = 1000)
    public void after() {
        // cleaning temporary files
        context.clearTempFile();

        // cleaning preparation
        context.getPreparationIds().forEach(preparationId -> {
            api.deletePreparation(preparationId).then().statusCode(200);
            LOGGER.debug("Suppression of preparation {}.", preparationId);
        });
        context.clearPreparation();

        // cleaning dataset
        context.getDatasetIds().forEach(datasetId -> {
            api.deleteDataSet(datasetId).then().statusCode(200);
            LOGGER.debug("Suppression of dataset {}.", datasetId);
        });
        context.clearDataset();

        // cleaning all features context object
        context.clearObject();
    }

}
