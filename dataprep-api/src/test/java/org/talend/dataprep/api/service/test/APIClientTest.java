// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import static com.jayway.restassured.http.ContentType.TEXT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.MixedContentMap;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.PreparationAPITest;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
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

    public List<UserDataSetMetadata> listDataSets(String name) throws IOException {
        InputStream result;
        if (name == null) {
            result = when().get("/api/datasets").asInputStream();
        }else {
            result = when().get("/api/datasets?name={name}", name).asInputStream();
        }
        CollectionType resultType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class,
                UserDataSetMetadata.class);
        return mapper.readValue(result, resultType);
    }

    /**
     * Create a dataset.
     *
     * @param file the classpath of the file to upload.
     * @param name the dataset name.
     * @return the dataset id.
     * @throws IOException sh*t happens.
     */
    public String createDataset(final String file, final String name) throws IOException {
        final InputStream resourceAsStream = PreparationAPITest.class.getResourceAsStream(file);
        assertNotNull(resourceAsStream);
        final String datasetContent = IOUtils.toString(resourceAsStream, UTF_8);
        final Response post = given() //
                .contentType(TEXT) //
                .body(datasetContent) //
                .queryParam("name", name) //
                .when() //
                .post("/api/datasets");

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
     * @param folderId where to create the preparation.
     * @return the preparation id.
     * @throws IOException sh*i happens.
     */
    public String createPreparationFromFile(final String file, final String name, final String folderId)
            throws IOException {
        final String dataSetId = createDataset(file, "testDataset-" + UUID.randomUUID());
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
                .contentType(JSON) //
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
        final String action = IOUtils.toString(PreparationAPITest.class.getResourceAsStream(actionFile), UTF_8);
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
        given().contentType(JSON).body(action).when().post("/api/preparations/{id}/actions", preparationId).then()
                .statusCode(is(200));
    }

    /**
     * Add a step to a preparation.
     *
     * @param preparationId the preparation id.
     * @param actionName action name
     * @param parameters action parameters
     * @throws IOException sh*t happens.
     */
    public void applyAction(final String preparationId, final String actionName, Map<String, String> parameters) throws IOException {
        org.talend.dataprep.api.preparation.Actions actions = new org.talend.dataprep.api.preparation.Actions();
        Action action = new Action();
        action.setName(actionName);
        action.setParameters(MixedContentMap.convert(parameters));
        actions.setActions(Collections.singletonList(action));
        given().contentType(JSON.withCharset(UTF_8)).content(actions) //
                .when().post("/api/preparations/{id}/actions", preparationId) //
                .then().statusCode(is(200));
    }

    /**
     * Fetch the preparation metadata.
     *
     * @param preparationId id of the preparation to fetch
     * @return the preparation details
     * @throws IOException if a connexion or parsing error happen
     */
    public Preparation getPreparation(String preparationId) throws IOException {
        String json = //
                expect() //
                        .statusCode(200).log().ifValidationFails() //
                        .when() //
                        .get("/api/preparations/{id}/details", preparationId).asString();
        return mapper.readerFor(Preparation.class).readValue(json);
    }

    /**
     * Fetch preparation results and extract metadata produced by the preparation.
     *
     * @param id preparation ID
     * @return metadata produced by the application of the preparation
     */
    public RowMetadata getPreparationContent(String id) throws IOException {
        InputStream inputStream = given().get("/api/preparations/{prepId}/content?version={version}&from={stepId}", id, "head",
                "HEAD").asInputStream();

        mapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper.readValue(inputStream, Data.class).metadata;
    }

    private static class Data {
        public RowMetadata metadata;
    }

}
