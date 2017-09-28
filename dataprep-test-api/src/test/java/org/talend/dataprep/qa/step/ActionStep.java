package org.talend.dataprep.qa.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.java.en.When;

/**
 * Step dealing with action
 */
public class ActionStep extends DataPrepStep {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step \"(.*)\" to the column \"(.*)\" of the preparation \"(.*)\"$")
    public void whenIAddAStepToAPreparation(String actionName, String columnName, String preparationName) {
        LOG.debug("I add a step {} to the column {} of the preparation {}", actionName, columnName, preparationName);
        String preparationId = context.getPreparationId(preparationName);
        api.addStep(preparationId, actionName, columnName, "0001");
    }

}
