package org.talend.dataprep.qa.api.story;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/cucumber"}, features = "classpath:org/talend/dataprep/qa/api/story/TDPImportExportCsv.feature")
public class TDPImportExportCsvTest {
}
