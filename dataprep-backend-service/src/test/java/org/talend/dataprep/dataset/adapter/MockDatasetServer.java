/*
 * ============================================================================
 *
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.dataprep.dataset.adapter.MockDatasetServer.AUTHENTICATION_TOKEN;

@RestController
@RequestMapping(value = "/api/v1", headers = HttpHeaders.AUTHORIZATION + "=" + AUTHENTICATION_TOKEN)
public class MockDatasetServer {

    public static final String AUTHENTICATION_TOKEN = "authentication-token";

    @GetMapping("/datasets/{datasetId}")
    public String getById(@RequestParam(required = false) Boolean withUiSpec,
            @RequestParam(required = false) Boolean advanced) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("dataset_payload_example.json"), UTF_8);
    }

    @GetMapping(value = "/dataset-sample/{datasetId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getSampleById(@RequestParam(defaultValue = "0") String offset,
            @RequestParam(defaultValue = "0") String limit) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(
                "api_v1_dataset-sample_8c0d7b05-ea9c-40e9-b506-cf828f255b6d_method_fetch.json"), UTF_8);
    }

}
