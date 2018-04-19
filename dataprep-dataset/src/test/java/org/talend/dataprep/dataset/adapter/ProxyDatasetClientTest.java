/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.talend.dataprep.dataset.domain.Dataset;
import org.talend.dataprep.security.Security;

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
        URL url = new URL("http://localhost:" + localServerPort + "/api/v1");
        datasetClient = new ProxyDatasetClient(restTemplateBuilder, url, security);
        when(security.getAuthenticationToken()).thenReturn(AUTHENTICATION_TOKEN);
    }

    @Test
    public void getByIdTest() {
        Dataset toto = datasetClient.findOne("toto");
        assertNotNull(toto);
    }

}
