package org.talend.dataprep.dataset.client;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.talend.dataprep.dataset.domain.Dataset;
import org.talend.dataprep.dataset.client.properties.DatasetProperties;
import org.talend.dataprep.security.Security;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ProxyDatasetClientTest extends TestParent {

    public static final String AUTHENTICATION_TOKEN = "authentication-token";

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @MockBean
    private Security security;

    private DatasetClient datasetClient;

    @Before
    public void updateClientUrl() throws MalformedURLException {
        DatasetProperties datasetProperties = new DatasetProperties();
        datasetProperties.setUrl(new URL("http://localhost:" + localServerPort + "/api/v1"));
        datasetClient = new ProxyDatasetClient(restTemplateBuilder, datasetProperties, security);
        when(security.getAuthenticationToken()).thenReturn(AUTHENTICATION_TOKEN);
    }

    @Test
    public void getByIdTest() {
        Dataset toto = datasetClient.findOne("toto");
        assertNotNull(toto);
    }

}
