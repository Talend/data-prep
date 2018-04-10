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

package org.talend.dataprep.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@ConditionalOnProperty(name = "dataset.type", havingValue = "legacy")
public class DataSetLegacyController implements ProxyController {

    private final RestTemplate restTemplate;

    public DataSetLegacyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<String> getDatasetMetadata(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        return restTemplate.getForEntity("/datasets/{datasetId}/metadata", String.class, datasetId);
    }

    @Override
    public ResponseEntity<String> getDatasetContent(@PathVariable String datasetId) {
        return restTemplate.getForEntity("/datasets/{datasetId}/content", String.class, datasetId);
    }

    @Override
    public ResponseEntity<String> getDatasetSample(@PathVariable String datasetId) {
        return restTemplate.getForEntity("/datasets/{datasetId}", String.class, datasetId);
    }
}
