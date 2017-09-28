package org.talend.dataprep.qa.step.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java8.En;

/**
 * Storage for Before and After actions.
 */
public class GlobalStep extends DataPrepStep implements En {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalStep.class);

    /**
     * Default constructor
     */
    public GlobalStep() {

        // this After method has "order 1000" because we have first of all to delete all the data we have created
        After(1000, () -> {

            // cleaning temporary files
            context.clearTempFile();

            // cleaning preparation
            context.getPreparationIds().forEach(preparationId -> {
                api.deletePreparation(preparationId).then().statusCode(200);
                LOG.debug("Suppression of preparation {}.", preparationId);
            });
            context.clearPreparation();

            // cleaning dataset
            context.getDatasetIds().forEach(datasetId -> {
                api.deleteDataSet(datasetId).then().statusCode(200);
                LOG.debug("Suppression of dataset {}.", datasetId);
            });
            context.clearDataset();

            // cleaning all features context object
            context.clearObject();
        });
    }
}
