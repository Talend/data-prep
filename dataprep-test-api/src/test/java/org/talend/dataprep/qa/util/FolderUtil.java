package org.talend.dataprep.qa.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;

public interface FolderUtil {

    /**
     * List all preparations within a folder.
     * 
     * @param folderName the folder to seek
     * @return
     */
    FolderContent listPreparation(String folderName) throws IOException;

    /**
     * List all folders in Data-prep.
     *
     * @return a {@link Set} of folders.
     */
    List<Folder> listFolders() throws IOException;

    /**
     * Search a {@link Folder} full path in a {@link List} of {@link Folder} and return the corresponding {@link Folder}.
     *
     * @param folderPath the folder full path.
     * @param folders the {@link List} of {@link Folder}
     * @return a {@link Folder} or <code>null</code> if the folder doesn't exist.
     */
    Folder extractFolder(String folderPath, List<Folder> folders) throws IOException;
}
