package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Folder {

    public String path;

    public String id;

    public String ownerId;

    public String getPath() {
        return path;
    }

    public Folder setPath(String path) {
        this.path = path;
        return this;
    }

    public String getId() {
        return id;
    }

    public Folder setId(String id) {
        this.id = id;
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Folder setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }
}
