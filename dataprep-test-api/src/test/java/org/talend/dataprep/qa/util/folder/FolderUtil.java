package org.talend.dataprep.qa.util.folder;

import com.jayway.restassured.response.Response;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface FolderUtil {

    /**
     * List all preparations within a folder.
     *
     * @param folderName the folder to seek
     * @return FolderContent
     */
    FolderContent listPreparation(String folderName) throws IOException;

    /**
     * List all folders in Data-prep.
     *
     * @return a {@link Set} of folders.
     */
    List<Folder> listFolders() throws IOException;

    /**
     * Search a {@link Folder} full path in a {@link List} of {@link Folder} and return the corresponding
     * {@link Folder}.
     *
     * @param folderPath the folder full path.
     * @param folders    the {@link List} of {@link Folder}
     * @return a {@link Folder} or <code>null</code> if the folder doesn't exist.
     */
    Folder extractFolder(String folderPath, Collection<Folder> folders) throws IOException;

    /**
     * Search a {@link Folder} from its path.
     *
     * @param folderPath the folder path.
     * @return the searched Folder if it exists.
     * @throws IOException
     */
    Folder searchFolder(String folderPath) throws IOException;

    /**
     * Delete a folder.
     *
     * @param folder the folder to delete
     */
    Response deleteFolder(Folder folder);

    /**
     * Sort a {@link Set} of {@link Folder} in its natural order based on their path.
     *
     * @param folders the {@link Set} of {@link Folder} to sort.
     * @return a natural {@link SortedSet} of the given {@link Folder} {@link Set}.
     */
    SortedSet<Folder> sortFolders(Set<Folder> folders);

    /**
     * Obtain a {@link SortedSet} of {@link Folder} sorted in a natural inverse order based on the folder path.
     *
     * @return an empty {@link SortedSet} of {@link Folder} sorted in a natural inverse order.
     */
    SortedSet<Folder> getEmptyReverseSortedSet();

    /**
     * Split a folder in a {@link Set} folder and subfolders.
     *
     * @param folder  the folder to split.
     * @param folders existing folders.
     * @return a {@link Set} of folders and subfolders.
     */
    Set<Folder> splitFolder(Folder folder, List<Folder> folders);

    /**
     * Return a string that represent the folder for dataprep API depending on the context (OS / EE)
     *
     * @param folder the {@link Folder} to represent.
     * @return a folder API representation.
     */
    String getAPIFolderRepresentation(Folder folder);

}
