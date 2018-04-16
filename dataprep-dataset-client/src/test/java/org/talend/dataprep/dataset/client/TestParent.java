package org.talend.dataprep.dataset.client;

import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestParent.DatasetClientTestConfiguration.class, properties = "dataset.api.url=toto")
public abstract class TestParent {

    @LocalServerPort
    protected int localServerPort;

    @SpringBootConfiguration
    @ComponentScan(basePackageClasses = DatasetClientTestConfiguration.class)
    @EnableAutoConfiguration
    @Import(org.talend.dataprep.configuration.HttpClient.class)
    public static class DatasetClientTestConfiguration {

    }

}
