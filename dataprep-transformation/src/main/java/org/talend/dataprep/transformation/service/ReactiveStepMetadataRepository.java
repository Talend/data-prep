package org.talend.dataprep.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;

import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.TopicProcessor;

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
            delegate.update(updateMessage.stepId, updateMessage.rowMetadata);
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
