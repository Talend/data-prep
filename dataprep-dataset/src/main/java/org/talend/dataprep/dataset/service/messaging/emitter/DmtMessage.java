package org.talend.dataprep.dataset.service.messaging.emitter;

/**
 * Basic test message.
 */
public class DmtMessage {

    String ig;
    String message;

    public String getIg() {
        return ig;
    }

    public DmtMessage setIg(String ig) {
        this.ig = ig;
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
