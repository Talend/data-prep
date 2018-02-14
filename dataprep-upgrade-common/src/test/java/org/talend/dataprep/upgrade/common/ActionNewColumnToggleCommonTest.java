package org.talend.dataprep.upgrade.common;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class ActionNewColumnToggleCommonTest {

    @Test
    public void shouldUpdateSteps() {
        // given
        final PreparationRepository repository = mock(PreparationRepository.class);
        final PreparationActions actions = mock(PreparationActions.class);
        final PersistentStep step = mock(PersistentStep.class);
        final Action action = mock(Action.class);
        List<Action> actionsList = new ArrayList<>();
        actionsList.add(action);

        when(step.id()).thenReturn("step-1");
        when(actions.getActions()).thenReturn(actionsList);
        when(action.getName()).thenReturn("action");
        when(repository.list(eq(PreparationActions.class))).thenReturn(Stream.of(actions));
        // Twice "action-1" to pass root preparation action filter.
        when(actions.id()).thenReturn("actions-1", "actions-1", "actions-2");
        when(repository.list(eq(PersistentStep.class), any())).thenReturn(Stream.of(step));

        // when
        ActionNewColumnToggleCommon.upgradeActions(repository);

        // then
        verify(repository, times(1)).add(eq(actions));
        verify(repository, times(1)).add(eq(step));
        verify(step, times(1)).setContent(eq("actions-2"));
    }

    @Test
    public void shouldNotUpdateSteps() {
        // given
        final PreparationRepository repository = mock(PreparationRepository.class);
        final PreparationActions actions = mock(PreparationActions.class);
        final PersistentStep step = mock(PersistentStep.class);
        final Action action = mock(Action.class);
        List<Action> actionsList = new ArrayList<>();
        actionsList.add(action);

        when(step.id()).thenReturn("step-1");
        when(actions.getActions()).thenReturn(actionsList);
        when(action.getName()).thenReturn("action");
        when(repository.list(eq(PreparationActions.class))).thenReturn(Stream.of(actions));
        // Twice "action-1" to pass root preparation action filter.
        when(actions.id()).thenReturn("actions-1", "actions-1", "actions-1"); // same id
        when(repository.list(eq(PersistentStep.class), any())).thenReturn(Stream.of(step));

        // when
        ActionNewColumnToggleCommon.upgradeActions(repository);

        // then
        verify(repository, times(1)).add(eq(actions));
        verify(repository, never()).add(eq(step));
        verify(step, never()).setContent(any());
    }

    @Test
    public void shouldNotUpdateRootStep() {
        // given
        final PreparationRepository repository = mock(PreparationRepository.class);
        final PreparationActions actions = mock(PreparationActions.class);
        final PersistentStep step = mock(PersistentStep.class);
        final Action action = mock(Action.class);
        List<Action> actionsList = new ArrayList<>();
        actionsList.add(action);

        when(step.id()).thenReturn(Step.ROOT_STEP.id()); // Listed step is root step
        when(actions.getActions()).thenReturn(actionsList);
        when(action.getName()).thenReturn("action");
        when(repository.list(eq(PreparationActions.class))).thenReturn(Stream.of(actions));
        when(actions.id()).thenReturn("actions-1", "actions-2");
        when(repository.list(eq(PersistentStep.class), any())).thenReturn(Stream.of(step));

        // when
        ActionNewColumnToggleCommon.upgradeActions(repository);

        // then
        verify(repository, times(1)).add(eq(actions));
        verify(repository, never()).add(eq(step));
        verify(step, never()).setContent(any());
    }
}
