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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.talend.dataprep.security.Security;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ProxyDatasetClient implements DatasetClient {

    private final RestTemplate restTemplate;

    public ProxyDatasetClient(RestTemplateBuilder builder, URL url, Security security) {
        this.restTemplate = builder.rootUri(url.toString())
                .additionalInterceptors(new SecurityAuthorizationInterceptor(security))
                .build();
    }

    @Override
    public Dataset findOne(String datasetId) {
        return restTemplate.getForObject("/datasets/{datasetId}", Dataset.class, datasetId);
    }

    @Override
    public ObjectNode findSchema(String datasetId) {
        return restTemplate.getForObject("/dataset-sample/" + datasetId, EncodedSample.class).getSchema();
    }

    @Override
    public String findData(String datasetId, PageRequest pageRequest) {
        return restTemplate.getForObject("/dataset-sample/{datasetId}?offset={offset}&limit={limit}", String.class,
                datasetId, pageRequest.getOffset(), pageRequest.getPageSize());
    }

    @Override
    public List<Dataset> findAll() {
        return Arrays.asList(restTemplate.getForObject("/datasets", Dataset[].class));
    }

    @Override
    public boolean exists(String datasetId) {
        return findOne(datasetId) != null;
    }

    @Override
    public long count() {
        return findAll().size();
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Dataset entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * Add authentication token to requests.
     *
     * @see org.springframework.http.client.support.BasicAuthorizationInterceptor
     */
    private static class SecurityAuthorizationInterceptor implements ClientHttpRequestInterceptor {

        private Security security;

        public SecurityAuthorizationInterceptor(Security security) {
            Validate.notNull(security);
            this.security = security;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, security.getAuthenticationToken());
            return execution.execute(request, body);
        }
    }
}
