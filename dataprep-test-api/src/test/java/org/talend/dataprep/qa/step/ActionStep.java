package org.talend.dataprep.qa.step;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import java.util.Map;

/**
 * Step dealing with action
 */
public class ActionStep extends DataPrepStep {

    public static final String PREPARATION_NAME = "preparationName";
    public static final String ACTION_NAME = "actionName";
    public static final String COLUMN_NAME = "columnName";
    public static final String COLUMN_ID = "columnId";


    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step \"(.*)\" to the column \"(.*)\" of the preparation \"(.*)\"$")
    public void whenIAddAStepToAPreparation(String actionName, String columnName, String preparationName) {
        LOG.debug("I add a step {} to the column {} of the preparation {}", actionName, columnName, preparationName);
        String preparationId = context.getPreparationId(preparationName);
        api.addStep(preparationId, actionName, columnName, "0001");
    }

    @When("^I add a step with parameters :$")
    public void whenIAddAStepToAPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(params.get(PREPARATION_NAME));
        api.addStep(preparationId, params.get(ACTION_NAME), params.get(COLUMN_NAME), params.get(COLUMN_ID));
    }

}
