package org.talend.dataprep.qa;

import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * This runner is used to run only OS cucumber test (do not need authentification)
  */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber"}, glue = "classpath:org/talend/dataprep/qa/step", features = "classpath:features/os")
public class OSRunnerConfigurationTest {

}
