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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.dataset.client.domain.Dataset;
import org.talend.dataprep.dataset.client.DatasetClient;
import org.talend.dataprep.dataset.client.domain.EncodedSample;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;

@RestController
@RequestMapping("/api/v1/datasets")
public class DataSetController {

    private final DatasetClient datasetClient;

    public DataSetController(DatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    /**
     * Get dataset by id
     * @param datasetId id of the dataset
     * @param withUiSpec Add UISpec to the returned json
     * @param advanced asks tcomp to add additionnal UISpec from the datastore
     * @return
     */
    @GetMapping("/{datasetId}")
    public ResponseEntity<Dataset> getDatasetMetadata(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        Dataset dataset = datasetClient.findOne(datasetId);

        return new ResponseEntity<>(dataset, HttpStatus.OK);
    }
}
