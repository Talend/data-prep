package org.talend.dataprep.transformation;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

        RowMetadata rowMetadata = new RowMetadata();

        String randomStepId = RandomStringUtils.random(10);

        reactiveStepMetadataRepository.update(randomStepId, rowMetadata);

        Thread.sleep(3000);

        verify(proxy, times(1)).asTechnicalUser();
        verify(delegate, times(1)).update(randomStepId, rowMetadata);
        verify(proxy, times(1)).releaseIdentity();
    }

    @Test
    public void testEmitInvalidateMessage() throws InterruptedException {

        String randomStepId = RandomStringUtils.random(10);

        reactiveStepMetadataRepository.invalidate(randomStepId);

        Thread.sleep(3000);

        verify(proxy, times(1)).asTechnicalUser();
        verify(delegate, times(1)).invalidate(randomStepId);
        verify(proxy, times(1)).releaseIdentity();
    }
}
