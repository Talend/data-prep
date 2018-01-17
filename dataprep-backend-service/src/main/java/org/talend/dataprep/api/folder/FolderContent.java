//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.List;

import org.talend.dataprep.api.dataset.DataSetMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Container for a {@link Folder} content.
 */
public class FolderContent implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /**
     * children folders
     */
    @JsonProperty("folders")
    private List<Folder> folders;

    @JsonProperty("datasets")
    private List<DataSetMetadata> datasets;

    public FolderContent() {
        // no op
    }

    public FolderContent(List<DataSetMetadata> datasets, List<Folder> folders) {
        this.datasets = datasets;
        this.folders = folders;
    }

    public List<DataSetMetadata> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DataSetMetadata> datasets) {
        this.datasets = datasets;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @Override
    public String toString()
    {
        return "FolderContent{" +
            "datasets=" + datasets +
            ", folders=" + folders +
            '}';
    }
}
