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

package org.talend.dataprep.maintenance.preparation;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.tql.api.TqlBuilder;

public class PreparationCleanerTest extends BaseMaintenanceTest {

    @InjectMocks
    private PreparationCleaner cleaner;

    @Mock
    private PreparationRepository repository;

    @Test
    public void removeOrphanSteps_should_remove_orphan_step_after_at_least_X_hours() {
        // given
        final String version = "1.2.3";
        final Step firstStep = new Step(Step.ROOT_STEP.id(), "first", version);
        final Step secondStep = new Step(firstStep.id(), "second", version);
        final Step orphanStep = new Step(secondStep.id(), "orphan", version);

        when(repository.list(eq(Step.class))).thenReturn(Stream.of(firstStep, secondStep, orphanStep));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(firstStep, secondStep, orphanStep).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        final Preparation preparation = new Preparation("#123", "1", secondStep.id(), version);
        preparation.setSteps(Arrays.asList(secondStep, firstStep));
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.of(preparation));

        // when
        cleaner.removeOrphanSteps();

        // then
        verify(repository, never()).remove(eq(firstStep));
        verify(repository, never()).remove(eq(secondStep));
        verify(repository, times(1)).remove(eq(orphanStep));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_step_that_still_belongs_to_a_preparation() {
        // given
        final String version = "1.2.3";
        final Step firstStep = new Step(Step.ROOT_STEP.id(), "first", version);
        final Step secondStep = new Step(firstStep.id(), "second", version);
        final Step thirdStep = new Step(secondStep.id(), "third", version);
        when(repository.list(eq(Step.class))).thenReturn(Stream.of(firstStep, secondStep, thirdStep));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(firstStep, secondStep, thirdStep).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        final Preparation firstPreparation = new Preparation("#458", "1", firstStep.id(), version);
        firstPreparation.setSteps(Collections.singletonList(firstStep));
        final Preparation secondPreparation = new Preparation("#5428", "2", thirdStep.id(), version);
        secondPreparation.setSteps(Arrays.asList(thirdStep, secondStep, firstStep));
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.of(firstPreparation, secondPreparation));

        // when
        cleaner.removeOrphanSteps();

