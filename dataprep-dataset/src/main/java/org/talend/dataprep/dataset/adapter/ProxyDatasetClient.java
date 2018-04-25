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

import org.apache.commons.lang3.Validate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.util.avro.AvroUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;

public class ProxyDatasetClient implements DatasetClient {

    private final RestTemplate restTemplate;

    private static final MediaType AVRO_SCHEMA_MEDIA_TYPE = MediaType.valueOf(AvroUtils.AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE);

    private static final MediaType AVRO_MEDIA_TYPE = MediaType.valueOf(AvroUtils.AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE);

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
    public String findSchema(String datasetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(AVRO_SCHEMA_MEDIA_TYPE));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/datasets/{datasetId}/schema", GET, entity, String.class, datasetId);
        return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
    }

    @Override
    public String findBinaryAvroData(String datasetId, PageRequest pageRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(AVRO_MEDIA_TYPE));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/datasets/{datasetId}/content?offset={offset}&limit={limit}", GET, entity,
                        String.class, datasetId, pageRequest.getOffset(), pageRequest.getPageSize());
        return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
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

        private SecurityAuthorizationInterceptor(Security security) {
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
