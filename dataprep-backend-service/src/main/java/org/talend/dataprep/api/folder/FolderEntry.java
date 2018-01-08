// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.Objects;

public class FolderEntry implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Type of this folder entry (dataset / preparation...). */
    private FolderContentType contentType;

    /** Content id of this entry (datasetId or PerparationId). */
    private String contentId;

    /** Id of the folder for this entry. It is set by the FolderRepository implementation. */
    private String folderId;

    /**
     * Default empty constructor.
     */
    public FolderEntry() {
        // no op only to help Jackson
    }

    /**
     * Constructor.
     * @param contentType the content type.
     * @param contentId the content id.
     */
    public FolderEntry(FolderContentType contentType, String contentId) {
        this.contentType = contentType;
        this.contentId = contentId;
    }

    public FolderContentType getContentType() {
        return contentType;
    }

    public void setContentType(FolderContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * @return the FolderId
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * @param folderId the folderId to set.
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "FolderEntry{" + "contentType='" + contentType.toString() + '\'' + ", contentId='" + contentId + '\''
                + ", folderId='" + folderId + '\'' + '}';
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FolderEntry that = (FolderEntry) o;
        return Objects.equals(contentType, that.contentType) && Objects.equals(contentId, that.contentId)
                && Objects.equals(folderId, that.folderId);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(contentType, contentId, folderId);
    }

}