package org.talend.dataprep.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.service.ReactiveStepMetadataRepository;
import org.talend.dataprep.transformation.service.StepMetadataRepository;

@RunWith(MockitoJUnitRunner.class)
public class ReactiveStepMetadataRepositoryTest {

    private static final String TASK_COMPLETED_SUCCESSFULLY = "OK";

    @InjectMocks
    private ReactiveStepMetadataRepository reactiveStepMetadataRepository;

    @Mock
    private StepMetadataRepository delegate;

    @Mock
    private SecurityProxy proxy;

    @Test
    public void testEmitUpdateMessage() throws InterruptedException, ExecutionException {
        // given
        RowMetadata rowMetadata = new RowMetadata();
        String stepId = "10";
        final CompletableFuture<String> future = new CompletableFuture<>();
        Mockito.doAnswer(invocation -> {
            future.complete(TASK_COMPLETED_SUCCESSFULLY);
            return null;
        }).when(delegate).update(stepId, rowMetadata);

        // when
        reactiveStepMetadataRepository.update(stepId, rowMetadata);

        // then
        String result = null;
        try {
            result = future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Delegate was never called");
        }
        assertEquals(TASK_COMPLETED_SUCCESSFULLY, result); // should always be true or else would have failed before
        verify(proxy, times(1)).asTechnicalUser();
        verify(delegate, times(1)).update(stepId, rowMetadata);
        verify(proxy, times(1)).releaseIdentity();
    }

    @Test
    public void testEmitInvalidateMessage() throws InterruptedException, ExecutionException {
        // given
        final CompletableFuture<String> future = new CompletableFuture<>();
        String stepId = "11";
        Mockito.doAnswer(invocation -> {
            future.complete(TASK_COMPLETED_SUCCESSFULLY);
            return null;
        }).when(delegate).invalidate(stepId);

        // when
        reactiveStepMetadataRepository.invalidate(stepId);

        // then
        String result = null;
        try {
            result = future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Delegate was never called");
        }
        assertEquals(TASK_COMPLETED_SUCCESSFULLY, result); // should always be true or else would have failed before
        verify(proxy, times(1)).asTechnicalUser();
        verify(delegate, times(1)).invalidate(stepId);
        verify(proxy, times(1)).releaseIdentity();
    }
}
