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

package org.talend.dataprep.preparation.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.preparation.BasePreparationTest;

import com.netflix.hystrix.HystrixCommandProperties;

/**
 * Unit/integration tests for the PreparationService
 */
public class PreparationServiceTest extends BasePreparationTest {

    /**
     * Where the folders are stored.
     */
    @Autowired
    private PreparationService preparationService;

    @Test
    public void testListAllShouldOnlyTakeInAccountPathIfPresentEvenIfNameAndFolderPathAreWrong() throws Exception {
        init();
        // then : path should override other props
        assertThat(preparationService
                .listAll("dont_exist", "wrong_folder_path", "/foo/prep_name_foo", null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testUnnamedPreparation() throws Exception {
        init();
        // then : path should override other props
        List<PreparationDTO> preparations = preparationService.listAll(null, "unnamedPreparation", null, null, null) //
                .collect(Collectors.toList());
        assertThat(preparations.size(), is(1));
        assertThat(preparations.get(0).getName(), is("Preparation"));
    }

    @Test
    public void testListAllShouldListWhenPathDoesNotContainsAnySlash() throws Exception {
        init();
        // then : path doesn't contain "/"
        assertThat(preparationService
                .listAll(null, null, "prep_name_home", null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldListWhenPathIsOneLevelAndStartsWithASlash() throws Exception {
        init();
        // then : path is one level and starts with a "/"
        assertThat(preparationService
                .listAll(null, null, "/prep_name_home", null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldNotListWhenNameAndFolderPathPointsToAnEmptyFolder() throws Exception {
        init();
        // then : path where there is no preparation
        assertThat(preparationService
                .listAll("dont_exist", "wrong_folder_path", null, null, null) //
                .collect(Collectors.toList())
                .size(), is(0));
    }

    @Test
    public void testListAllShouldListWhenNameAndFolderPathPointsToAFolderContainingPreparations() throws Exception {
        init();
        // then : should be the normal behaviour without path parameter
        assertThat(preparationService
                .listAll("prep_name_home", "/", null, null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldListWhenNameAndFolderPathPointsToASubFolderContainingPreparations() throws Exception {
        init();
        // then : should be the normal behaviour without path parameter
        assertThat(preparationService
                .listAll("prep_name_foo", "/foo", null, null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldListWhenPathPointsToASubFolderAndDoesNotStartWithASlash() throws Exception {
        init();
        // then : : should list if path doesn't start with "/"
        assertThat(
                preparationService
                        .listAll(null, null, "foo/prep_name_foo", null, null)
                        .collect(Collectors.toList())
                        .size(),
                is(1));
    }

    @Test
    public void testListAllShouldListWhenPathPointsToASubFolderAndStartsWithASlash() throws Exception {
        init();
        // then : should list if path starts with "/"
        assertThat(preparationService
                .listAll(null, null, "/foo/prep_name_foo", null, null) //
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldListWhenPreparationNameContainsSpecialCharactersAsInTDP4779() throws Exception {
        init();
        // then : : should list preparation with special character in preparation (see
        // https://jira.talendforge.org/browse/TDP-4779)
        assertThat(preparationService
                .listAll(null, null, "foo/Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv", null, null)
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void testListAllShouldListWhenFolderAndPreparationNameContainsSpecialCharactersAsInTDP4779()
            throws Exception {
        init();
        // then : : should list preparation with special character in folder and preparation (see
        // https://jira.talendforge.org/browse/TDP-4779)
        assertThat(preparationService
                .listAll(null, null,
                        "Folder Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv/Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv",
                        null, null)
                .collect(Collectors.toList())
                .size(), is(1));
    }

    @Test
    public void setHeadShouldCleanStepList() throws IOException {

        // get a prep
        Preparation preparation = new Preparation();
        preparation.setName("prep_name_foo");
        preparation.setDataSetId("1234");
        preparation.setRowMetadata(new RowMetadata());
        PreparationDTO prep = clientTest.createPreparation(preparation, home.getId());

        final String step =
                IOUtils.toString(this.getClass().getResourceAsStream("actions/append_lower_case.json"), UTF_8);
        for (int i = 0; i < 5; i++) {
            clientTest.addStep(prep.getId(), step);
        }

        prep = clientTest.getPreparation(prep.getId());

        List<String> originalStepIds = prep.getSteps();

        updateHeadAndCheckResult(prep, originalStepIds, 0);
        updateHeadAndCheckResult(prep, originalStepIds, 3);
        updateHeadAndCheckResult(prep, originalStepIds, 2);
    }

    private void updateHeadAndCheckResult(PreparationDTO prep, List<String> originalStepIds, Integer indexOfStep) {
        preparationService.setPreparationHead(prep.getId(), originalStepIds.get(indexOfStep));

        PreparationDTO updatedPrep = preparationService.getPreparation(prep.getId());

        // we check that headId is the correct one and there is only one step on the list
        assertNotNull(updatedPrep);
        assertEquals(originalStepIds.get(indexOfStep), updatedPrep.getHeadId());
        assertEquals(indexOfStep + 1, updatedPrep.getSteps().size());
        assertEquals(originalStepIds.get(indexOfStep), updatedPrep.getSteps().get(indexOfStep));
    }

    private void init() throws IOException {
        HystrixCommandProperties.Setter().withCircuitBreakerEnabled(false);
        createFolder(home.getId(), "foo");
        createFolder(home.getId(), "unnamedPreparation");
        final Folder foo = getFolder(home.getId(), "foo");
        final Folder unnamedPreparation = getFolder(home.getId(), "unnamedPreparation");

        createFolder(home.getId(), "Folder Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv");
        final Folder specialChar =
                getFolder(home.getId(), "Folder Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv");

        Preparation preparation = new Preparation();
        preparation.setName("prep_name_foo");
        preparation.setDataSetId("1234");
        preparation.setRowMetadata(new RowMetadata());
        clientTest.createPreparation(preparation, foo.getId());

        preparation.setName("prep_name_home");
        clientTest.createPreparation(preparation, home.getId());

        preparation.setName("Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv");
        clientTest.createPreparation(preparation, foo.getId());

        preparation.setName("Cr((eate Email A!ddressrrrbb[zzzz (copie-é'(-è_çà)+&.csv");
        clientTest.createPreparation(preparation, specialChar.getId());

        preparation.setName(null);
        clientTest.createPreparation(preparation, unnamedPreparation.getId());
    }
}
