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

import org.apache.avro.generic.IndexedRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/dataset-sample")
public class DatasetSampleController {

    private final DatasetClient datasetClient;

    public DatasetSampleController(DatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    @GetMapping("/{datasetId}")
    public Stream<IndexedRecord> getDatasetSample(@PathVariable String datasetId,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "1") int limit) {
        return datasetClient.findData(datasetId, new PageRequest(offset, limit));
    }

}
