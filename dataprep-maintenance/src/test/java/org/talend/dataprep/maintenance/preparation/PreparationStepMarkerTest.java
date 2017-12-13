package org.talend.dataprep.maintenance.preparation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(MockitoJUnitRunner.class)
public class PreparationStepMarkerTest {

    @Test
    public void shouldMarkUnusedSteps() {
        // Given
        StepMarker marker = new PreparationStepMarker();
        PreparationRepository repository = mock(PreparationRepository.class);
        final Preparation preparation = new Preparation();

        final Step step = mock(Step.class);
        when(step.id()).thenReturn("1234");
        when(step.getParent()).thenReturn(Step.ROOT_STEP.id());
        when(repository.get(eq("1234"), eq(Step.class))).thenReturn(step);

        preparation.setSteps(Arrays.asList(Step.ROOT_STEP, step));
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.of(preparation));

        // When
        final StepMarker.Result result = marker.mark(repository, "myMarker-1234");

        // Then
        assertEquals(StepMarker.Result.COMPLETED, result);
        verify(repository, times(1)).add(eq(Collections.singletonList(step)));
        verify(step, times(1)).setMarker(eq("myMarker-1234"));
    }

    @Test
    public void shouldDisableCleanUpAtStart() {
        // Given
        StepMarker marker = new PreparationStepMarker();
        PreparationRepository repository = mock(PreparationRepository.class);
        when(repository.exist(eq(Preparation.class), any())).thenReturn(true);

        // When
        final StepMarker.Result result = marker.mark(repository, "myMarker-1234");

        // Then
        assertEquals(StepMarker.Result.INTERRUPTED, result);
    }

    @Test
    public void shouldDisableCleanUpDuringProcess() {
        // Given
        StepMarker marker = new PreparationStepMarker();
        PreparationRepository repository = mock(PreparationRepository.class);
        when(repository.exist(eq(Preparation.class), any())).thenReturn(false, true);
        final Preparation preparation = new Preparation();
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.of(preparation));

        // When
        final StepMarker.Result result = marker.mark(repository, "myMarker-1234");

        // Then
        assertEquals(StepMarker.Result.INTERRUPTED, result);
        verify(repository, never()).add(Matchers.<Collection<Step>>any());
    }
}