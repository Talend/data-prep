package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.DataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;

/**
 * Step dealing with preparation
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class PreparationStep implements En {

    @Autowired
    private DataPrepAPIHelper dpah;

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PreparationStep.class);

    /**
     * Default constructor
     */
    public PreparationStep() {

        Given("^I create a preparation with name \"(.*)\"$", (String preparationName) -> {

            LOG.debug("I create a preparation with name {}", preparationName);
        });

        When("^I export the preparation \"(.*)\"$", (String preparationName) -> {
            LOG.debug("I export the preparation {}", preparationName);
        });


        Then("^I check that exported preparation equals \"(.*)\"$", (String expectedCSVFileName) -> {
            LOG.debug("I check taht export preparation equals {}", expectedCSVFileName);
        });
    }
}
