package org.talend.dataprep.dataset.service.messaging.emitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.talend.daikon.messages.MessageEnvelope;
import org.talend.daikon.messages.MessageKey;
import org.talend.daikon.messages.MessageTypes;
import org.talend.daikon.messages.envelope.MessageEnvelopeHandler;
import org.talend.daikon.messages.header.producer.MessageHeaderFactory;
import org.talend.daikon.messages.keys.MessageKeyFactory;

@Component
public class KafkaEventEmitter {

    @Autowired
    private MessageHeaderFactory messageHeaderFactory;

    @Autowired
    private MessageEnvelopeHandler messageEnvelopeHandler;

    @Autowired
    private MessageKeyFactory messageKeyFactory;

    @Autowired
    private ProductEventsSink productEventsSink;

    public void sendMessage() {
        String json = "{}";
        MessageEnvelope envelope = messageEnvelopeHandler.wrap(MessageTypes.EVENT, "ProductUpdated", json, "json");

        MessageKey messageKey = messageKeyFactory.createMessageKey();

        Message<MessageEnvelope> message =
                MessageBuilder.withPayload(envelope).setHeader(KafkaHeaders.MESSAGE_KEY, messageKey).build();

        // send event
        productEventsSink.productEvents().send(message);

    }

}
