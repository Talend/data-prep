package org.talend.dataprep.api.folder;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.preparation.Identifiable;

import java.io.Serializable;

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

    /* id of the content i.e datasetId or preparationId or something else */
    @JsonProperty("contentId")
    private String contentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    public FolderEntry(String name, String contentClass, String contentId, String path) {
        this.name = name;
        this.contentClass = contentClass;
        this.contentId = contentId;
        this.path = path;
    }

    public FolderEntry(String id, String contentClass, String contentId, String name, String path) {
        this(contentClass, contentId, name, path);
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    @Override
    public String toString()
    {
        return "FolderEntry{" +
            "contentClass='" + contentClass + '\'' +
            ", id='" + id + '\'' +
            ", contentId='" + contentId + '\'' +
            ", name='" + name + '\'' +
            ", path='" + path + '\'' +
            '}';
    }
}
