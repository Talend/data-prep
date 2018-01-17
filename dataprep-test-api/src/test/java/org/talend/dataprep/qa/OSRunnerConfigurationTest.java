// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.qa;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * This runner is used to run only OS cucumber test (do not need authentification)
 */
@RunWith(Cucumber.class) //
@CucumberOptions(plugin = { "pretty", "html:target/cucumber", "json:target/cucumber.json" }, //
        glue = { "classpath:org/talend/dataprep/qa/step", "classpath:org/talend/dataprep/qa/config" }, //
        features = "classpath:features") //
public class OSRunnerConfigurationTest {

}