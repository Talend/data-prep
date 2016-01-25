package org.talend.dataprep.api.dataset;

import java.io.Serializable;

/**
 * Bean for a dataset move request. Easier to use a bean rather than manual de/serialization
 */
public class DataSetMoveRequest implements Serializable {

    private String folderPath;

    private String newFolderPath;

    private String newName;

    public DataSetMoveRequest() {
        // no op but Jackson need empty constructor
    }

    public DataSetMoveRequest(String folderPath, String newFolderPath, String newName) {
        this.folderPath = folderPath;
        this.newFolderPath = newFolderPath;
        this.newName = newName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getNewFolderPath() {
        return newFolderPath;
    }

    public void setNewFolderPath(String newFolderPath) {
        this.newFolderPath = newFolderPath;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @Override
    public String toString() {
        return "DataSetMoveRequest{" + "folderPath='" + folderPath + '\'' //
                + ", newFolderPath='" + newFolderPath + '\'' //
                + ", newName='" + newName + '\'' + '}';
    }
}
