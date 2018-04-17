package org.talend.dataprep.dataset.client;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/v1")
public class MockDatasetServer {

    @RequestMapping(value = "/datasets/{datasetId}", method = GET)
    public String getById() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("dataset_payload_example.json"), UTF_8);
    }

}
