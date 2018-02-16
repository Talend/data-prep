package org.talend.dataprep.dataset.service.messaging.listener;

import org.apache.avro.generic.IndexedRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.talend.daikon.messages.header.consumer.ExecutionContextUpdater;

@Component
public class KafkaEventListener {

    @Autowired
    private ExecutionContextUpdater executionContextUpdater;

    @StreamListener("dataSetKafkaTest")
    public void onEvent(Message message) {
        IndexedRecord record = (IndexedRecord) message.getPayload();
        executionContextUpdater.updateExecutionContext(record);
        // LOGGER.info("Received a product event " + message.getPayload());

    }

}
