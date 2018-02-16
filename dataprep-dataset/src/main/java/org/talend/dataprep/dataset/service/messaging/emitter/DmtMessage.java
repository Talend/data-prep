package org.talend.dataprep.dataset.service.messaging.emitter;

/**
 * Basic test message.
 */
public class DmtMessage {

    String id;
    String message;

    public String getId() {
        return id;
    }

    public DmtMessage setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DmtMessage setMessage(String message) {
        this.message = message;
        return this;
    }
}
