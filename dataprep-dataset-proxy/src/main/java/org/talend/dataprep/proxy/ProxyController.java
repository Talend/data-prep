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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1")
public interface ProxyController {

    /**
     * Get dataset metadata
     * @param datasetId id of the dataset
     * @param withUiSpec Add UISpec to the returned json
     * @param advanced asks tcomp to add additionnal UISpec from the datastore
     * @return
     */
    @GetMapping("/datasets/{datasetId}")
    ResponseEntity<String> getDatasetMetadata(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced);

    /**
     * Get full dataset content
     * @param datasetId if of the dataset
     * @return
     */
    @GetMapping("/dataset-content/{datasetId}")
    ResponseEntity<String> getDatasetContent(@PathVariable String datasetId);

    /**
     * Get a dataset sample content
     * @param datasetId if of the dataset
     * @return
     */
    @GetMapping("/dataset-sample/{datasetId}")
    ResponseEntity<String> getDatasetSample(@PathVariable String datasetId);
}
