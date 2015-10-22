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
     * @param parentPath the parent
     * @param child the child to add to the parent
     */
    Folder addFolder(String parentPath, String child);

    /**
     * add or update (if alreadu exists) the entry
     * @param parent the parent path
     * @param folderEntry the {@link FolderEntry} to add to the parent
     */
    FolderEntry addFolderEntry(String parent, FolderEntry folderEntry);

    /**
     * 
     * @param parent the parent path
     * @param folderEntry the {@link FolderEntry} to remove from the parent
     */
    void removeFolderEntry(String parent, FolderEntry folderEntry);


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
