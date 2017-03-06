// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import org.talend.daikon.content.local.LocalContentServiceConfiguration;

@RunWith(SpringRunner.class)
@Import(LocalContentServiceConfiguration.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "dataset.asynchronous.analysis=false",
        "content-service.store=local" })
public abstract class ServiceBaseTest {

    @Configuration
    @ComponentScan(basePackages = {"org.talend.daikon.content", "org.talend.dataprep"})
    public static class TestComponentScan {
    }

    @Value("${local.server.port}")
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
            RestAssured.port = port;

            // Overrides connection information with random port value
            MockPropertySource connectionInformation = new MockPropertySource()
                    .withProperty("dataset.service.url", "http://localhost:" + port)
                    .withProperty("transformation.service.url", "http://localhost:" + port)
                    .withProperty("preparation.service.url", "http://localhost:" + port);
            environment.getPropertySources().addFirst(connectionInformation);
            environmentSet = true;
        }
    }

    @Test
    public void contextLoads() {
        // this is needed for tests suites, so that they have only one context load
    }

}
