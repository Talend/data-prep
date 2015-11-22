package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.List;

import org.talend.dataprep.api.dataset.DataSetMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Container for a {@link Folder} content.
 */
public class FolderContent implements Serializable {

    /**
     * childs folders
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
