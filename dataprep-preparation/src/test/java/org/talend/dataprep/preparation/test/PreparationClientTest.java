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

package org.talend.dataprep.preparation.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

import com.jayway.restassured.response.Response;

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

    private String homeFolderId;

    @PostConstruct
    private void init() {
        this.homeFolderId = folderRepository.getHome().getId();
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

}
