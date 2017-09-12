package org.talend.dataprep;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * This runner is used to run only OS cucumber test (do not need authentification)
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber", "json:target/cucumber.json"},
        glue = "classpath:org/talend/dataprep/qa/step",
        features = "classpath:features")
public class OSRunnerConfigurationTest {

}
