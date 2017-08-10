// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.preparation.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.BasePreparationTest;

@TestPropertySource(properties = { "dataset.metadata.store: in-memory" })
public class PreparationCleanerTest extends BasePreparationTest {

    @Autowired
    private PreparationCleaner cleaner;

    @Autowired
    ConfigurableEnvironment environment;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void removeOrphanSteps_should_remove_orphan_step_after_at_least_X_hours() {
        // given
        final String version = versionService.version().getVersionId();
        final Step firstStep = new Step(rootStep.id(), "first", version);
        final Step secondStep = new Step(firstStep.id(), "second", version);
        final Step orphanStep = new Step(secondStep.id(), "orphan", version);
        final Preparation preparation = new Preparation("#123", "1", secondStep.id(), version);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(orphanStep);
        repository.add(preparation);

        // when
        cleaner.removeOrphanSteps();

        // then
        assertNull(repository.get(orphanStep.getId(), Step.class));
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_step_that_still_belongs_to_a_preparation() {
        // given
        final String version = versionService.version().getVersionId();
        final Step firstStep = new Step(rootStep.id(), "first", version);
        final Step secondStep = new Step(firstStep.id(), "second", version);
        final Step thirdStep = new Step(secondStep.id(), "third", version);

        final Preparation firstPreparation = new Preparation("#458", "1", firstStep.id(), version);
        final Preparation secondPreparation = new Preparation("#5428", "2", thirdStep.id(), version);

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(thirdStep);
        repository.add(firstPreparation);
        repository.add(secondPreparation);

        // when
        cleaner.removeOrphanSteps();

        // then
        assertNotNull(repository.get(firstStep.getId(), Step.class));
        assertNotNull(repository.get(secondStep.getId(), Step.class));
        assertNotNull(repository.get(thirdStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_root_step() {
        // given
        repository.clear();
        assertNotNull(repository.get(rootStep.getId(), Step.class));

        // when
        cleaner.removeOrphanSteps();

        // then
        assertNotNull(repository.get(rootStep.getId(), Step.class));
    }

    @Test
    public void removeOrphanSteps_should_remove_orphan_step_content() {
        // given
        final String version = versionService.version().getVersionId();
        final PreparationActions content = new PreparationActions();
        content.setAppVersion(version);
        final Step step = new Step(rootStep.id(), content.id(), version);

        repository.add(step);
        repository.add(content);

        // when
        cleaner.removeOrphanSteps();

        // then
        assertNull(repository.get(step.getId(), Step.class));
        assertNull(repository.get(content.getId(), PreparationActions.class));
    }

    @Test
    public void test_clean_preparation_dont_remove_mutualized_actions() throws Exception {
        // given
        final String version = versionService.version().getVersionId();
        final PreparationActions content = new PreparationActions();
        List<Action> actions = new ArrayList<>();
        content.append(actions);
        content.setAppVersion(version);
        repository.add(content);

        final Step stepFirstPreparation = new Step(rootStep.getId(), content.getId(), version);
        final Step stepSecondPreparation = new Step(rootStep.getId(), content.getId(), version);

        repository.add(stepFirstPreparation);
        repository.add(stepSecondPreparation);

        Preparation firstPreparation = new Preparation("1", null, stepFirstPreparation.getId(), version);
        Preparation secondPreparation = new Preparation("2", null, stepSecondPreparation.getId(), version);

        repository.add(firstPreparation);
        repository.add(secondPreparation);

        int expectedNbActions = repository.list(PreparationActions.class).collect(Collectors.toList()).size();

        // when
        repository.remove(firstPreparation);
        cleaner.removeOrphanSteps();

        // then
        int nbActions = repository.list(PreparationActions.class).collect(Collectors.toList()).size();
        assertEquals(expectedNbActions, nbActions);

        // when
        repository.remove(secondPreparation);
        cleaner.removeOrphanSteps();

        // then
        nbActions = repository.list(PreparationActions.class).collect(Collectors.toList()).size();
        assertEquals(1, nbActions);
    }
}
