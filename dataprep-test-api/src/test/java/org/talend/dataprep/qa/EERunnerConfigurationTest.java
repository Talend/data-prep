package org.talend.dataprep.qa;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This runner is used to run both OS and EE specific cucumber test.
 * Before each scenario it will log the user for EE context
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber" , "json:target/cucumber.json"},
        glue = "classpath:org/talend/dataprep/qa/step",
        features = "classpath:features/")
public class EERunnerConfigurationTest extends DataStepRunner {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EERunnerConfigurationTest.class);

    @BeforeClass
    public static void setUp() {
        LOG.info("Adding EE context information before launching cucumber test");
    }

}
