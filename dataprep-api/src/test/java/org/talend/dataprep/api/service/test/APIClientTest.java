// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.PreparationAPITest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Test client for data-prep API.
 */
@Component
public class APIClientTest {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(APIClientTest.class);

    @Autowired
    protected ObjectMapper mapper;

    /**
     * Create a dataset.
     *
     * @param file the classpath of the file to upload.
     * @param name the dataset name.
     * @param type the dataset type.
     * @return the dataset id.
     * @throws IOException sh*t happens.
     */
    public String createDataset(final String file, final String name, final String type) throws IOException {
        final InputStream resourceAsStream = PreparationAPITest.class.getResourceAsStream(file);
        assertNotNull(resourceAsStream);
        final String datasetContent = IOUtils.toString(resourceAsStream, "UTF-8");
        final Response post = given() //
                .contentType(ContentType.JSON) //
                .body(datasetContent) //
                .queryParam("Content-Type", type) //
                .when() //
                .post("/api/datasets?name={name}", name);

        final int statusCode = post.getStatusCode();
        if (statusCode != 200) {
            LOGGER.error("Unable to create dataset (HTTP " + statusCode + "). Error: {}", post.asString());
        }
        assertThat(statusCode, is(200));
        final String dataSetId = post.asString();
        assertNotNull(dataSetId);
        assertThat(dataSetId, not(""));

        return dataSetId;
    }

    /**
     * Create an empty preparation from a dataset.
     *
     * @param file the dataset classpath file to upload.
     * @param name the preparation name.
     * @param type the dataset type.
     * @param folderId where to create the preparation.
     * @return the preparation id.
     * @throws IOException sh*i happens.
     */
    public String createPreparationFromFile(final String file, final String name, final String type, final String folderId)
            throws IOException {
        final String dataSetId = createDataset(file, "testDataset-" + UUID.randomUUID(), type);
        return createPreparationFromDataset(dataSetId, name, folderId);
    }

    /**
     * Create an empty preparation from a dataset.
     *
     * @param dataSetId the dataset id to create the preparation from.
     * @param name the preparation name.
     * @param folderId where to create the preparation.
     * @return the preparation id.
     * @throws IOException sh*t happens.
     */
    public String createPreparationFromDataset(final String dataSetId, final String name, final String folderId)
            throws IOException {

        RequestSpecification request = given() //
                .contentType(ContentType.JSON) //
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\"}");

        if (folderId != null) {
            request = request.queryParam("folder", folderId);
        }

        final Response response = request //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/api/preparations");

        assertThat(response.getStatusCode(), is(200));

        final String preparationId = response.asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param actionFile the classpath of the step to add.
     * @throws IOException sh*t happens.
     */
    public void applyActionFromFile(final String preparationId, final String actionFile) throws IOException {
        final String action = IOUtils.toString(PreparationAPITest.class.getResourceAsStream(actionFile), "UTF-8");
        applyAction(preparationId, action);
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param action the content of the step to add.
     * @throws IOException sh*t happens.
     */
    public void applyAction(final String preparationId, final String action) throws IOException {
        given().contentType(ContentType.JSON).body(action).when().post("/api/preparations/{id}/actions", preparationId).then()
                .statusCode(is(200));
    }

}
