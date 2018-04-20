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

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.talend.dataprep.dataset.adapter.ProxyDatasetClientTest.AUTHENTICATION_TOKEN;

@RestController
@RequestMapping(value = "/api/v1", headers = HttpHeaders.AUTHORIZATION + "=" + AUTHENTICATION_TOKEN)
public class MockDatasetServer {

    @RequestMapping(value = "/datasets/{datasetId}", method = GET)
    public String getById(@RequestParam(required = false) Boolean withUiSpec,
            @RequestParam(required = false) Boolean advanced) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("dataset_payload_example.json"), UTF_8);
    }

    @RequestMapping(value = "/dataset-sample/{datasetId}", method = GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getSampleById(@RequestParam(defaultValue = "0") String offset,
            @RequestParam(defaultValue = "0") String limit) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(
                "api_v1_dataset-sample_8c0d7b05-ea9c-40e9-b506-cf828f255b6d_method_fetch.json"), UTF_8);
    }

}
