package org.talend.dataprep.preparation.store.file;

import static org.junit.Assert.*;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.PreparationTest;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Unit test for the FileSystemPreparationRepository.
 * 
 * @see FileSystemPreparationRepositoryTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileSystemPreparationRepositoryTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "preparation.store=file",
        "preparation.store.file.location=target/test/store/preparation" })
public class FileSystemPreparationRepositoryTest {

    /**
     * Bean needed to resolve test properties set by the @TestPropertySource annotation
     * 
     * @see FileSystemPreparationRepository#preparationsLocation
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /** The preparation repository to test. */
    @Autowired
    private FileSystemPreparationRepository repository;

    /**
     * Clean up repository after each test.
     */
    @After
    public void cleanUpAfterTests() {
        repository.clear();
    }

    @Test
    public void shouldGetPreparationThatWasAdded() throws JsonProcessingException {
        Preparation expected = getPreparation("7561486");
        repository.add(expected);
        final Preparation actual = repository.get(expected.id(), Preparation.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetStepThatWasAdded() {
        final Step expected = new Step(ROOT_STEP.id(), "684fdqs638");
        repository.add(expected);
        final Step actual = repository.get(expected.id(), Step.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldActionListThatWasAdded() {
        final List<Action> actions = PreparationTest.getSimpleAction("uppercase", "column_name", "lastname");
        PreparationActions expected = new PreparationActions(actions);

        repository.add(expected);
        final PreparationActions actual = repository.get(expected.id(), PreparationActions.class);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetOnlyWantedClass() {
        final Step expected = new Step(ROOT_STEP.id(), "8rq4868");
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
    public void shouldListOnlyWantedClass() {

        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.stream().forEach(prep -> repository.add(prep));

        // store some steps
        ids.stream().map(i -> new Step(ROOT_STEP.id(), "step" + i)) //
                .forEach(step -> repository.add(step));

        // list all preparations
        final Collection<Preparation> actual = repository.listAll(Preparation.class);

        assertEquals(ids.size(), actual.size());
        preparations.stream().forEach(actual::contains);
    }

    @Test
    public void shouldListOnlyPreparationsForDatasets() {
        List<Integer> ids = Arrays.asList(1, 2, 3);

        // store preparations
        final List<Preparation> preparations = ids.stream() //
                .map(i -> getPreparation(String.valueOf(i))) //
                .collect(Collectors.toList());

        preparations.stream().forEach(prep -> repository.add(prep));

        // and some steps to add some noise
        ids.stream().map(i -> new Step(ROOT_STEP.id(), "step" + i)) //
                .forEach(step -> repository.add(step));

        // get some preparation by dataset id
        final Preparation expected = preparations.get(1);
        final Collection<Preparation> actual = repository.getByDataSet(expected.getDataSetId());

        assertEquals(1, actual.size());
        assertTrue(actual.contains(expected));

    }

    /**
     * @param datasetId the preparation id.
     * @return a preparation with a root step an a the given dataset id.
     */
    private Preparation getPreparation(String datasetId) {
        Preparation preparation = new Preparation(datasetId, Step.ROOT_STEP.id());
        preparation.setName("prep-" + datasetId);
        return preparation;
    }
}