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

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.util.avro.AvroUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasets")
public class DataSetController {

    private final DatasetClient datasetClient;

    public DataSetController(DatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    @GetMapping
    public List<Dataset> getAllDatasetMetadata() {
        return datasetClient.findAll();
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

    @GetMapping(value = "/{datasetId}/schema", produces = AvroUtils.AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE)
    public String getDatasetSchema(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        return datasetClient.findSchema(datasetId);
    }

    @GetMapping(value = "/{datasetId}/content", produces = AvroUtils.AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE)
    public Resource getDatasetContent(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        return new InputStreamResource(
                datasetClient.findBinaryAvroData(datasetId, new PageRequest(0, Integer.MAX_VALUE)));
    }

}
