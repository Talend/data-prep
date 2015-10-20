package org.talend.dataprep.api.folder;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.preparation.Identifiable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FolderEntry extends Identifiable implements Serializable {

    @Id
    @JsonProperty("id")
    private String id;

    // FIXME olamy: not sure we need to export that in json result
    @JsonProperty("contentClass")
    /**
     * this is the class of the content Dataset or Preparation or something else..
     */
    private String contentClass;

    /* id of the content i.e datasetId or preparationId */
    private String contentId;

    public FolderEntry(String id, String contentClass, String contentId) {
        this.id = id;
        this.contentClass = contentClass;
        this.contentId = contentId;
    }

    @Override
    public String id() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentClass() {
        return contentClass;
    }

    public void setContentClass(String contentClass) {
        this.contentClass = contentClass;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public String toString() {
        return "FolderEntry{" + "id='" + id + //
                '\'' + ", contentClass='" + contentClass + //
                '\'' + ", contentId='" + contentId + '\'' + '}';
    }
}
