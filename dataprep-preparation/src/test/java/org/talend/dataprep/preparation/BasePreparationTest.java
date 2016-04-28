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

package org.talend.dataprep.preparation;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Base class for all Preparation unit/integration tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public abstract class BasePreparationTest {

    @Value("${local.server.port}")
    public int port;

    /** The root step. */
    @Resource(name = "rootStep")
    protected Step rootStep;

    @Autowired
    protected PreparationRepository repository;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected VersionService versionService;

    /** Where the folders are stored.*/
    @Autowired
    protected FolderRepository folderRepository;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
        folderRepository.clear();
    }

    /**
     * Create a preparation by calling the preparation API.
     *
     * @param preparationContent preparation content in json.
     * @return the preparation id.
     */
    protected String createPreparationWithAPI(String preparationContent) {
        return createPreparationWithAPI(preparationContent, null);
    }

    /**
     * Create a preparation by calling the preparation API.
     *
     * @param preparationContent preparation content in json.
     * @param folderPath the folder path where tp create the preparation (can be null / empty)
     * @return the preparation id.
     */
    protected String createPreparationWithAPI(String preparationContent, String folderPath) {
        final Response response = given() //
                .contentType(ContentType.JSON) //
                .body(preparationContent) //
                .queryParam("folder", folderPath) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations");

        assertThat(response.getStatusCode(), is(200));
        return response.asString();
    }
}
