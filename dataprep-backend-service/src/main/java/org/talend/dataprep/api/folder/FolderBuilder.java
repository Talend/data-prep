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

import java.util.HashSet;
import java.util.Set;

import org.talend.dataprep.api.share.Owner;

/**
 * Builder class for Folder.
 */
public class FolderBuilder {

    private String id;
    private String path;
    private String name;
    private String ownerId;
    private Owner owner;
    private String parentId;
    private long creationDate;
    private long lastModificationDate;
    private long nbPreparations;
    private boolean shared = false;
    private Set<String> roles = new HashSet<>();

    private FolderBuilder() {
        this.creationDate = System.currentTimeMillis();
        this.lastModificationDate = this.creationDate;
    }

    public static FolderBuilder folder() {
        return new FolderBuilder();
    }

    public FolderBuilder id(String id) {
        this.id = id;
        return this;
    }

    public FolderBuilder path(String path) {
        this.path = path;
        return this;
    }

    public FolderBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FolderBuilder parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public FolderBuilder owner(Owner owner) {
        this.owner = owner;
        return this;
    }

    public FolderBuilder ownerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public FolderBuilder role(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public FolderBuilder role(boolean shared) {
        this.shared = shared;
        return this;
    }

    public FolderBuilder creationDate(long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public FolderBuilder lastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    public FolderBuilder nbPreparations(long nbPreparations) {
        this.nbPreparations = nbPreparations;
        return this;
    }


    public Folder build() {
        final Folder folder = new Folder();
        folder.setId(id);
        folder.setPath(path);
        folder.setName(name);
        folder.setOwnerId(ownerId);
        folder.setOwner(owner);
        folder.setSharedFolder(shared);
        folder.setRoles(roles);
        folder.setNbPreparations(nbPreparations);
        folder.setCreationDate(creationDate);
        folder.setLastModificationDate(lastModificationDate);
        folder.setParentId(parentId);
        return folder;
    }


}