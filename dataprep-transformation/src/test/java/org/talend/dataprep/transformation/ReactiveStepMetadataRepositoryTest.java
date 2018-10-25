package org.talend.dataprep.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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

    @InjectMocks
    private ReactiveStepMetadataRepository reactiveStepMetadataRepository;

    @Mock
    private StepMetadataRepository delegate;

    @Mock
    private SecurityProxy proxy;

    @Test
    public void testEmitUpdateMessage() throws InterruptedException {
        // given
        CountDownLatch delegateInvalidateLatch = new CountDownLatch(1);
        RowMetadata rowMetadata = new RowMetadata();
        String stepId = "10";
        Mockito.doAnswer(invocation -> {
            delegateInvalidateLatch.countDown();
            return null;
        }).when(delegate).update(stepId, rowMetadata);

        CountDownLatch proxyReleaseLatch = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            proxyReleaseLatch.countDown();
            return null;
        }).when(proxy).releaseIdentity();

        // when
        reactiveStepMetadataRepository.update(stepId, rowMetadata);

        // then
        boolean awaitSuccess = delegateInvalidateLatch.await(1, TimeUnit.SECONDS);
        if (!awaitSuccess) {
            fail("Delegate was never called");
        }
        verify(proxy).asTechnicalUser();
        verify(delegate).update(stepId, rowMetadata); // not really needed as the future verify that
        proxyReleaseLatch.await(1, TimeUnit.SECONDS); // To be sure the method has been called
        verify(proxy).releaseIdentity();
    }

    @Test
    public void testEmitInvalidateMessage() throws InterruptedException {
        // given
        CountDownLatch delegateInvalidateLatch = new CountDownLatch(1);
        String stepId = "11";
        Mockito.doAnswer(invocation -> {
            delegateInvalidateLatch.countDown();
            return null;
        }).when(delegate).invalidate(stepId);

        CountDownLatch proxyReleaseLatch = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            proxyReleaseLatch.countDown();
            return null;
        }).when(proxy).releaseIdentity();

        // when
        reactiveStepMetadataRepository.invalidate(stepId);

        // then
        boolean awaitSuccess = delegateInvalidateLatch.await(1, TimeUnit.SECONDS);
        if (!awaitSuccess) {
            fail("Delegate was never called");
        }
        verify(proxy).asTechnicalUser();
        verify(delegate).invalidate(stepId); // not really needed as the future verify that
        proxyReleaseLatch.await(1, TimeUnit.SECONDS); // To be sure the method has been called
        verify(proxy).releaseIdentity();
    }
}
