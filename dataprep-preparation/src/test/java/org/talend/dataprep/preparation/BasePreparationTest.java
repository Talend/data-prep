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

package org.talend.dataprep.preparation;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.lock.store.LockedResourceRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.test.PreparationClientTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * Base class for all Preparation unit/integration tests.
 */
public abstract class BasePreparationTest extends ServiceBaseTest {

    @Autowired
    protected PreparationRepository repository;

    @Autowired
    protected VersionService versionService;

    /** Where the folders are stored. */
    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected LockedResourceRepository lockRepository;

    @Autowired
    protected PreparationClientTest clientTest;

    /** the HOME folder */
    protected Folder home;

    @Before
    public void setUp() {
        super.setUp();
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
                .queryParam("folderId", folderId) //
                .when() //
                .expect().statusCode(200).log().ifError() //
                .post("/preparations");

        assertThat(response.getStatusCode(), is(200));
        return response.asString();
    }

    /**
     * Create a new preparation via the PreparationService.
     *
     * @param datasetId the dataset id related to this preparation.
     * @param name preparation name.
     * @param numberOfColumns the wanted number of columns in the preparation row metadata.
     * @return The preparation id
     */
    protected String createPreparationFromService(final String datasetId, final String name, int numberOfColumns) {
        final Preparation preparation = new Preparation(UUID.randomUUID().toString(), datasetId, Step.ROOT_STEP.id(),
                versionService.version().getVersionId());
        preparation.setName(name);
        preparation.setCreationDate(0);
        RowMetadata rowMetadata = new RowMetadata();
        for (int i = 0; i < numberOfColumns; i++) {
            rowMetadata.addColumn(new ColumnMetadata());
        }
        preparation.setRowMetadata(rowMetadata);
        repository.add(preparation);
        return preparation.id();
    }

    protected void createFolder(final String parentId, final String path) {
        given().queryParam("parentId", parentId) //
                .queryParam("path", path) //
                .expect().statusCode(200).log().ifError()//
                .when() //
                .put("/folders").then().assertThat().statusCode(200);
    }

    protected Folder getFolder(final String parentId, final String name) throws IOException {
        return getFolderChildren(parentId).stream().filter((folder) -> folder.getName().equals(name)).findFirst().orElse(null);
    }

    protected List<Folder> getFolderChildren(final String id) throws IOException {
        final Response response = given() //
                .when() //
                .get("/folders?parentId={parentId}", id);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        return mapper.readValue(response.asString(), new TypeReference<List<Folder>>() {
        });
    }
}
