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

package org.talend.dataprep.dataset.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.talend.dataprep.dataset.client.domain.Dataset;
import org.talend.dataprep.dataset.client.domain.EncodedSample;
import org.talend.dataprep.dataset.client.properties.DatasetProperties;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;

@Service
public class ProxyDatasetClient implements DatasetClient {

    private final RestTemplate restTemplate;

    private final AvroMapper avroMapper;

    public ProxyDatasetClient(RestTemplateBuilder builder, DatasetProperties datasetProperties) {
        String dataSetUrl = datasetProperties.getUrl().toString();
        this.restTemplate = builder.rootUri(dataSetUrl).build();
        this.avroMapper = new AvroMapper();
    }

    @Override
    public Dataset findOne(String datasetId) {
        ResponseEntity<Dataset> entity =
                restTemplate.getForEntity("/api/v1/datasets/{datasetId}?withUiSpec={withUiSpec}&advanced={advanced}",
                        Dataset.class, datasetId, false, false);
        return entity.getBody();
    }

    @Override
    public ObjectNode findSample(String datasetId, int offset, int size) {
        return restTemplate.getForEntity("/api/v1/dataset-sample/{datasetId}?offset={offset}&size={size}",
                EncodedSample.class, datasetId, offset, size) //
                .getBody() //
                .getSchema();
    }

    @Override
    public List<Dataset> findAll() {
        ResponseEntity<Dataset[]> entity = restTemplate.getForEntity("/api/v1/datasets", Dataset[].class);
        return Arrays.asList(entity.getBody());
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
}
