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

package org.talend.dataprep.api.preparation;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.*;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.preparation.FixedIdPreparationContent;
import org.talend.dataprep.preparation.FixedIdStep;
import org.talend.dataprep.preparation.store.PersistentPreparationRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class PreparationUtilsTest extends ServiceBaseTest {

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private VersionService versionService;

    @Autowired
    private PreparationUtils preparationUtils;

    @Test
    public void should_list_steps_ids_history_from_root() {
        // given
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        final PreparationActions newContent2 = newContent1.append(actions);

        final String version = versionService.version().getVersionId();
        final Step step1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        final Step step2 = new Step(step1.id(), newContent2.id(), version);

        repository.add(newContent1);
        repository.add(newContent2);
        repository.add(step1);
        repository.add(step2);

        // when
        List<String> ids = preparationUtils.listStepsIds(step1.id(), repository);

        // then
        assertThat(ids, hasItem(Step.ROOT_STEP.id()));
        assertThat(ids, hasItem(step1.id()));
        assertThat(ids, not(hasItem(step2.id())));

        // when
        ids = preparationUtils.listStepsIds(step2.id(), repository);

        // then
        assertThat(ids, hasItem(Step.ROOT_STEP.id()));
        assertThat(ids, hasItem(step1.id()));
        assertThat(ids, hasItem(step2.id()));
    }

    @Test
    public void should_list_steps_ids_history_from_limit() {
        // given
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        final PreparationActions newContent2 = newContent1.append(actions);

        final String version = versionService.version().getVersionId();
        final Step step1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        final Step step2 = new Step(step1.id(), newContent2.id(), version);

        repository.add(newContent1);
        repository.add(newContent2);
        repository.add(step1);
        repository.add(step2);

        // when
        List<String> ids = preparationUtils.listStepsIds(step2.id(), step1.getId(), repository);

        // then
        assertThat(ids, not(hasItem(Step.ROOT_STEP.id())));
        assertThat(ids, hasItem(step1.id()));
        assertThat(ids, hasItem(step2.id()));

        // when
        ids = preparationUtils.listStepsIds(step2.id(), step2.getId(), repository);

        // then
        assertThat(ids, not(hasItem(Step.ROOT_STEP.id())));
        assertThat(ids, not(hasItem(step1.id())));
        assertThat(ids, hasItem(step2.id()));
    }

    @Test
    public void should_list_steps_history_from_root() {
        // given
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        final PreparationActions newContent2 = newContent1.append(actions);

        final String version = versionService.version().getVersionId();
        final Step step1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        final Step step2 = new Step(step1.id(), newContent2.id(), version);

        repository.add(newContent1);
        repository.add(newContent2);
        repository.add(step1);
        repository.add(step2);

        // when
        List<Step> steps = preparationUtils.listSteps(step1.id(), repository);

        // then
        assertThat(steps, hasItem(Step.ROOT_STEP));
        assertThat(steps, hasItem(step1));
        assertThat(steps, not(hasItem(step2)));

        // when
        steps = preparationUtils.listSteps(step2.id(), repository);

        // then
        assertThat(steps, hasItem(Step.ROOT_STEP));
        assertThat(steps, hasItem(step1));
        assertThat(steps, hasItem(step2));
    }

    @Test
    public void should_list_steps_history_from_limit() {
        // given
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent1 = PreparationActions.ROOT_ACTIONS.append(actions);
        final PreparationActions newContent2 = newContent1.append(actions);

        final String version = versionService.version().getVersionId();
        final Step step1 = new Step(Step.ROOT_STEP.id(), newContent1.id(), version);
        final Step step2 = new Step(step1.id(), newContent2.id(), version);

        repository.add(newContent1);
        repository.add(newContent2);
        repository.add(step1);
        repository.add(step2);

        // when
        List<Step> steps = preparationUtils.listSteps(step2, step1.getId(), repository);

        // then
        assertThat(steps, not(hasItem(Step.ROOT_STEP)));
        assertThat(steps, hasItem(step1));
        assertThat(steps, hasItem(step2));

        // when
        steps = preparationUtils.listSteps(step2, step2.getId(), repository);

        // then
        assertThat(steps, not(hasItem(Step.ROOT_STEP)));
        assertThat(steps, not(hasItem(step1)));
        assertThat(steps, hasItem(step2));
    }

    @Test
    public void should_return_empty_list_with_null_last_step() throws Exception {
        // when
        final List<Step> steps = preparationUtils.listSteps(null, "limit", repository);

        // then
        assertThat(steps, hasSize(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_with_null_limit() throws Exception {
        // when
        preparationUtils.listSteps(Step.ROOT_STEP, null, repository);

        // then
        fail("Should have thrown IllegalArgumentException because limit step is null");
    }

    @Test
    public void prettyPrint() throws Exception {
        // given
        final String version = versionService.version().getVersionId();
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        final PreparationActions newContent = new PreparationActions(actions, version);
        final Step step = new Step(Step.ROOT_STEP.id(), newContent.id(), version);
        final Preparation preparation = new Preparation("#15325878", "1234", step.id(), version);

        repository.add(newContent);
        repository.add(step);
        repository.add(preparation);

        // when
        PreparationUtils.prettyPrint(repository, preparation, new NullOutputStream());

        // Basic walk through code, no assert.
    }

    @Test
    public void scatterNull() throws Exception {
        // When
        final Collection<Identifiable> identifiableList = PreparationUtils.scatter(null);

        // Then
        assertEquals(0, identifiableList.size());
    }

    @Test
    public void scatterStep() throws Exception {
        // Given
        final Step step = new Step();

        // When
        final Collection<Identifiable> identifiableList = PreparationUtils.scatter(step);

        // Then
        assertEquals(1, identifiableList.size());
        final Iterator<Identifiable> iterator = identifiableList.iterator();
        assertEquals(step, iterator.next());
    }

    @Test
    public void scatterPreparationAction() throws Exception {
        // Given
        final PreparationActions preparationActions = new PreparationActions();

        // When
        final Collection<Identifiable> identifiableList = PreparationUtils.scatter(preparationActions);

        // Then
        assertEquals(1, identifiableList.size());
        assertEquals(preparationActions, identifiableList.iterator().next());
    }

    @Test
    public void scatterPreparation() throws Exception {
        // Given
        final Preparation preparation = new Preparation();
        final Step step1 = new FixedIdStep("step-1234");
        final Step step2 = new FixedIdStep("step-5678");
        preparation.setSteps(Arrays.asList(step1, step2));
        repository.add(preparation);

        // When
        final Collection<Identifiable> identifiableList = PreparationUtils.scatter(preparation);

        // Then
        assertEquals(3, identifiableList.size());
        final Iterator<Identifiable> iterator = identifiableList.iterator();
        assertEquals(preparation, iterator.next());
        assertEquals(step1, iterator.next());
        assertEquals(step2, iterator.next());
    }

    @Test
    public void scatterPreparationSave() throws Exception {
        // Given
        final Preparation preparation = new Preparation();
        final Step step1 = new FixedIdStep("step-1234");
        final Step step2 = new FixedIdStep("step-5678");
        preparation.setSteps(Arrays.asList(step1, step2));

        // When
        repository.add(preparation);

        // Then
        assertEquals(PersistentPreparationRepository.class, repository.getClass());
        assertTrue(repository.exist(Step.class, eq("id", "step-1234")));
        assertTrue(repository.exist(Step.class, eq("id", "step-5678")));
    }

    @Test
    public void scatterPreparationLoad() throws Exception {
        // Given
        final Preparation preparation = new Preparation();
        preparation.setId("prep-1234");
        final Step step1 = new FixedIdStep("step-1234");
        final Step step2 = new FixedIdStep("step-5678");
        final PreparationActions actions1 = new FixedIdPreparationContent("actions-1234");
        final PreparationActions actions2 = new FixedIdPreparationContent("actions-5678");
        step1.setContent(actions1.id());
        step2.setContent(actions2.id());
        preparation.setHeadId(step2.id());
        final List<Step> expectedSteps = Arrays.asList(Step.ROOT_STEP, step1, step2);
        preparation.setSteps(expectedSteps);
        preparation.setHeadId(step2.getId());

        // When
        repository.add(actions1);
        repository.add(actions2);
        repository.add(preparation);
        final Preparation savedPreparation = repository.get("prep-1234", Preparation.class);

        // Then
        assertEquals(PersistentPreparationRepository.class, repository.getClass());
        assertEquals(preparation.getId(), savedPreparation.getId());
        assertEquals(3, savedPreparation.getSteps().size());
        assertEquals(Step.ROOT_STEP.id(), savedPreparation.getSteps().get(0).getId());
        assertEquals("step-1234", savedPreparation.getSteps().get(1).getId());
        assertEquals("step-5678", savedPreparation.getSteps().get(2).getId());
        assertNotNull(savedPreparation.getSteps().get(0).getContent());
        assertNotNull(savedPreparation.getSteps().get(1).getContent());
        assertNotNull(savedPreparation.getSteps().get(2).getContent());
        assertEquals(PreparationActions.ROOT_ACTIONS.id(), savedPreparation.getSteps().get(0).getContent());
        assertEquals("actions-1234", savedPreparation.getSteps().get(1).getContent());
        assertEquals("actions-5678", savedPreparation.getSteps().get(2).getContent());
    }


    public static List<Action> getSimpleAction(final String actionName, final String paramKey, final String paramValue) {
        final Action action = new Action();
        action.setName(actionName);
        action.getParameters().put(paramKey, paramValue);

        final List<Action> actions = new ArrayList<>();
        actions.add(action);

        return actions;
    }
}
