// ============================================================================
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

/**
 * Model a folder info.
 */
public class FolderInfo implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Folder metadata.
     */
    private Folder folder;

    /**
     * Hierarchy from home to folder.
     */
    private Iterable<Folder> hierarchy;

    /**
     * Default empty constructor.
     */
    public FolderInfo() {
        // for the json serialization
    }

    /**
     * Constructor with folder and hierarchy.
     */
    public FolderInfo(final Folder folder, final Iterable<Folder> hierarchy) {
        this.folder = folder;
        this.hierarchy = hierarchy;
    }

    /**
     * @return the folder metadata.
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder the folder metadata.
     */
    public void setFolder(final Folder folder) {
        this.folder = folder;
    }

    /**
     * @return the folder hierarchy
     */
    public Iterable<Folder> getHierarchy() {
        return hierarchy;
    }

    /**
     * @param hierarchy the list of folder from home to current
     */
    public void setHierarchy(final Iterable<Folder> hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FolderInfo folderInfo = (FolderInfo) o;
        return Objects.equals(folder, folderInfo.folder)
                && Objects.equals(hierarchy, folderInfo.hierarchy);
    }

    /**
     * @see Object#hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(folder, hierarchy);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "FolderInfos{" + //
                "folder='" + folder + '\'' + //
                ", hierarchy=" + hierarchy + //
                '}';
    }
}
