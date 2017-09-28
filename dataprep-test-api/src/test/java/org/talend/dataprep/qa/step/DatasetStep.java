package org.talend.dataprep.qa.step;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.java.en.Given;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStep.class);

    @Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$")
    public void givenIUploadTheDataSet(String fileName, String name) throws IOException {
        LOGGER.debug("I upload the dataset {} with name {}.", fileName, name);
        String datasetId = api.uploadDataset(fileName, name) //
                .then().statusCode(200) //
                .extract().body().asString();
        context.storeDatasetRef(datasetId, name);
    }

}
