package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;

/**
 * Step dealing with action
 */
public class ActionStep implements En {

    /**
     * Default constructor
     */
    public ActionStep() {

        When("^I add a step \"(.*)\" to the column \"(.*)\" of the preparation \"(.*)\"$", (String actionName, String columnName, String preparationName) -> {
            System.out.println("actionName = " + actionName);
            System.out.println("columnName = " + columnName);
            System.out.println("preparationName = " + preparationName);
        });

    }
}
