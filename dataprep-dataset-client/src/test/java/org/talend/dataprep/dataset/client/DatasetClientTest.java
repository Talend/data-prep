package org.talend.dataprep.dataset.client;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertNotNull;

public class DatasetClientTest extends TestParent {

    @Autowired
    private DatasetClient datasetClient;

    @Before
    public void updateClientUrl() {
        ReflectionTestUtils.setField(datasetClient, "datasetApiUrl", "http://localhost:" + localServerPort + "/api/v1");
    }

    @Test
    public void getByIdTest() {
        Dataset toto = datasetClient.findOne("toto");
        assertNotNull(toto);
    }

    @Test
    public void test() {
        RestAssured.port = localServerPort;
        RestAssured
                .get("/api/v1/datasets/{datasetId}", 10)
        .then().statusCode(200);
    }

}
