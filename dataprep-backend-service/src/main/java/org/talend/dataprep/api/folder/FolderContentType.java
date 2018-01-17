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

/**
 * Folder content type constants.
 */
public enum FolderContentType {
    /** For datasets. */
    DATASET("dataset"),

    /** For preparations. */
    PREPARATION("preparation");

    /** Constant friendly name. */
    private final String name;

    /**
     * Default constructor.
     * @param name the constant friendly name.
     */
    FolderContentType(String name) {
        this.name = name;
    }

    /**
     * Returns the content type that matches the given name.
     */
    public static FolderContentType fromName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null is not permitted as a valid content type for Folder entry");
        }

        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Folder entry content type : '" + name +'\'', e);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}