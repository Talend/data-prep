package org.talend.dataprep.qa;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber"}, glue = "classpath:org/talend/dataprep/qa/step", features = "classpath:features/")
public class EERunnerConfigurationTest {

}
