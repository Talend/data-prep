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

package org.talend.dataprep.preparation.test;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNABLE_TO_READ_CONTENT;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.FolderTreeNode;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.test.MockTDPException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Test client for preparation service.
 */
@Component
public class PreparationClientTest {

    /** Where the folders are stored. */
    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected PreparationRepository preparationRepository;

    @Autowired
    protected ObjectMapper mapper;

    private String homeFolderId;

    @PostConstruct
    private void init() {
        this.homeFolderId = folderRepository.getHome().getId();
    }

    static {
        RestAssured.defaultParser = Parser.JSON;
    }

    /**
     * Create a preparation by calling the preparation API.
     *
     * @param preparationContent preparation content in json.
     * @return the preparation id.
     */
    public String createPreparationWithAPI(final String preparationContent) {
        return createPreparationWithAPI(preparationContent, homeFolderId);
    }

    /**
     * Create a preparation by calling the preparation API.
     *
     * @param preparationContent preparation content in json.
     * @param folderId the folder id where tp create the preparation (can be null / empty)
     * @return the preparation id.
     */
    public String createPreparationWithAPI(final String preparationContent, final String folderId) {
        final Response response = given() //
                .contentType(JSON) //
                .body(preparationContent) //
                .queryParam("folderId", folderId) //
                .when() //
                .post("/preparations");

        assertThat(response.getStatusCode(), is(200));
        return response.asString();
    }

    /**
     * Append an action to a preparation.
     *
     * @param preparationId The preparation id.
     * @param stepContent The step content json.
     * @return The created stepContent id.
     */
    public String addStep(final String preparationId, final String stepContent) throws IOException {
        final Response post = given().body(stepContent)//
                .contentType(JSON)//
                .when()//
                .post("/preparations/{id}/actions", preparationId);

        assertEquals("fail to add step : " + post.statusLine(), 200, post.statusCode());
        final Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        return preparation.getHeadId();
    }

    /**
     * Append an action to a preparation.
     *
     * @param preparationId The preparation id.
     * @param stepToAppend The step to append.
     * @return The created stepContent id.
     */
    public void addStep(final String preparationId, AppendStep stepToAppend) {
        given().body(singletonList(stepToAppend)).contentType(ContentType.JSON).when()
                .post("/preparations/{id}/actions", preparationId).then().statusCode(200).log().ifError();
    }

    /**
     * @param preparationId the wanted preparation id.
     * @return the preparation details from its id.
     */
    public PreparationMessage getDetails(String preparationId) {
        return getDetails(preparationId, null);
    }

    /**
     * Return the details of a preparation at a given (optional) step.
     *
     * @param preparationId the wanted preparation id.
     * @param wantedStepId the optional wanted step id.
     * @return the details of a preparation at a given (optional) step.
     */
    public PreparationMessage getDetails(String preparationId, String wantedStepId) {
        final RequestSpecification specs = given();

        if (StringUtils.isNotBlank(wantedStepId)) {
            specs.queryParam("stepId", wantedStepId);
        }
        final Response response = specs.when().get("/preparations/{id}/details", preparationId);

        if (response.getStatusCode() != 200) {
            throw new MockTDPException(response);
        }

        try {
            return mapper.readerFor(PreparationMessage.class).readValue(response.asString());
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_CONTENT);
        }

    }

    /**
     * delete a preparation.
     *
     * @param preparationId the preparation id to delete.
     */
    public void deletePreparation(String preparationId) {
        when().delete("/preparations/{id}", preparationId).then().statusCode(OK.value());
    }

    public PreparationMessage getPreparation(String preparationId) {
        return when().get("/preparations/{id}/details", preparationId).as(PreparationMessage.class);
    }

    public PreparationMessage createPreparation(final Preparation preparation) {
        Response post = given().contentType(JSON).content(preparation).expect().statusCode(200).log().ifValidationFails()
                .post("/preparations?folderId={folderId}", homeFolderId);
        return getPreparation(post.asString());
    }

    /**
     *
     * @param preparation
     * @param folderId the id of folder where to c
     * @return the newly created preparation
     */
    public PreparationMessage createPreparation(final Preparation preparation, final String folderId) {
        Response post = given().contentType(JSON).content(preparation).expect().statusCode(200).log().ifValidationFails()
                .post("/preparations?folderId={folderId}", folderId);
        return getPreparation(post.asString());
    }

    public FolderTreeNode getFolderTree() {
        return get("/folders/tree").as(FolderTreeNode.class);
    }

    /**
     * Append an action to a preparation
     *
     * @param preparationId The preparation id
     * @param transformationJson The transformation json
     */
    public void applyTransformation(final String preparationId, final String transformationJson) throws IOException {
        given() //
                .body(transformationJson) //
                .contentType(ContentType.JSON) //
                .when() //
                .post("/preparations/{id}/actions", preparationId);
    }

    /**
     * Sets the preparation head step to given step ID.
     */
    public void setPreparationHead(String preparationId, String headId) {
        put("/preparations/{id}/head/{headId}", preparationId, headId);
    }

    public void deleteStep(String prepId, String stepIdToRemove) {
        when().delete("/preparations/{id}/actions/{action}", prepId, stepIdToRemove);
    }
}
