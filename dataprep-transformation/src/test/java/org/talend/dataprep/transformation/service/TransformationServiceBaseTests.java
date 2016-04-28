//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.test.TransformationServiceUrlRuntimeUpdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Base class for TransformationService integration tests.
 */
public abstract class TransformationServiceBaseTests extends TransformationBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationServiceBaseTests.class);

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    private TransformationServiceUrlRuntimeUpdater urlUpdater;

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    /** The dataprep ready to use jackson object mapper. */
    @Autowired
    protected ObjectMapper mapper;

    @Before
    public void setUp() {

        // Overrides connection information with random port value
        MockPropertySource connectionInformation = new MockPropertySource()
                .withProperty("dataset.service.url", "http://localhost:" + port)
                .withProperty("transformation.service.url", "http://localhost:" + port)
                .withProperty("preparation.service.url", "http://localhost:" + port);
        environment.getPropertySources().addFirst(connectionInformation);

        urlUpdater.setUp();
    }

    @After
    public void cleanUp() {
        dataSetMetadataRepository.clear();
    }

    protected String createDataset(final String file, final String name, final String type) throws IOException {

        try {
            Thread.sleep(250L); // a little pause is needed otherwise an error UNKNOWN_DATASET_CONTENT may sometime
            // happen
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        final String datasetContent = IOUtils.toString(this.getClass().getResourceAsStream(file));
        final Response post = given() //
                .contentType(ContentType.JSON) //
                .body(datasetContent) //
                .queryParam("Content-Type", type) //
                .queryParam("name", name) //
                .expect().statusCode(200).log().ifError() //
                .when() //
                .post("/datasets");

        assertThat(post.getStatusCode(), is(200));
        final String dataSetId = post.asString();
        assertNotNull(dataSetId);
        assertThat(dataSetId, not(""));

        return dataSetId;

    }

    protected String createEmptyPreparationFromDataset(final String dataSetId, final String name) throws IOException {
        final Response post = given() //
                .contentType(ContentType.JSON)//
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\"}")//
                .when()//
                .expect().statusCode(200).log().ifError() //
                .post("/preparations");

        assertThat(post.getStatusCode(), is(200));

        final String preparationId = post.getBody().asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    protected void applyActionFromFile(final String preparationId, final String actionFile) throws IOException {
        final String action = IOUtils.toString(this.getClass().getResourceAsStream(actionFile));
        applyAction(preparationId, action);
    }

    protected void applyAction(final String preparationId, final String action) throws IOException {
        given().contentType(ContentType.JSON) //
                .body(action) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations/{id}/actions", preparationId) //
                .then() //
                .statusCode(is(200));
    }

    protected Preparation getPreparation(final String preparationId) throws IOException {
        final String json = given().when() //
                .expect().statusCode(200).log().ifError() //
                .get("/preparations/{id}", preparationId) //
                .asString();

        return mapper.readerFor(Preparation.class).readValue(json);

    }
}
