package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;

/**
 * Step dealing with preparation
 */
public class PreparationStep implements En {

    /**
     * Default constructor
     */
    public PreparationStep() {

        Given("^I create a preparation with name \"(.*)\"$", (String preparationName) -> {
            System.out.println("preparationName = " + preparationName);
        });

        When("^I export the preparation \"(.*)\"$", (String preparationName) -> {
            System.out.println("preparationName = " + preparationName);
        });


        Then("^I check that exported preparation equals \"(.*)\"$", (String expectedCSVFileName) -> {
            System.out.println("expectedCSVFileName = " + expectedCSVFileName);
        });
    }
}
