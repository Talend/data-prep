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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.lock.store.LockedResourceRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Base class for all Preparation unit/integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
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

    @Autowired
    protected LockedResourceRepository lockRepository;

    /** the HOME folder */
    protected Folder home;

    @Before
    public void setUp() {
        RestAssured.port = port;
        home = folderRepository.getHome();
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
    protected String createPreparationWithAPI(final String preparationContent) {
        return createPreparationWithAPI(preparationContent, home.getId());
    }

    /**
     * Create a preparation by calling the preparation API.
     *
     * @param preparationContent preparation content in json.
     * @param folderId the folder id where tp create the preparation (can be null / empty)
     * @return the preparation id.
     */
    protected String createPreparationWithAPI(final String preparationContent, final String folderId) {
        final Response response = given() //
                .contentType(ContentType.JSON) //
                .body(preparationContent) //
                .queryParam("folder", folderId) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations");

        assertThat(response.getStatusCode(), is(200));
        return response.asString();
    }
}
