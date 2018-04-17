package org.talend.dataprep.dataset.client;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class EncodedSample {

    /**
     * Avro schema for data.
     */
    private ObjectNode schema;

    /**
     * Avro raw data as string.
     */
    private String data;

    /** Enumeration value that can be JSON for now. Binary maybe later as Avro might be json or binary... */
    private String encoding;

    public ObjectNode getSchema() {
        return schema;
    }

    public void setSchema(ObjectNode schema) {
        this.schema = schema;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
