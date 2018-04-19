package org.talend.dataprep.dataset.adapter;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;

import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

public class TotoTest {

    private static final Logger LOGGER = getLogger(TotoTest.class);

    @Test
    public void name() throws URISyntaxException {
        String datasetId = "ahahahahha";
        PageRequest pageRequest = new PageRequest(0, 10);

        URIBuilder builder = new URIBuilder("/dataset-sample/" + datasetId) //
                .addParameter("offset", Integer.toString(pageRequest.getOffset())) //
                .addParameter("limit", Integer.toString(pageRequest.getPageSize()));
        LOGGER.warn("url: " + builder.build());
    }
}
