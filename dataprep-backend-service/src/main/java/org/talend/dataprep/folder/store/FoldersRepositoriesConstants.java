package org.talend.dataprep.folder.store;

import org.talend.dataprep.folder.store.file.FolderPath;

public class FoldersRepositoriesConstants {

    /**
     * Path separator used to separate path elements on string serialization.
     * <p>Should only be used by {@link FolderPath#serializeAsString()}
     * and {@link FolderPath#deserializeFromString(String)}.
     */
    public static final Character PATH_SEPARATOR = '/';

    /** Content type property name in {@link org.talend.dataprep.api.folder.FolderEntry}. */
    public static final String CONTENT_TYPE = "contentType";

    /** Content ID property name in {@link org.talend.dataprep.api.folder.FolderEntry}. */
    public static final String CONTENT_ID = "contentId";

    /** Folder ID property name in {@link org.talend.dataprep.api.folder.FolderEntry}. */
    public static final String FOLDER_ID = "folderId";

    /** Owner ID property name in {@link org.talend.dataprep.api.folder.Folder}. */
    public static final String OWNER_ID = "ownerId";

    /** Path property name in {@link org.talend.dataprep.api.folder.Folder}. */
    public static final String PATH = "path";
}
