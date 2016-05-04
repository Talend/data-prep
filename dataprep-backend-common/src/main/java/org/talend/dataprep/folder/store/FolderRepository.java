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

package org.talend.dataprep.folder.store;

import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;


/**
 * Folder repository that manage folders and folder entries.
 */
public interface FolderRepository {

    /**
     * Return true if the given folder exists.
     * @param path the wanted folder path.
     * @return true if the given folder exists.
     */
    boolean exists(String path);

    /**
     * @return A {@link java.lang.Iterable iterable} of children {@link Folder folder}. Returned folders are expected to be
     * visible by current user.
     * @param path the parent folder in the format /ffo/blab/mm or <code>null</code> for root folder
     */
    Iterable<Folder> children(String path);

    /**
     * 
     * @param path the path to create
     */
    Folder addFolder(String path);

    /**
     * Remove folder and content recursively only if no entry is found.
     * 
     * @param path the path to remove only the last part is removed.
     * @throws NotEmptyFolderException if folder recursively contains any entries (dataset etc...)
     */
    void removeFolder(String path) throws NotEmptyFolderException;

    /**
     * Rename a folder and its content recursively.
     *
     * @param path the path to rename
     * @param newPath the full new path
     */
    void renameFolder(String path, String newPath);

    /**
     * Add or replace (if already exists) the entry.
     *
     * If the path does not exist, it is automatically created.
     *
     * @param folderEntry the {@link FolderEntry} to add.
     * @param path where to add this folder entry.
     */
    FolderEntry addFolderEntry(FolderEntry folderEntry, String path);

    /**
     * Remove a {@link FolderEntry}.
     *
     * @param folderPath the folder path containing the entry
     * @param contentId the id
     * @param contentType  the type dataset, preparation
     */
    void removeFolderEntry(String folderPath, String contentId, FolderContentType contentType);

    /**
     * List the {@link FolderEntry} of the wanted type within the given path.
     * @param path the parent path
     * @param contentType the contentClass to filter folder entries
     * @return A {@link java.lang.Iterable iterable} of {@link FolderEntry} content filtered for the given type
     */
    Iterable<FolderEntry> entries(String path, FolderContentType contentType);

    /**
     * Look for all the {@link FolderEntry} that points to an existing content.
     *
     * It's useful when you want to delete a dataset hence all its references.
     *
     * @param contentId the id
     * @param contentType  the type dataset, preparation
     * @return A {@link Iterable} of the {@link FolderEntry} containing the folderEntry as described by contentType and contentId.
     */
    Iterable<FolderEntry> findFolderEntries(String contentId, FolderContentType contentType);

    /**
     * <b>if the destination or entry doesn't exist a {@link IllegalArgumentException} will be thrown</b>
     *  @param folderEntry the {@link FolderEntry} to move.
     * @param sourcePath where to look for the folder entry.
     * @param destinationPath the destination where to move the entry.
     */
    void moveFolderEntry(FolderEntry folderEntry, String sourcePath, String destinationPath);

    /**
     * Copy the given folder entry to the target destination.
     * @param folderEntry the {@link FolderEntry} to move
     * @param destinationPath the destination where to copy the entry
     */
    void copyFolderEntry(FolderEntry folderEntry, String destinationPath);

    /**
     * Clear the whole content.
     */
    void clear();

    /**
     * used mainly for testing purpose
     * 
     * @return the number of created {@link Folder}
     */
    long size();

    /**
     *
     * @return A {@link Iterable} containing all folders
     */
    Iterable<Folder> allFolder();

    /**
     *
     * @param queryString part of the name to search in folder (not case sensitive)
     * @return A {@link Iterable} of {@link Folder} with the query string in the name
     */
    Iterable<Folder> searchFolders(String queryString);

    /**
     * Return the folder that holds the given content id and content type.
     *
     * @param contentId the wanted content id.
     * @param type the content type.
     * @return the folder that holds the wanted entry or null if not found.
     */
    Folder locateEntry(String contentId, FolderContentType type);
}
