package org.talend.dataprep.qa;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * This runner is used to run both OS and EE specific cucumber test.
 * Before each scenario it will log the user for EE context
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber"}, glue = "classpath:org/talend/dataprep/qa/step", features = "classpath:features/")
public class EERunnerConfigurationTest {

    @BeforeClass
    public static void setUp(){
        System.out.println("adding EE Context");
    }

}
