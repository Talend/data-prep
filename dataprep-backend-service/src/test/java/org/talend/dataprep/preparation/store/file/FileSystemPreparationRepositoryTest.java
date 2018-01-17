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

package org.talend.dataprep.preparation.store.file;

import static org.junit.Assert.*;
import static org.talend.tql.api.TqlBuilder.contains;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.preparation.PreparationTest;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepositoryTest;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Unit test for the FileSystemPreparationRepository.
 *
 * @see FileSystemPreparationRepositoryTest
 */
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "preparation.store=file",
        "preparation.store.file.location=target/test/store/preparation" })
public class FileSystemPreparationRepositoryTest extends PreparationRepositoryTest {

    @Autowired
    private VersionService versionService;

    /** The preparation repository to test. */
    @Autowired
    private PreparationRepository repository;

    /**
     * Clean up repository after each test.
     */
    @After
    public void cleanUpAfterTests() {
        repository.clear();
    }

    @Test
    public void shouldGetPreparationThatWasAdded() throws JsonProcessingException {

        // given
        Preparation expected = getPreparation("7561486");
        repository.add(expected);

        // when
        final Preparation actual = repository.get(expected.id(), Preparation.class);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStepThatWasAdded() {
        final Step expected = new Step(Step.ROOT_STEP.id(), "684fdqs638", versionService.version().getVersionId());
        repository.add(expected);
        final Step actual = repository.get(expected.id(), Step.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldListActionThatWasAdded() {
        final List<Action> actions = PreparationTest.getSimpleAction("uppercase", "column_name", "lastname");
        PreparationActions expected = new PreparationActions(actions, versionService.version().getVersionId());

        repository.add(expected);
        final PreparationActions actual = repository.get(expected.id(), PreparationActions.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetOnlyWantedClass() {
        final Step expected = new Step(Step.ROOT_STEP.id(), "8rq4868", versionService.version().getVersionId());
        repository.add(expected);
        assertNull(repository.get(expected.id(), Preparation.class));
        assertNull(repository.get(expected.id(), PreparationActions.class));
    }

    @Test
    public void shouldRemove() {
        final Preparation preparation = getPreparation("id#8486343");
        repository.add(preparation);
        repository.remove(preparation);
        assertNull(repository.get(preparation.id(), Preparation.class));
    }

    @Test
    public void shouldOverwriteContent() {
        Preparation expected = getPreparation("khdgf");
        expected.setName("old name");
        repository.add(expected);
        expected.setName("new name");
        repository.add(expected);
        final Preparation actual = repository.get(expected.id(), Preparation.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldListOnlyPreparationsContainingGivenName() {
        final List<Integer> ids = Arrays.asList(1, 12, 3);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.forEach(prep -> repository.add(prep));

        // and some steps to add some noise
        ids.stream().map(i -> new Step(Step.ROOT_STEP.id(), "step" + i, versionService.version().getVersionId())) //
                .forEach(step -> repository.add(step));

        // get some preparation by dataset id
        final Preparation expected1 = preparations.get(0);
        final Preparation expected2 = preparations.get(1);
        final Collection<Preparation> actual = repository.list(Preparation.class, contains("name", "1")).collect(Collectors.toList());

        assertEquals(2, actual.size());
        assertTrue(actual.contains(expected1));
        assertTrue(actual.contains(expected2));

    }

    @Test
    public void shouldListOnlyPreparationsExactlyMatchingGivenName() {
        final List<Integer> ids = Arrays.asList(1, 12, 3);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.forEach(prep -> repository.add(prep));

        // and some steps to add some noise
        ids.stream().map(i -> new Step(Step.ROOT_STEP.id(), "step" + i, versionService.version().getVersionId())) //
                .forEach(step -> repository.add(step));

        // get some preparation by dataset id
        final Preparation expected = preparations.get(0);
        final Collection<Preparation> actual = repository.list(Preparation.class, eq("name", "prep-1")).collect(Collectors.toList());

        assertEquals(1, actual.size());
        assertTrue(actual.contains(expected));
    }


    /**
     * @param datasetId the preparation id.
     * @return a preparation with a root step an a the given dataset id.
     */
    @Override
    protected Preparation getPreparation(String datasetId) {
        Preparation preparation = new Preparation(UUID.randomUUID().toString(), datasetId, Step.ROOT_STEP.id(), versionService.version().getVersionId());
        preparation.setName("prep-" + datasetId);
        return preparation;
    }

    @Override
    public PreparationRepository getRepository() {
        return repository;
    }
}
