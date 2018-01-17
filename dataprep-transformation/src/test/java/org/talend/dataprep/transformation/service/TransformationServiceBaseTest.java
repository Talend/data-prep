// ============================================================================
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

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.test.SpringLocalizationRule;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.test.TransformationServiceUrlRuntimeUpdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Base class for TransformationService integration tests.
 */
public abstract class TransformationServiceBaseTest extends TransformationBaseTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TransformationServiceBaseTest.class);

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    protected FolderRepository folderRepository;

    protected Folder home;

    /** The dataprep ready to use jackson object mapper. */
    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    private TransformationServiceUrlRuntimeUpdater urlUpdater;

    @Rule
    public SpringLocalizationRule rule = new SpringLocalizationRule(Locale.US);

    @Before
    public void setUp() {
        super.setUp();
        urlUpdater.setUp();
        home = folderRepository.getHome();
    }

    @After
    public void cleanUp() {
        folderRepository.clear();
        dataSetMetadataRepository.clear();
    }

    protected String createFolder(final String path) throws IOException {
        final Folder folder = folderRepository.addFolder(home.getId(), path);
        return folder.getId();
    }

    protected String createDataset(final String file, final String name, final String type) throws IOException {

        try {
            Thread.sleep(250L); // a little pause is needed otherwise an error UNKNOWN_DATASET_CONTENT may sometime
            // happen
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        final String datasetContent = IOUtils.toString(this.getClass().getResourceAsStream(file), UTF_8);
        final Response post = given() //
                .contentType(type) //
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

    protected DataSetMetadata getMetadata(final String dataSetId) throws IOException {

        final Response get = given() //
                .contentType(ContentType.JSON) //
                .expect().statusCode(200).log().ifError() //
                .when() //
                .get("/datasets/{id}/metadata", dataSetId);

        final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(get.asInputStream());
        assertNotNull(dataSet);

        return dataSet.getMetadata();
    }

    protected DataSet getDataset(final String dataSetId) throws IOException {

        final Response get = given() //
                .contentType(ContentType.JSON) //
                .expect().statusCode(200).log().ifError() //
                .when() //
                .get("/datasets/{id}/content", dataSetId);

        final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(get.asInputStream());
        assertNotNull(dataSet);

        return dataSet;

    }

    protected String createEmptyPreparationFromDataset(final String dataSetId, final String name) throws IOException {
        return this.createEmptyPreparationFromDataset(home.getId(), dataSetId, name);
    }

    protected String createEmptyPreparationFromDataset(final String folderId, final String dataSetId, final String name)
            throws IOException {
        final Response post = given() //
                .contentType(ContentType.JSON)//
                .accept(ContentType.ANY) //
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\", \"rowMetadata\":{\"columns\":[]}}")//
                .when()//
                .post("/preparations?folderId=" + folderId);

        assertThat(post.getStatusCode(), is(200));

        final String preparationId = post.getBody().asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    protected String createEmptyPreparationFromDatasetWithMetadata(final String dataSetId, final String name) throws IOException {

        RowMetadata rowMetadata = getMetadata(dataSetId).getRowMetadata();
        String metadata = mapper.writeValueAsString(rowMetadata);
        final Response post = given() //
                .contentType(ContentType.JSON)//
                .accept(ContentType.ANY) //
                .body("{ \"name\": \"" + name + "\", \"dataSetId\": \"" + dataSetId + "\", \"rowMetadata\":" + metadata + "}")//
                .when()//
                .post("/preparations?folderId=" + home.getId());

        assertThat(post.getStatusCode(), is(200));

        final String preparationId = post.getBody().asString();
        assertThat(preparationId, notNullValue());
        assertThat(preparationId, not(""));

        return preparationId;
    }

    protected void applyActionFromFile(final String preparationId, final String actionFile) throws IOException {
        final String action = IOUtils.toString(this.getClass().getResourceAsStream(actionFile), UTF_8);
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
                .get("/preparations/{id}/details", preparationId) //
                .asString();

        return mapper.readerFor(Preparation.class).readValue(json);

    }
}
