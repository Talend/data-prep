package org.talend.dataprep.dataset.service.messaging.emitter;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface ProductEventsSink {

    @Output("productEvents")
    MessageChannel productEvents();

}
