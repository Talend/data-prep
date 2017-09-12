package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        After(() -> {
            // cleaning dataset
            context.getDatasetIds().forEach(datasetId -> {
                dpah.deleteDataSet(datasetId).then().statusCode(200);
                LOG.debug("Suppression of dataset {}.", datasetId);
            });
            context.clearDataset();

            // cleaning preparation
            context.getPreparationIds().forEach(preparationId -> {
                dpah.deletePreparation(preparationId).then().statusCode(200);
                LOG.debug("Suppression of preparation {}.", preparationId);
            });
            context.clearPreparation();
        });
    }
}
