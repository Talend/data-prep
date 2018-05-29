package org.talend.dataprep.preparation.event;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.tql.api.TqlBuilder;

@RunWith(MockitoJUnitRunner.class)
public class PreparationUpdateListenerTest {

    @InjectMocks
    private PreparationUpdateListener preparationUpdateListener;

    @Mock
    private PreparationRepository preparationRepository;

    @Mock
    private PreparationUtils preparationUtils;

    @Test
    public void shouldRemoveStepRowMetadata() {
        // given
        final Step step1 = new Step();
        step1.setId(UUID.randomUUID().toString());
        step1.setRowMetadata("srmd-1");
        final Step step2 = new Step();
        step2.setId(UUID.randomUUID().toString());
        step2.setRowMetadata("srmd-2");
        step2.setParent(step1.id());
        final Step step3 = new Step();
        step3.setId(UUID.randomUUID().toString());
        step3.setRowMetadata(null); // Intentionally left to null
        step3.setParent(step2.id());

        final Preparation preparation = new Preparation();
        preparation.setHeadId(step3.id());

        when(preparationUtils.listSteps(eq(preparation.getHeadId()), eq(preparationRepository))).thenReturn(Arrays.asList(Step.ROOT_STEP, step1, step2, step3));
        when(preparationRepository.list(eq(Preparation.class), eq(TqlBuilder.eq("dataSetId", "ds-1234"))))
                .thenReturn(Stream.of(preparation));
        when(preparationRepository.get(eq(step1.id()), eq(Step.class))).thenReturn(step1);
        when(preparationRepository.get(eq(step2.id()), eq(Step.class))).thenReturn(step2);

        // when
        final DataSetMetadata metadata = new DataSetMetadata();
        metadata.setId("ds-1234");
        preparationUpdateListener.onUpdate(new DatasetUpdatedEvent(metadata));

        // then
        verify(preparationRepository, times(1)).add(any(Preparation.class));
        verify(preparationRepository, times(1)).remove(eq(StepRowMetadata.class), eq(TqlBuilder.eq("id", "srmd-1")));
        verify(preparationRepository, times(1)).remove(eq(StepRowMetadata.class), eq(TqlBuilder.eq("id", "srmd-2")));
    }
}