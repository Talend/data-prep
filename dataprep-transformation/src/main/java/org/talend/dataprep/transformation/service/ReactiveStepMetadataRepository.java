package org.talend.dataprep.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;

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

    private final BlockingSink<String> invalidates;

    private final BlockingSink<UpdateMessage> updates;

    public ReactiveStepMetadataRepository(StepMetadataRepository delegate) {
        this.delegate = delegate;

        final TopicProcessor<String> invalidateFlux = TopicProcessor.create();
        final TopicProcessor<UpdateMessage> updateFlux = TopicProcessor.create();
        invalidateFlux.subscribe(stepId -> {
            LOGGER.debug("Delayed invalidate of step #{}.", stepId);
            invalidate(stepId);
            LOGGER.debug("Delayed invalidate of step #{} done.", stepId);
        });
        updateFlux.subscribe(updateMessage -> {
            LOGGER.debug("Delayed update of step #{}.", updateMessage.stepId);
            try {
                delegate.update(updateMessage.stepId, updateMessage.rowMetadata);
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to update step metadata for step #{}.", updateMessage.stepId, e);
                } else {
                    LOGGER.error("Unable to update step metadata for step #{}.", updateMessage.stepId);
                }
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
        updates.emit(new UpdateMessage(stepId, rowMetadata));
    }

    @Override
    public void invalidate(String stepId) {
        invalidates.emit(stepId);
    }

    private static class UpdateMessage {

        private final String stepId;

        private final RowMetadata rowMetadata;

        private UpdateMessage(String stepId, RowMetadata rowMetadata) {
            this.stepId = stepId;
            this.rowMetadata = rowMetadata;
        }
    }
}
