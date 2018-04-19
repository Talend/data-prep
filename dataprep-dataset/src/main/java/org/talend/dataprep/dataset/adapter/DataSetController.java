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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.dataset.domain.Dataset;

import java.util.List;

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
    public Dataset getDatasetMetadata(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        return datasetClient.findOne(datasetId);
    }

    @GetMapping
    public List<Dataset> getAllDatasetMetadata() {
        return datasetClient.findAll();
    }
}
