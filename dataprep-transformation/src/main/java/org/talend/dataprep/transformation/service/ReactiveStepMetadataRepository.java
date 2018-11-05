package org.talend.dataprep.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.multitenant.context.TenancyContext;
import org.talend.daikon.multitenant.context.TenancyContextHolder;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.security.SecurityProxy;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.TopicProcessor;

/**
 * <p>
 * An implementation of {@link StepMetadataRepository} that uses {@link reactor.core.publisher.Flux} to process
 * {@link #invalidate(String) invalidate} and {@link #update(String, RowMetadata) update} requests.
 * </p>
 * <p>
 * Use this implementation for async writes operations to a {@link StepMetadataRepository}.
 * </p>
 */
public class ReactiveStepMetadataRepository implements StepMetadataRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveStepMetadataRepository.class);

    private final StepMetadataRepository delegate;

    private final BlockingSink<InvalidateMessage> invalidates;

    private final BlockingSink<UpdateMessage> updates;

    public ReactiveStepMetadataRepository(StepMetadataRepository delegate, SecurityProxy proxy) {
        this.delegate = delegate;

        final TopicProcessor<InvalidateMessage> invalidateFlux = TopicProcessor.create();
        final TopicProcessor<UpdateMessage> updateFlux = TopicProcessor.create();
        invalidateFlux.subscribe(invalidateMessage -> {
            LOGGER.debug("Delayed invalidate of step #{}.", invalidateMessage.stepId);
            try {
                TenancyContextHolder.setContext(invalidateMessage.context);
                proxy.asTechnicalUser();
                delegate.invalidate(invalidateMessage.stepId);
            } finally {
                proxy.releaseIdentity();
                TenancyContextHolder.clearContext();
            }
            LOGGER.debug("Delayed invalidate of step #{} done.", invalidateMessage.stepId);
        });
        updateFlux.subscribe(updateMessage -> {
            LOGGER.debug("Delayed update of step #{}.", updateMessage.stepId);
            try {
                TenancyContextHolder.setContext(updateMessage.context);
                proxy.asTechnicalUser();
                delegate.update(updateMessage.stepId, updateMessage.rowMetadata);
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to update step metadata for step #{}.", updateMessage.stepId, e);
                } else {
                    LOGGER.error("Unable to update step metadata for step #{}.", updateMessage.stepId);
                }
            } finally {
                proxy.releaseIdentity();
                TenancyContextHolder.clearContext();
            }
            LOGGER.debug("Delayed update of step #{} done.", updateMessage.stepId);
        });

        invalidates = invalidateFlux.connectSink();
        updates = updateFlux.connectSink();

        LOGGER.info("Using asynchronous step row metadata update.");
    }

    @Override
    public RowMetadata get(String stepId) {
        return delegate.get(stepId);
    }

    @Override
    public void update(String stepId, RowMetadata rowMetadata) {
        updates.emit(new UpdateMessage(stepId, rowMetadata, TenancyContextHolder.getContext()));
    }

    @Override
    public void invalidate(String stepId) {
        invalidates.emit(new InvalidateMessage(stepId, TenancyContextHolder.getContext()));
    }

    private static class UpdateMessage {

        private final String stepId;

        private final RowMetadata rowMetadata;

        private final TenancyContext context;

        private UpdateMessage(String stepId, RowMetadata rowMetadata, TenancyContext context) {
            this.stepId = stepId;
            this.rowMetadata = rowMetadata;
            this.context = context;
        }
    }

    private static class InvalidateMessage {

        private final String stepId;

        private final TenancyContext context;

        public InvalidateMessage(String stepId, TenancyContext context) {
            this.stepId = stepId;
            this.context = context;
        }
    }
}
