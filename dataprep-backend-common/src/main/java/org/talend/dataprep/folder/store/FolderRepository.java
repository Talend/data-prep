package org.talend.dataprep.folder.store;

import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.lock.DistributedLock;

/**
 *
 */
public interface FolderRepository {

    /**
     * @return A {@link java.lang.Iterable iterable} of child {@link Folder folder}. Returned folders are expected to be
     * visible by current user.
     * @param folder the parent folder
     */
    Iterable<Folder> childs(Folder folder);

    /**
     * 
     * @param parent the parent {@link Folder folder}
     * @param child the child {@link Folder folder} to add to the parent
     */
    Folder addFolder(Folder parent, Folder child);

    /**
     * add or update (if alreadu exists) the entry
     * @param parent the parent {@link Folder}
     * @param folderEntry the {@link FolderEntry} to add to the parent
     */
    FolderEntry addFolderEntry(Folder parent, FolderEntry folderEntry);

    /**
     * 
     * @param parent the parent {@link Folder}
     * @param folderEntry the {@link FolderEntry} to remove from the parent
     */
    void removeFolderEntry(Folder parent, FolderEntry folderEntry);

    /**
     * remove folder and content recursively
     * 
     * @param folder the parent {@link Folder} to remove
     */
    void removeFolder(Folder folder);

    /**
     * 
     * @return the root {@link Folder folder}
     */
    Folder rootFolder();

    /**
     *
     * @param folder the parent {@link Folder}
     * @param contentType the contentClass to filter folder entries
     * @return A {@link java.lang.Iterable iterable} of {@link FolderEntry} content filtered for the given type
     */
    Iterable<FolderEntry> entries(Folder folder, String contentType);

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
