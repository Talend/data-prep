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

package org.talend.dataprep.api.dataset;

import java.io.Serializable;

/**
 * Bean for a dataset move request. Easier to use a bean rather than manual de/serialization
 */
class DataSetMoveRequest implements Serializable {

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
