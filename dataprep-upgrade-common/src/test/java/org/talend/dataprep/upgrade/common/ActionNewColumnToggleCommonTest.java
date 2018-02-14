package org.talend.dataprep.upgrade.common;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.Test;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class ActionNewColumnToggleCommonTest {

    @Test
    public void shouldUpdateSteps() {
        // given
        final PreparationRepository repository = mock(PreparationRepository.class);
        final PreparationActions actions = mock(PreparationActions.class);
        final PersistentStep step = mock(PersistentStep.class);

        when(repository.list(eq(PreparationActions.class))).thenReturn(Stream.of(actions));
        when(actions.id()).thenReturn("actions-1", "actions-2");
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

        when(repository.list(eq(PreparationActions.class))).thenReturn(Stream.of(actions));
        when(actions.id()).thenReturn("actions-1"); // same id
        when(repository.list(eq(PersistentStep.class), any())).thenReturn(Stream.of(step));

        // when
        ActionNewColumnToggleCommon.upgradeActions(repository);

        // then
        verify(repository, times(1)).add(eq(actions));
        verify(repository, never()).add(eq(step));
        verify(step, never()).setContent(any());
    }
}