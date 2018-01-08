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
import java.util.List;
import java.util.Objects;

/**
 * Model a folder tree node.
 */
public class FolderTreeNode implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The node folder
     */
    private Folder folder;

    /**
     * The node children
     */
    private List<FolderTreeNode> children;

    /**
     * Default empty constructor.
     */
    public FolderTreeNode() {
        // for the json serialization
    }

    public FolderTreeNode(final Folder folder, final List<FolderTreeNode> children) {
        this.folder = folder;
        this.children = children;
    }

    /**
     * @return the node folder
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * @param folder the node folder
     */
    public void setFolder(final Folder folder) {
        this.folder = folder;
    }

    /**
     * @return the children
     */
    public List<FolderTreeNode> getChildren() {
        return children;
    }

    /**
     * @param children the node children
     */
    public void setChildren(final List<FolderTreeNode> children) {
        this.children = children;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FolderTreeNode node = (FolderTreeNode) o;
        return Objects.equals(this.folder, node.folder)
                && Objects.equals(children, node.children);
    }

    /**
     * @see Object#hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(folder, children);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "FolderTreeNode {" + //
                "   folder='" + folder + '\'' + //
                ",  children='" + children + '\'' + //
                '}';
    }
}
