//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.folder;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FolderEntry implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("contentType")
    /**
     * this is the type of the content dataset or preparation or something else..
     */
    private String contentType;

    /* id of the content i.e datasetId or preparationId or something else */
    @JsonProperty("contentId")
    private String contentId;

    @JsonProperty("path")
    private String path;

    public FolderEntry() {
        // no op only to help Jackson
    }

    public FolderEntry(String contentType, String contentId, String path) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.path = path;
        this.buildId();
    }

    public void buildId() {
        this.id = contentType + '@' + contentId + '@' + path;
    }

    public String id() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "FolderEntry{" + "contentId='" + contentId + '\'' + ", id='" + id + '\'' + ", contentType='" + contentType + '\''
                + ", path='" + path + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FolderEntry that = (FolderEntry) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
