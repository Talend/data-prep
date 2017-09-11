package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.helper.DataPrepAPIHelper;


/**
 * Step dealing with dataset
 */
public class DataSetStep implements En {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataSetStep.class);

    @Autowired
    private DataPrepAPIHelper dpah;

    /**
     * Default constructor
     */
    public DataSetStep() {

        Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$", (String fileName, String name) -> {
            LOG.debug("I upload the dataset {} with name {}", fileName, name);
        });

    }
}
