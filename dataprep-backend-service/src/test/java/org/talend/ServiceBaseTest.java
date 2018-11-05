// ============================================================================
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

package org.talend;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.talend.ServiceBaseTest.TEST_LOCALE;

import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import org.talend.daikon.content.local.LocalContentServiceConfiguration;
import org.talend.dataprep.configuration.DataPrepComponentScanConfiguration;
import org.talend.dataprep.dataset.adapter.MockDatasetServer;
import org.talend.dataprep.test.LocalizationRule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;

@RunWith(SpringRunner.class)
@Import({ LocalContentServiceConfiguration.class, DataPrepComponentScanConfiguration.class })
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = ServiceBaseTest.DatasetClientTestConfiguration.class,
        properties = { "dataset.asynchronous.analysis=false", "content-service.store=local",
                "dataprep.locale:" + TEST_LOCALE })
public abstract class ServiceBaseTest {

    public static final String TEST_LOCALE = "en-US";

    @Rule
    public LocalizationRule rule = new LocalizationRule(Locale.US);

    @LocalServerPort
    protected int port;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper mapper;

    private boolean environmentSet = false;

    @Before
    public void setUp() {
        if (!environmentSet) {
            RestAssured.baseURI = RestAssured.DEFAULT_URI;
            RestAssured.port = port;

            // Overrides connection information with random port value
            String url = RestAssured.baseURI + ":" + port;
            MockPropertySource connectionInformation = new MockPropertySource()
                    .withProperty("dataset.service.url", url)
                    .withProperty("transformation.service.url", url)
                    .withProperty("preparation.service.url", url)
                    .withProperty("async_store.service.url", url)
                    .withProperty("gateway.service.url", url)
                    .withProperty("fullrun.service.url", url);
            environment.getPropertySources().addFirst(connectionInformation);
            environmentSet = true;
        }
    }

    @Test
    public void contextLoads() {
        // this is needed for tests suites, so that they have only one context load
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(org.talend.dataprep.configuration.HttpClient.class)
    public static class DatasetClientTestConfiguration {

        @Bean
        MockDatasetServer mockDatasetServer() {
            return new MockDatasetServer();
        }

    }

}
