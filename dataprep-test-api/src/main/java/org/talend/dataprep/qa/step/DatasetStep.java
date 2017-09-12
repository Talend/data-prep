package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step dealing with dataset
 */
public class DatasetStep extends DataPrepStep implements En{

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetStep.class);

    /**
     * Default constructor
     */
    public DatasetStep() {

        Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$", (String fileName, String name) -> {
            LOG.debug("I upload the dataset {} with name {}.", fileName, name);
            try {
                String datasetId = dpah.uploadDataset(fileName, name)
                        .then().statusCode(200)
                        .extract().body().asString();
                context.storeDatasetRef(datasetId, name);
            } catch (java.io.IOException ioException) {
                LOG.error("Fail to upload file {}.", fileName, ioException);
                Assert.fail();
            }
        });

    }
}
