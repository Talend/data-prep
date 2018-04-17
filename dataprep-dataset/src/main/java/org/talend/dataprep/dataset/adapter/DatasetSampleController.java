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

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.dataset.client.DatasetClient;
import org.talend.dataprep.dataset.client.domain.EncodedSample;

@RestController
@RequestMapping("/api/v1/dataset-sample")
public class DatasetSampleController {

    private final DatasetClient datasetClient;

    public DatasetSampleController(DatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<EncodedSample> getDatasetSample(@PathVariable String datasetId,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "0") int size) {
        return new ResponseEntity<>(datasetClient.findSample(datasetId, new PageRequest(offset, size)), HttpStatus.OK);
    }

}
