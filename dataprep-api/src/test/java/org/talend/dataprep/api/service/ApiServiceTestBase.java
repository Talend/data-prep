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

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.Application;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.datepattern.DatePatternRepository;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;
import org.talend.dataprep.transformation.test.TransformationServiceUrlRuntimeUpdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Base test for all API service unit.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
public abstract class ApiServiceTestBase {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ApiServiceTestBase.class);

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    @Qualifier("ContentStore#local")
    protected DataSetContentStore contentStore;

    @Autowired
    protected PreparationRepository preparationRepository;

    @Autowired
    protected ContentCache cache;

    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected DatePatternRepository datePatternRepository;

    @Autowired
    TransformationServiceUrlRuntimeUpdater transformationUrlUpdater;

    protected Folder home;

    @Before
    public void setUp() {
        // Overrides connection information with random port value
        MockPropertySource connectionInformation = new MockPropertySource("tac properties")
                .withProperty("dataset.service.url", "http://localhost:" + port)
                .withProperty("transformation.service.url", "http://localhost:" + port)
                .withProperty("preparation.service.url", "http://localhost:" + port);

        environment.getPropertySources().addFirst(connectionInformation);

        transformationUrlUpdater.setUp();
        home = folderRepository.getHome();
    }

    @After
    public void tearDown() {
        dataSetMetadataRepository.clear();
        contentStore.clear();
        preparationRepository.clear();
        cache.clear();
        folderRepository.clear();
    }


    protected AggregationParameters getAggregationParameters(String input) throws IOException {
        InputStream parametersInput = this.getClass().getResourceAsStream(input);
        return mapper.readValue(parametersInput, AggregationParameters.class);
    }



    protected String createDataset(final String file, final String name, final String type) throws IOException {
        final String datasetContent = IOUtils.toString(PreparationAPITest.class.getResourceAsStream(file));
        final Response post = given() //
            .contentType(ContentType.JSON) //
            .body(datasetContent) //
            .queryParam("Content-Type", type) //
            .when() //
            .post("/api/datasets?name={name}", name);

        final int statusCode = post.getStatusCode();
        if(statusCode != 200) {
            LOGGER.error("Unable to create dataset (HTTP " + statusCode + "). Error: {}", post.asString());
        }
        assertThat(statusCode, is(200));
        final String dataSetId = post.asString();
        assertNotNull(dataSetId);
        assertThat(dataSetId, not(""));

        return dataSetId;
    }

    protected String createPreparationFromFile(final String file, final String name, final String type) throws IOException {
        final String dataSetId = createDataset(file, "testDataset-"+ System.currentTimeMillis(), type);
        return createPreparationFromDataset(dataSetId, name);
    }


    protected String createPreparationFromFile(final String file, final String name, final String type, final String folderId) throws IOException {
        final String dataSetId = createDataset(file, "testDataset-"+ System.currentTimeMillis(), type);
        return createPreparationFromDataset(dataSetId, name, folderId);
    }


    protected String createPreparationFromDataset(final String dataSetId, final String name) throws IOException {
        return createPreparationFromDataset(dataSetId, name, home.getId());
    }

    protected String createPreparationFromDataset(final String dataSetId, final String name, final String folderId) throws IOException {

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

    protected void applyActionFromFile(final String preparationId, final String actionFile) throws IOException {
        final String action = IOUtils.toString(PreparationAPITest.class.getResourceAsStream(actionFile));
        applyAction(preparationId, action);
    }

    protected void applyAction(final String preparationId, final String action) throws IOException {
        given().contentType(ContentType.JSON).body(action).when().post("/api/preparations/{id}/actions", preparationId).then()
                .statusCode(is(200));
    }
}
