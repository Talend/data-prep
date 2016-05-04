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

package org.talend.dataprep.preparation.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.BasePreparationTest;


@TestPropertySource(properties={"dataset.metadata.store: in-memory", "preparation.store.remove.hours: 2"})
public class PreparationCleanerTest extends BasePreparationTest {

    @Autowired
    private PreparationCleaner cleaner;

    @Autowired
    ConfigurableEnvironment environment;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void removeOrphanSteps_should_remove_orphan_step_after_at_least_X_hours() {
        //given
        final String version = versionService.version().getVersionId();
        final Step firstStep = new Step(rootStep.getId(), "first", version);
        final Step secondStep = new Step(firstStep.getId(), "second", version);
        final Step orphanStep = new Step(secondStep.getId(), "orphan", version);
        final Preparation preparation = new Preparation("#123", "1", secondStep.id(), version);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(orphanStep);
        repository.add(preparation);

        //when: after 0 hour - should not remove
        cleaner.removeOrphanSteps();
        assertNotNull(repository.get(orphanStep.getId(), Step.class));
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));

        //when: after 1 hour - should not remove
        cleaner.removeOrphanSteps();
        assertNotNull(repository.get(orphanStep.getId(), Step.class));
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));

        //when: after 2 hours
        cleaner.removeOrphanSteps();

        //then
        assertNull(repository.get(orphanStep.getId(), Step.class));
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_step_that_still_belongs_to_a_preparation() {
        //given
        final String version = versionService.version().getVersionId();
        final Step firstStep = new Step(rootStep.getId(), "first", version);
        final Step secondStep = new Step(firstStep.getId(), "second", version);
        final Step thirdStep = new Step(secondStep.getId(), "third", version);

        final Preparation firstPreparation = new Preparation("#458", "1", firstStep.id(), version);
        final Preparation secondPreparation = new Preparation("#5428", "2", thirdStep.id(), version);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(thirdStep);
        repository.add(firstPreparation);
        repository.add(secondPreparation);

        //when
        cleaner.removeOrphanSteps(); //0 hour
        cleaner.removeOrphanSteps(); //1 hour
        cleaner.removeOrphanSteps(); //2 hour

        //then
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
        assertNotNull(repository.get(thirdStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_root_step() {
        //given
        repository.clear();
        assertNotNull(repository.get(rootStep.getId(), Step.class));

        //when
        cleaner.removeOrphanSteps(); //0 hour
        cleaner.removeOrphanSteps(); //1 hour
        cleaner.removeOrphanSteps(); //2 hour

        //then
        assertNotNull(repository.get(rootStep.getId(), Step.class));
    }
}
