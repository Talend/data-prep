package org.talend.dataprep.folder.store;

import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.lock.DistributedLock;

/**
 *
 */
public interface FolderRepository {

    char PATH_SEPARATOR = '/';

    /**
     * @return A {@link java.lang.Iterable iterable} of child {@link Folder folder}. Returned folders are expected to be
     * visible by current user.
     * @param path the parent folder in the format /ffo/blab/mm or <code>null</code> for root folder
     */
    Iterable<Folder> childs(String path);

    /**
     * 
     * @param path the path to create
     */
    Folder addFolder(String path);

    /**
     * add or update (if already exists) the entry
     * @param folderEntry the {@link FolderEntry} to add
     */
    FolderEntry addFolderEntry(FolderEntry folderEntry);

    /**
     *
     * @param folderEntry the {@link FolderEntry} to remove
     */
    void removeFolderEntry(FolderEntry folderEntry);


    /**
     * remove folder and content recursively
     * 
     * @param path the path to remove only the last part is remove
     */
    void removeFolder(String path);

    /**
     *
     * @param path the parent path
     * @param contentType the contentClass to filter folder entries
     * @return A {@link java.lang.Iterable iterable} of {@link FolderEntry} content filtered for the given type
     */
    Iterable<FolderEntry> entries(String path, String contentType);

    /**
     * clear the whole content
     */
    void clear();

    /**
     * used mainly for testing purpose
     * 
     * @return the number of created {@link Folder}
     */
    int size();

    DistributedLock createFolderLock(String id);

}
