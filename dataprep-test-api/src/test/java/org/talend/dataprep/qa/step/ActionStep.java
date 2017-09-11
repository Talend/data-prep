package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.qa.SpringContextConfiguration;

/**
 * Step dealing with action
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
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