        // then
        verify(repository, never()).remove(eq(firstStep));
        verify(repository, never()).remove(eq(secondStep));
        verify(repository, never()).remove(eq(thirdStep));
    }

    @Test
    public void removeOrphanSteps_should_not_remove_root_step() {
        // given
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.empty());
        final PersistentStep rootStep = new PersistentStep();
        rootStep.setId(Step.ROOT_STEP.id());
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(rootStep));

        // when
        cleaner.removeOrphanSteps();

        // then
        verify(repository, never()).remove(eq(Step.ROOT_STEP));
    }

    @Test
    public void removeOrphanSteps_should_remove_orphan_step_content() {
        // given
        final String version = "1.2.3";
        final PreparationActions content = new PreparationActions();
        content.setAppVersion(version);
        when(repository.list(PreparationActions.class)).thenReturn(Stream.of(content));
        when(repository.get(content.id(), PreparationActions.class)).thenReturn(content);

        final Step step = new Step(Step.ROOT_STEP.id(), content.id(), version);
        when(repository.list(eq(Step.class))).thenReturn(Stream.of(step));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(step).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        when(repository.list(eq(Preparation.class))).thenReturn(Stream.empty());

        // when
        cleaner.removeOrphanSteps();

        // then
        verify(repository, times(1)).remove(eq(content));
    }

    @Test
    public void shouldNotRemovePreparationActions_sharedByMultiplePreparation() throws Exception {
        // given
        final String version = "1.2.3";
        final PreparationActions content = new PreparationActions();
        List<Action> actions = new ArrayList<>();
        content.append(actions);
        content.setAppVersion(version);
        when(repository.list(PreparationActions.class)).thenReturn(Stream.of(content));
        when(repository.get(content.id(), PreparationActions.class)).thenReturn(content);

        // 2 preparations, with each, one step that shares the same action
        final Step stepFirstPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);
        final Step stepSecondPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);

        // add the steps to the repository
        when(repository.exist(eq(PersistentStep.class), eq(TqlBuilder.eq("contentId", content.id())))).thenReturn(true);

        when(repository.list(Step.class)).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        Preparation firstPreparation = new Preparation("1", null, stepFirstPreparation.getId(), version);
        firstPreparation.setSteps(Collections.singletonList(stepFirstPreparation));
        Preparation secondPreparation = new Preparation("2", null, stepSecondPreparation.getId(), version);
        secondPreparation.setSteps(Collections.singletonList(stepSecondPreparation));

        // add the preparations to the repository
        when(repository.list(Preparation.class)).thenReturn(Stream.of(firstPreparation, secondPreparation));

        // when
        cleaner.removeOrphanSteps();

        // then
        verify(repository, never()).remove(eq(content));
    }

    @Test
    public void shouldNotRemovePreparationActions_ownedByOnePreparation() throws Exception {

        // given
        final String version = "1.2.3";
        final PreparationActions content = new PreparationActions();
        List<Action> actions = new ArrayList<>();
        content.append(actions);
        content.setAppVersion(version);
        when(repository.list(PreparationActions.class)).thenReturn(Stream.of(content));
        when(repository.get(content.id(), PreparationActions.class)).thenReturn(content);

        // 2 preparations, with each, one step that shares the same action
        final Step stepFirstPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);
        final Step stepSecondPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);

        // add the steps to the repository
        when(repository.list(Step.class)).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        Preparation firstPreparation = new Preparation("1", null, stepFirstPreparation.getId(), version);
        firstPreparation.setSteps(Collections.singletonList(stepFirstPreparation));
        Preparation secondPreparation = new Preparation("2", null, stepSecondPreparation.getId(), version);
        secondPreparation.setSteps(Collections.singletonList(stepSecondPreparation));

        // add the preparations to the repository
        when(repository.exist(eq(PersistentStep.class), any())).thenReturn(true);
        when(repository.list(Preparation.class)).thenReturn(Stream.of(secondPreparation)); // Remove first preparation

        // when
        cleaner.removeOrphanSteps();

        // then
        // when the first preparation is removed, the shared action is not deleted
        verify(repository, never()).remove(eq(content));
    }

    @Test
    public void shouldRemovePreparationActions_noPreparation() throws Exception {

        // given
        final String version = "1.2.3";
        final PreparationActions content = new PreparationActions();
        List<Action> actions = new ArrayList<>();
        content.append(actions);
        content.setAppVersion(version);
        when(repository.list(PreparationActions.class)).thenReturn(Stream.of(content));
        when(repository.get(content.id(), PreparationActions.class)).thenReturn(content);

        // 2 preparations, with each, one step that shares the same action
        final Step stepFirstPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);
        final Step stepSecondPreparation = new Step(Step.ROOT_STEP.getId(), content.getId(), version);

        // add the steps to the repository
        when(repository.list(Step.class)).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation));
        when(repository.list(eq(PersistentStep.class))).thenReturn(Stream.of(stepFirstPreparation, stepSecondPreparation).map(s -> {
            final PersistentStep persistentStep = new PersistentStep();
            persistentStep.setId(s.id());
            persistentStep.setContent(s.getContent());
            return persistentStep;
        }));

        Preparation firstPreparation = new Preparation("1", null, stepFirstPreparation.getId(), version);
        firstPreparation.setSteps(Collections.singletonList(stepFirstPreparation));
        Preparation secondPreparation = new Preparation("2", null, stepSecondPreparation.getId(), version);
        secondPreparation.setSteps(Collections.singletonList(stepSecondPreparation));

        // when
        when(repository.exist(eq(PersistentStep.class), eq(TqlBuilder.eq("contentId", content.id())))).thenReturn(false);
        when(repository.list(Preparation.class)).thenReturn(Stream.empty()); // Remove first and second preparations
        cleaner.removeOrphanSteps();

        // then
        verify(repository, times(2)).remove(eq(content)); // 2 steps content
    }


}
