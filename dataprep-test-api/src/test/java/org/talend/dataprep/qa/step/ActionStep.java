package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step dealing with action
 */
public class ActionStep implements En {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    /**
     * Default constructor
     */
    public ActionStep() {

        When("^I add a step \"(.*)\" to the column \"(.*)\" of the preparation \"(.*)\"$", (String actionName, String columnName, String preparationName) -> {
            LOG.debug("I add a step {} to the column {} of the preparation {}", actionName, columnName, preparationName);
        });

    }
}
