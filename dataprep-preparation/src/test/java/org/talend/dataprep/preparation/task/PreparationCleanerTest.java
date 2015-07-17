package org.talend.dataprep.preparation.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationRepository;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.Application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
public class PreparationCleanerTest {
    @Autowired
    private PreparationCleaner cleaner;

    @Autowired
    private PreparationRepository repository;

    @Autowired
    ConfigurableEnvironment environment;

    @Before
    public void setUp() {
        final MockPropertySource repositoryInformation = new MockPropertySource().withProperty("dataset.metadata.store", "in-memory");
        environment.getPropertySources().addFirst(repositoryInformation);
    }

    @Test
    public void removeOrphanSteps_should_remove_orphan_step() {
        //given
        final Step firstStep = new Step(ROOT_STEP.getId(), "first");
        final Step secondStep = new Step(firstStep.getId(), "second");
        final Step orphanStep = new Step(secondStep.getId(), "orphan");
        final Preparation preparation = new Preparation("1", secondStep);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(orphanStep);
        repository.add(preparation);

        //when
        cleaner.removeOrphanSteps();

        //then
        assertNull(repository.get(orphanStep.getId(), Step.class));
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_step_that_still_belongs_to_a_preparation() {
        //given
        final Step firstStep = new Step(ROOT_STEP.getId(), "first");
        final Step secondStep = new Step(firstStep.getId(), "second");
        final Step thirdStep = new Step(secondStep.getId(), "third");

        final Preparation firstPreparation = new Preparation("1", firstStep);
        final Preparation secondPreparation = new Preparation("2", thirdStep);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(thirdStep);
        repository.add(firstPreparation);
        repository.add(secondPreparation);

        //when
        cleaner.removeOrphanSteps();

        //then
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
        assertNotNull(repository.get(thirdStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_root_step() {
        //given
        repository.clear();
        assertNotNull(repository.get(ROOT_STEP.getId(), Step.class));

        //when
        cleaner.removeOrphanSteps();

        //then
        assertNotNull(repository.get(ROOT_STEP.getId(), Step.class));
    }
}
